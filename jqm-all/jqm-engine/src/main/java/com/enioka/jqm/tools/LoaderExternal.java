package com.enioka.jqm.tools;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobInstance;

class LoaderExternal implements Runnable
{
    private static Logger jqmlogger = LoggerFactory.getLogger(LoaderExternal.class);

    int jobId;
    String opts;
    String logFile;
    int killCheckPeriodMs = 1000;
    QueuePoller qp = null;

    public LoaderExternal(DbConn cnx, JobInstance job, QueuePoller qp)
    {
        this.jobId = job.getId();
        this.qp = qp;
        opts = job.getJD().getJavaOpts() == null
                ? GlobalParameter.getParameter(cnx, "defaultExternalOpts", "-Xms32m -Xmx128m -XX:MaxPermSize=64m")
                : job.getJD().getJavaOpts();
        killCheckPeriodMs = Integer.parseInt(GlobalParameter.getParameter(cnx, "internalPollingPeriodMs", "1000"));

        logFile = "./logs";
        logFile = FilenameUtils.concat(logFile, StringUtils.leftPad("" + jobId, 10, "0") + ".log");
    }

    @Override
    public void run()
    {
        jqmlogger.debug("Starting external loader for job " + jobId);
        String java_path = FilenameUtils.concat(System.getProperty("java.home"), "bin/java");
        List<String> args = new ArrayList<String>();

        args.add(java_path);
        args.addAll(Arrays.asList(opts.split(" ")));
        args.add("com.enioka.jqm.tools.Main");
        args.add("-s");
        args.add("" + this.jobId);

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        pb.environment().put("CLASSPATH", System.getProperty("java.class.path"));

        Process p = null;
        try
        {
            jqmlogger.debug("Starting external JVM for ID " + jobId);
            p = pb.start();
        }
        catch (IOException e)
        {
            jqmlogger.error("Could not launch an external payload", e);
            qp.decreaseNbThread(this.jobId);
            return;
        }

        // Wait for end, flushing logs.
        int res = -1;
        InputStreamReader isr = null;
        BufferedReader br = null;
        String buf = "";
        FileWriter f = null;
        String linesep = System.getProperty("line.separator");

        try
        {
            isr = new InputStreamReader(p.getInputStream(), "UTF8");
            f = new FileWriter(logFile);
            br = new BufferedReader(isr);

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
                Thread.sleep(1000);
            }
        }
        catch (Exception e)
        {
            jqmlogger.error("could not retrieve external payload flows", e);
        }
        finally
        {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(f);
            IOUtils.closeQuietly(isr);

            qp.decreaseNbThread(this.jobId);
        }

        if (res != 0)
        {
            jqmlogger.error("An external payload has exited with return code " + res + ". Abnormal - it should always be 0.");
        }
    }
}
