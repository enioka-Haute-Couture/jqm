package com.enioka.jqm.integration.tests;

import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * All tests directly concerning payload logging.
 */
public class LogTest extends JqmBaseTest
{
    @Before
    public void beforeTests()
    {
        if (Files.exists(Path.of("./target/server/logs")))
        {
            try
            {
                FileUtils.cleanDirectory(new File("./target/server/logs"));
            }
            catch (IOException e)
            {
                throw new RuntimeException("Could not clean log directory", e);
            }
        }
    }

    @Test
    public void testLogPerLaunchJavaRunner() throws IOException
    {
        PrintStream outIni = System.out;
        PrintStream errIni = System.err;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        GlobalParameter.setParameter(cnx, "logFilePerLaunch", "true");
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        cnx.commit();
        long i = jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();
        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
        String filePath = FilenameUtils.concat("./target/server/logs", fileName);
        File f = new File(filePath);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertTrue(f.exists());

        newOut.flush();

        // check that the alljobslogger do not log into logfile
        Assert.assertFalse(Files.readString(Path.of(filePath), StandardCharsets.UTF_8).contains("alljobslogger"));

        System.setErr(errIni);
        System.setOut(outIni);
    }

    // @Test
    // logback from test/resources is not use when launching test from jqm-all (CI breaks)
    public void testBothLogJavaRunner() throws IOException
    {

        PrintStream outIni = System.out;
        PrintStream errIni = System.err;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        GlobalParameter.setParameter(cnx, "logFilePerLaunch", "both");
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        cnx.commit();
        long i = jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();
        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
        String filePath = FilenameUtils.concat("./target/server/logs", fileName);
        File f = new File(filePath);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertTrue(f.exists());

        newOut.flush();

        // check that the alljobslogger log into logfile. In real life, the logger write into another file (see logback configuration)
        Assert.assertTrue(Files.readString(Path.of(filePath), StandardCharsets.UTF_8).contains("alljobslogger"));

        System.setErr(errIni);
        System.setOut(outIni);
    }

    @Test
    public void testNoLogJavaRunner()
    {
        PrintStream outIni = System.out;
        PrintStream errIni = System.err;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        GlobalParameter.setParameter(cnx, "logFilePerLaunch", "false");
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        cnx.commit();
        long i = jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();
        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
        String filePath = FilenameUtils.concat("./target/server/logs", fileName);
        File f = new File(filePath);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertFalse(f.exists());

        newOut.flush();

        System.setErr(errIni);
        System.setOut(outIni);

    }

