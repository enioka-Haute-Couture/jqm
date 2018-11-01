package com.enioka.jqm.tools;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.api.JobInstanceTracker;
import com.enioka.jqm.api.JobRunnerCallback;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Instruction;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShellJobInstanceTracker implements JobInstanceTracker
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ShellJobInstanceTracker.class);

    private JobInstance ji;
    private Process process;
    private JobRunnerCallback cb;
    private String login = null, pwd = null, url = null;

    ShellJobInstanceTracker(JobInstance ji, JobRunnerCallback cb)
    {
        this.ji = ji;
        this.cb = cb;
    }

    @Override
    public void initialize(DbConn cnx)
    {
        login = this.cb.getWebApiUser(cnx).getKey();
        pwd = this.cb.getWebApiUser(cnx).getValue();
        url = this.cb.getWebApiLocalUrl(cnx);
    }

    @Override
    public State run()
    {
        // Program itself, with parameters, possibly with integrated shell
        List<String> args = OsHelpers.getProcessArguments(this.ji);

        // Ready launch
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(false);

        // Environment variables
        Map<String, String> env = new HashMap<String, String>(10);

        env.put("JQM_JD_APPLICATION_NAME", this.ji.getJD().getApplicationName());
        env.put("JQM_JD_KEYWORD_1", this.ji.getJD().getKeyword1() != null ? this.ji.getJD().getKeyword1() : "");
        env.put("JQM_JD_KEYWORD_2", this.ji.getJD().getKeyword2() != null ? this.ji.getJD().getKeyword2() : "");
        env.put("JQM_JD_KEYWORD_3", this.ji.getJD().getKeyword3() != null ? this.ji.getJD().getKeyword3() : "");
        env.put("JQM_JD_MODULE", this.ji.getJD().getModule() != null ? this.ji.getJD().getModule() : null);
        env.put("JQM_JD_PRIORITY", this.ji.getJD().getPriority() != null ? this.ji.getJD().getPriority().toString() : "0");

        env.put("JQM_JI_ID", this.ji.getId() + "");
        env.put("JQM_JI_KEYWORD_1", this.ji.getKeyword1() != null ? this.ji.getKeyword1() : "");
        env.put("JQM_JI_KEYWORD_2", this.ji.getKeyword2() != null ? this.ji.getKeyword2() : "");
        env.put("JQM_JI_KEYWORD_3", this.ji.getKeyword3() != null ? this.ji.getKeyword3() : "");
        env.put("JQM_JI_MODULE", this.ji.getModule() != null ? this.ji.getModule() : "");
        env.put("JQM_JI_USER_NAME", this.ji.getUserName() != null ? this.ji.getUserName() : "");
        env.put("JQM_JI_PARENT_ID", this.ji.getParentId() + "");

        env.put("JQM_NODE_NAME", this.ji.getNode().getName());

        env.put("JQM_Q_NAME", this.ji.getQ().getName());

        env.put("JQM_API_LOGIN", this.login != null ? this.login : "");
        env.put("JQM_API_PASSWORD", this.pwd != null ? this.pwd : "");
        env.put("JQM_API_LOCAL_URL", this.url != null ? this.url : "");

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
            w.waitForEnd();
            jqmlogger.debug("Shell payload " + this.ji.getId() + " - the external process has exited with RC " + res);
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
            this.process.destroy();
            break;
        case PAUSE:
            jqmlogger.warn("Cannot pause a running shell job instance");
            break;
        default:
            // Ignore.
        }
    }
}
