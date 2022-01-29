package com.enioka.jqm.integration.tests;

import java.io.File;
import java.io.PrintStream;

import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * All tests directly concerning payload logging.
 */
public class LogTest extends JqmBaseTest
{
    @Test
    public void testMultiLog() throws Exception
    {
        PrintStream out_ini = System.out;
        PrintStream err_ini = System.err;
        FileUtils.cleanDirectory(new File("./target/server/logs"));

        GlobalParameter.setParameter(cnx, "logFilePerLaunch", "true");
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        cnx.commit();
        int i = jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();
        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        String fileName = StringUtils.leftPad("" + i, 10, "0") + ".stdout.log";
        File f = new File(FilenameUtils.concat("./target/server/logs", fileName));

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
        Assert.assertTrue(f.exists());

        System.setErr(err_ini);
        System.setOut(out_ini);
    }
}
