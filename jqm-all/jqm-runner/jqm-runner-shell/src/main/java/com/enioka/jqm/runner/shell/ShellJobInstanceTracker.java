package com.enioka.jqm.runner.shell;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.api.JobRunnerException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Instruction;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.State;
import com.enioka.jqm.runner.api.JobInstanceTracker;
import com.enioka.jqm.runner.api.JobRunnerCallback;
import com.enioka.jqm.runner.shell.api.jmx.ShellJobInstanceTrackerMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShellJobInstanceTracker implements JobInstanceTracker, ShellJobInstanceTrackerMBean
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ShellJobInstanceTracker.class);

    private JobInstance ji;
    private Process process;
    private JobRunnerCallback cb;
    private JobManager engineApi;
    private String login = null, pwd = null, url = null;
    private ObjectName name = null;
    private File tmpDir = null, deliveryDir = null;

    ShellJobInstanceTracker(JobInstance ji, JobRunnerCallback cb, JobManager engineApi)
    {
        this.ji = ji;
        this.cb = cb;
        this.engineApi = engineApi;
    }

    @Override
    public void initialize(DbConn cnx)
    {
        // API user
        login = this.cb.getWebApiUser(cnx).getKey();
        pwd = this.cb.getWebApiUser(cnx).getValue();
        url = this.cb.getWebApiLocalUrl(cnx);

        // JMX
        if (cb != null && cb.isJmxEnabled())
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try
            {
                name = new ObjectName(cb.getJmxBeanName());
                mbs.registerMBean(this, name);
            }
            catch (Exception e)
            {
                throw new JobRunnerException("Could not create JMX bean for running job instance", e);
            }
        }

        // Temp work directory
        tmpDir = this.engineApi.getWorkDir();

        // Delivery
        deliveryDir = new File(tmpDir.getAbsolutePath() + "_delivery");
        if (!deliveryDir.mkdirs())
        {
            throw new JobRunnerException("Could not create delivery directory");
        }
    }

    @Override
    public State run()
    {
        // Program itself, with parameters, possibly with integrated shell
        List<String> args = OsHelpers.getProcessArguments(this.ji);

        // Ready launch
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.directory(new File(this.ji.getNode().getRepo()));
        pb.redirectErrorStream(false);

        // Environment variables
        Map<String, String> env = new HashMap<>(10);

        env.put("JQM_JD_APPLICATION_NAME", this.ji.getJD().getApplicationName());
        env.put("JQM_JD_KEYWORD_1", this.ji.getJD().getKeyword1() != null ? this.ji.getJD().getKeyword1() : "");
        env.put("JQM_JD_KEYWORD_2", this.ji.getJD().getKeyword2() != null ? this.ji.getJD().getKeyword2() : "");
        env.put("JQM_JD_KEYWORD_3", this.ji.getJD().getKeyword3() != null ? this.ji.getJD().getKeyword3() : "");
        env.put("JQM_JD_MODULE", this.ji.getJD().getModule() != null ? this.ji.getJD().getModule() : "");
        env.put("JQM_JD_PRIORITY", this.ji.getJD().getPriority() != null ? this.ji.getJD().getPriority().toString() : "0");

        env.put("JQM_JI_ID", this.ji.getId() + "");
        env.put("JQM_JI_KEYWORD_1", this.ji.getKeyword1() != null ? this.ji.getKeyword1() : "");
        env.put("JQM_JI_KEYWORD_2", this.ji.getKeyword2() != null ? this.ji.getKeyword2() : "");
        env.put("JQM_JI_KEYWORD_3", this.ji.getKeyword3() != null ? this.ji.getKeyword3() : "");
        env.put("JQM_JI_MODULE", this.ji.getModule() != null ? this.ji.getModule() : "");
        env.put("JQM_JI_USER_NAME", this.ji.getUserName() != null ? this.ji.getUserName() : "");
        env.put("JQM_JI_PARENT_ID", this.ji.getParentId() + "");
        env.put("JQM_JI_TEMP_DIR", this.tmpDir.getAbsolutePath());
        env.put("JQM_JI_DELIVERY_DIR", this.deliveryDir.getAbsolutePath());

        env.put("JQM_NODE_NAME", this.ji.getNode().getName());
        env.put("JQM_NODE_APPLICATION_ROOT", this.ji.getNode().getRepo());
        env.put("JQM_NODE_LOG_LEVEL", this.ji.getNode().getRootLogLevel());

        env.put("JQM_Q_NAME", this.ji.getQ().getName());

        env.put("JQM_API_LOGIN", this.login != null ? this.login : "");
        env.put("JQM_API_PASSWORD", this.pwd != null ? this.pwd : "");
        env.put("JQM_API_LOCAL_URL", this.url != null ? this.url : "");

        env.putAll(ji.getEnvVarCache());

        pb.environment().putAll(env);

        // Start
        try
        {
            jqmlogger.debug("Starting process for shell payload " + this.ji.getId() + " - arguments: " + args.toString());
            process = pb.start();
        }
        catch (IOException e)
        {
            jqmlogger.error("Could not launch a shell payload", e);
            return State.CRASHED;
        }

        // Wait for end, flushing logs.
        try
        {
            Waiter w = StreamGobbler.plumbProcess(process);
            int res = process.waitFor();
            jqmlogger.debug("Shell payload " + this.ji.getId() + " - the external process has exited with RC " + res);
            w.waitForEnd();
            jqmlogger.debug("External process outputs are closed");
            return res == 0 ? State.ENDED : State.CRASHED;
        }
        catch (InterruptedException e)
        {
            // Clear status and return
            Thread.interrupted();
            return State.CRASHED;
        }
        catch (Exception e)
        {
            jqmlogger.warn("Shell job plumbing has failed", e);
            return State.CRASHED;
        }
    }

    @Override
    public void wrap()
    {
        // Unregister MBean
        if (this.cb != null && this.cb.isJmxEnabled())
        {
            try
            {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.unregisterMBean(name);
            }
            catch (Exception e)
            {
                jqmlogger.error("Could not unregister JobInstance JMX bean", e);
            }
        }

        // Collect delivery files
        for (File f : this.deliveryDir.listFiles())
        {
            try
            {
                this.engineApi.addDeliverable(f.getAbsolutePath(), f.getName());
            }
            catch (IOException e)
            {
                jqmlogger.warn("Could not register delivery file " + f.getAbsolutePath(), e);
            }
        }
        this.deliveryDir.delete();
    }

    @Override
    public void handleInstruction(Instruction instruction)
    {
        switch (instruction)
        {
        case KILL:
            // For shell job instances, this is rather easy. This is however platform-dependant, as forking OSes will kill the tree, and
            // spawning OSes will only kill the process itself.
            jqmlogger.debug("Killing process");
            if (this.process == null)
            {
                return; // handle it next time.
            }
            KillHelpers.kill(this.process);
            break;
        case PAUSE:
            jqmlogger.warn("Cannot pause a running shell job instance");
            break;
        default:
            // Ignore.
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // JMX
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void kill()
    {
        this.handleInstruction(Instruction.KILL);
    }

    @Override
    public String getApplicationName()
    {
        return this.ji.getJD().getApplicationName();
    }

    @Override
    public Calendar getEnqueueDate()
    {
        return this.ji.getCreationDate();
    }

    @Override
    public String getKeyword1()
    {
        return this.ji.getKeyword1();
    }

    @Override
    public String getKeyword2()
    {
        return this.ji.getKeyword2();
    }

    @Override
    public String getKeyword3()
    {
        return this.ji.getKeyword3();
    }

    @Override
    public String getModule()
    {
        return this.ji.getModule();
    }

    @Override
    public String getUser()
    {
        return this.ji.getUserName();
    }

    @Override
    public String getSessionId()
    {
        return this.ji.getSessionID();
    }

    @Override
    public Long getId()
    {
        return this.ji.getId();
    }
}
