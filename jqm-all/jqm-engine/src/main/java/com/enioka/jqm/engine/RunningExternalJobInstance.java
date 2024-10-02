package com.enioka.jqm.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.enioka.jqm.cl.ExtClassLoader;
import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobInstance;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tracker and launcher for running payloads inside a {@link JqmSingleRunner} running in a new process.
 */
class RunningExternalJobInstance implements Runnable
{
    private static Logger jqmlogger = LoggerFactory.getLogger(RunningExternalJobInstance.class);

    long jobId;
    JobInstance ji;
    String opts;
    String logFilePath;
    String rootPath;
    int killCheckPeriodMs = 1000;
    QueuePoller qp = null;

    public RunningExternalJobInstance(DbConn cnx, JobInstance job, QueuePoller qp) throws IOException
    {
        this.jobId = job.getId();
        this.ji = job;
        this.qp = qp;
        opts = job.getJD().getJavaOpts() == null
                ? GlobalParameter.getParameter(cnx, "defaultExternalOpts", "-Xms32m -Xmx128m -XX:MaxMetaspaceSize=64m")
                : job.getJD().getJavaOpts();
        killCheckPeriodMs = Integer.parseInt(GlobalParameter.getParameter(cnx, "internalPollingPeriodMs", "1000"));

        rootPath = ExtClassLoader.getRootDir();

        logFilePath = FilenameUtils.concat(rootPath, "./logs");
        logFilePath = FilenameUtils.concat(logFilePath, StringUtils.leftPad("" + jobId, 10, "0") + ".log");
        jqmlogger.debug("Using {} as log path", logFilePath);

        File logFileDir = new File(FilenameUtils.getFullPath(logFilePath));

        if (!logFileDir.isDirectory() && !logFileDir.mkdirs())
        {
            throw new JqmInitError("Could not create directory " + logFileDir.getAbsolutePath());
        }
    }

    @Override
    public void run()
    {
        jqmlogger.debug("Starting external loader for job " + jobId);
        String java_path = FilenameUtils.concat(System.getProperty("java.home"), "bin/java");
        List<String> args = new ArrayList<>();

        args.add(java_path);
        args.addAll(Arrays.asList(opts.split(" ")));
        args.add("-jar");
        args.add(FilenameUtils.concat(rootPath, "jqm.jar"));
        args.add("Start-Single");
        args.add("--id");
        args.add("" + this.jobId);
        jqmlogger.trace("Starting JVM arguments are {}", args);

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);

        Process p = null;
        try
        {
            jqmlogger.debug("Starting external JVM for ID " + jobId);
            p = pb.start();
            jqmlogger.debug("External process was started with PID {}", p.pid());
        }
        catch (IOException e)
        {
            jqmlogger.error("Could not launch an external payload", e);
            qp.releaseResources(this.ji);
            return;
        }

        // Wait for end, flushing logs.
        int res = -1;
        String buf = "";
        String linesep = System.getProperty("line.separator");

        try (InputStreamReader isr = new InputStreamReader(p.getInputStream(), "UTF8");
                FileWriter f = new FileWriter(logFilePath);
                BufferedReader br = new BufferedReader(isr);)
        {
            while (true)
            {
                // Dump log file
                while (br.ready())
                {
                    buf = br.readLine();
                    if (buf != null)
                    {
                        f.write(buf + linesep);
                        jqmlogger.debug(buf);
                    }
                }

                // Done?
                try
                {
                    res = p.exitValue();
                    jqmlogger.debug("External payload " + jobId + " - the external process has exited with RC " + res);
                    // if here, has exited.
                    break;
                }
                catch (IllegalThreadStateException t)
                {
                    // Nothing to do - it means process still running
                }

                // Wait between loops.
                Thread.sleep(100);
            }
        }
        catch (Exception e)
        {
            jqmlogger.error("could not retrieve external payload flows", e);
        }
        finally
        {
            qp.releaseResources(this.ji);
        }

        if (res != 0)
        {
            jqmlogger.error("An external payload has exited with return code " + res + ". Abnormal - it should always be 0.");
        }
    }
}