    @Test
    public void testNoLogForShellJob() throws Exception
    {
        PrintStream outIni = System.out;
        PrintStream errIni = System.err;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        try
        {
            final String logTextStdout = "log into stdout";
            final String logTextStderr = "log into stderr";

            GlobalParameter.setParameter(cnx, "logFilePerLaunch", "false");

            final String shellCommandString = "echo " + logTextStdout + ";echo " + logTextStderr + " >&2";
            CreationTools.createJobDef("test job", true, "none", new HashMap<>(), shellCommandString, TestHelpers.qNormal, 0, "TestApp1",
                    null, "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, JobDef.PathType.DEFAULTSHELLCOMMAND);
            cnx.commit();
            long i = jqmClient.newJobRequest("TestApp1", "TestUser").enqueue();
            addAndStartEngine();
            TestHelpers.waitFor(1, 20000, cnx);

            String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
            File f = new File(FilenameUtils.concat("./target/server/logs", fileName));

            Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
            Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
            Assert.assertFalse(f.exists());

            String fileNameErr = StringUtils.leftPad("" + i, 10, "0") + ".stderr.log";
            File fErr = new File(FilenameUtils.concat("./target/server/logs", fileNameErr));
            Assert.assertFalse(fErr.exists());

            newOut.flush();

            String outContent = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            Assert.assertFalse(outContent.contains("|" + logTextStdout));
            Assert.assertFalse(outContent.contains("|" + logTextStderr));
        }
        finally
        {
            System.setErr(errIni);
            System.setOut(outIni);
            System.out.write(byteArrayOutputStream.toByteArray());
        }
    }

    @Test
    public void testOnlyFileLogForShellJob() throws Exception
    {
        PrintStream outIni = System.out;
        PrintStream errIni = System.err;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        try
        {
            final String logTextStdout = "log into stdout";
            final String logTextStderr = "log into stderr";

            GlobalParameter.setParameter(cnx, "logFilePerLaunch", "true");

            final String shellCommandString = "echo " + logTextStdout + ";echo " + logTextStderr + " >&2";
            CreationTools.createJobDef("test job", true, "none", new HashMap<>(), shellCommandString, TestHelpers.qNormal, 0, "TestApp1",
                    null, "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, JobDef.PathType.DEFAULTSHELLCOMMAND);
            cnx.commit();
            long i = jqmClient.newJobRequest("TestApp1", "TestUser").enqueue();
            addAndStartEngine();
            TestHelpers.waitFor(1, 20000, cnx);

            String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
            File f = new File(FilenameUtils.concat("./target/server/logs", fileName));

            Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
            Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
            Assert.assertTrue(f.exists());

            String content = FileUtils.readFileToString(f, "UTF-8");
            Assert.assertEquals(logTextStdout, content.trim());

            String fileNameErr = StringUtils.leftPad("" + i, 10, "0") + ".stderr.log";
            File fErr = new File(FilenameUtils.concat("./target/server/logs", fileNameErr));
            Assert.assertTrue(fErr.exists());

            String contentErr = FileUtils.readFileToString(fErr, "UTF-8");
            Assert.assertEquals(logTextStderr, contentErr.trim());

            newOut.flush();

            String outContent = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            Assert.assertFalse(outContent.contains("|" + logTextStdout));
            Assert.assertFalse(outContent.contains("|" + logTextStderr));
        }
        finally
        {
            System.setErr(errIni);
            System.setOut(outIni);
            System.out.write(byteArrayOutputStream.toByteArray());
        }
    }

    // @Test
    // logback from test/resources is not use when launching test from jqm-all (CI breaks)
    public void testMultiLogForShellJob() throws Exception
    {
        PrintStream outIni = System.out;
        PrintStream errIni = System.err;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        try
        {
            final String logTextStdout = "log into stdout";
            final String logTextStderr = "log into stderr";

            GlobalParameter.setParameter(cnx, "logFilePerLaunch", "both");

            final String shellCommandString = "echo " + logTextStdout + ";echo " + logTextStderr + " >&2";
            CreationTools.createJobDef("test job", true, "none", new HashMap<>(), shellCommandString, TestHelpers.qNormal, 0, "TestApp1",
                    null, "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, JobDef.PathType.DEFAULTSHELLCOMMAND);
            cnx.commit();
            long i = jqmClient.newJobRequest("TestApp1", "TestUser").enqueue();
            addAndStartEngine();
            TestHelpers.waitFor(1, 20000, cnx);

            String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
            File f = new File(FilenameUtils.concat("./target/server/logs", fileName));

            Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
            Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
            Assert.assertTrue(f.exists());

            String content = FileUtils.readFileToString(f, "UTF-8");
            Assert.assertEquals(logTextStdout, content.trim());

            String fileNameErr = StringUtils.leftPad("" + i, 10, "0") + ".stderr.log";
            File fErr = new File(FilenameUtils.concat("./target/server/logs", fileNameErr));
            Assert.assertTrue(fErr.exists());

            String contentErr = FileUtils.readFileToString(fErr, "UTF-8");
            Assert.assertEquals(logTextStderr, contentErr.trim());

            newOut.flush();

            String outContent = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            Assert.assertTrue(outContent.contains("|" + logTextStdout));
            Assert.assertTrue(outContent.contains("|" + logTextStderr));

        }
        finally
        {
            System.setErr(errIni);
            System.setOut(outIni);
            System.out.write(byteArrayOutputStream.toByteArray());
        }
    }

    @Test
    public void testJobExceptionLogLevel()
    {
        CreationTools.createJobDef(null, true, "pyl.SecThrow", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "CrashingJob", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        cnx.commit();
        addAndStartEngine();

        Logger logger = (Logger) LoggerFactory.getLogger("com.enioka.jqm");
        Level originalLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.list = new CopyOnWriteArrayList<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);
        try
        {
            String[] configuredLevels = { null, "TRACE", "DEBUG", "INFO", "WARN", "ERROR", " warn ", "invalid", "   " };
            Level[] expectedLevels = { Level.INFO, Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.WARN, Level.INFO,
                    Level.INFO };
            for (int i = 0; i < configuredLevels.length; i++)
            {
                if (i > 0)
                {
                    GlobalParameter.setParameter(cnx, "jobExceptionLogLevel", configuredLevels[i]);
                    cnx.commit();
                }
                appender.list.clear();
                long id = jqmClient.newJobRequest("CrashingJob", "TestUser").enqueue();
                TestHelpers.waitFor(i + 1, 20000, cnx);
                Assert.assertEquals(i + 1, TestHelpers.getNonOkCount(cnx));
                Assert.assertEquals(0, TestHelpers.getOkCount(cnx));

                String message = "Job instance " + id + " has crashed. Exception was:";
                ILoggingEvent event = appender.list.stream().filter(e -> message.equals(e.getFormattedMessage())).findFirst()
                        .orElseThrow(() -> new AssertionError("Missing job exception log: " + message));
                Assert.assertEquals(expectedLevels[i], event.getLevel());
                Assert.assertNotNull(event.getThrowableProxy());
                if (i >= 7)
                {
                    Assert.assertTrue(appender.list.stream().anyMatch(
                            e -> e.getLevel() == Level.WARN && e.getFormattedMessage().startsWith("Invalid jobExceptionLogLevel")));
                }
            }
        }
        finally
        {
            logger.setLevel(originalLevel);
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
