package com.enioka.jqm.integration.tests;

import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * All tests directly concerning payload logging.
 */
public class LogTest extends JqmBaseTest {
    @Before
    public void beforeTests() {
        if (Files.exists(Path.of("./target/server/logs"))) {
            try {
                FileUtils.cleanDirectory(new File("./target/server/logs"));
            } catch (IOException e) {
                throw new RuntimeException("Could not clean log directory", e);
            }
        }
    }

    @Test
    public void testLogPerLaunchJavaRunner() throws IOException {
        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;

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
        File f = new File(FilenameUtils.concat("./target/server/logs", fileName));

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertTrue(f.exists());

        newOut.flush();
        Assert.assertEquals(0, Files.size(Path.of("jobsoutput.log")));

        System.setErr(err_ini);
        System.setOut(out_ini);
    }

    @Test
    public void testBothLogJavaRunner() throws IOException {


        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;

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
        File f = new File(FilenameUtils.concat("./target/server/logs", fileName));

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertTrue(f.exists());

        Assert.assertTrue(Files.size(Path.of("jobsoutput.log")) != 0);

        FileUtils.write(new File("jobsoutput.log"), "", Charset.defaultCharset());
        newOut.flush();
        System.setErr(err_ini);
        System.setOut(out_ini);
    }

    @Test
    public void testNoLogJavaRunner() throws IOException {
        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;

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
        File f = new File(FilenameUtils.concat("./target/server/logs", fileName));

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertFalse(f.exists());

        newOut.flush();
        Assert.assertEquals(0, Files.size(Path.of("jobsoutput.log")));

        System.setErr(err_ini);
        System.setOut(out_ini);

    }

    @Test
    public void testNoLogForShellJob() throws Exception {
        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        try {
            final String logTextStdout = "log into stdout";
            final String logTextStderr = "log into stderr";

            GlobalParameter.setParameter(cnx, "logFilePerLaunch", "false");

            final String shellCommandString = "echo " + logTextStdout + ";echo " + logTextStderr + " >&2";
            CreationTools.createJobDef("test job", true, "none", new HashMap<>(), shellCommandString, TestHelpers.qNormal, 0, "TestApp1", null, "module1",
                "kw1", "kw2", null, false, cnx, null, false, null, false, JobDef.PathType.DEFAULTSHELLCOMMAND);
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

            Assert.assertEquals(0, Files.size(Path.of("jobsoutput.log")));
        } finally {
            System.setErr(err_ini);
            System.setOut(out_ini);
            System.out.write(byteArrayOutputStream.toByteArray());
        }
    }

    @Test
    public void testOnlyFileLogForShellJob() throws Exception {
        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        try {
            final String logTextStdout = "log into stdout";
            final String logTextStderr = "log into stderr";

            GlobalParameter.setParameter(cnx, "logFilePerLaunch", "true");

            final String shellCommandString = "echo " + logTextStdout + ";echo " + logTextStderr + " >&2";
            CreationTools.createJobDef("test job", true, "none", new HashMap<>(), shellCommandString, TestHelpers.qNormal, 0, "TestApp1", null, "module1",
                "kw1", "kw2", null, false, cnx, null, false, null, false, JobDef.PathType.DEFAULTSHELLCOMMAND);
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

            Assert.assertEquals(0, Files.size(Path.of("jobsoutput.log")));
        } finally {
            System.setErr(err_ini);
            System.setOut(out_ini);
            System.out.write(byteArrayOutputStream.toByteArray());
        }
    }

    @Test
    public void testMultiLogForShellJob() throws Exception {
        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(byteArrayOutputStream);
        System.setOut(newOut);

        try {
            final String logTextStdout = "log into stdout";
            final String logTextStderr = "log into stderr";

            GlobalParameter.setParameter(cnx, "logFilePerLaunch", "both");

            final String shellCommandString = "echo " + logTextStdout + ";echo " + logTextStderr + " >&2";
            CreationTools.createJobDef("test job", true, "none", new HashMap<>(), shellCommandString, TestHelpers.qNormal, 0, "TestApp1", null, "module1",
                "kw1", "kw2", null, false, cnx, null, false, null, false, JobDef.PathType.DEFAULTSHELLCOMMAND);
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

            Assert.assertTrue(Files.size(Path.of("jobsoutput.log")) != 0);


        } finally {
            System.setErr(err_ini);
            System.setOut(out_ini);
            System.out.write(byteArrayOutputStream.toByteArray());
            FileUtils.write(new File("jobsoutput.log"), "", Charset.defaultCharset());
        }
    }

}
