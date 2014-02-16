package com.enioka.jqm.tools;

import java.io.FileNotFoundException;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

/**
 * These are not automated tests, but helpers during dev time. They are not meant to complete successfully.
 * 
 * @author Marc-Antoine
 * 
 */
public class ManualTests
{
    public static Logger jqmlogger = Logger.getLogger(ManualTests.class);
    public static Server s;

    @BeforeClass
    public static void testInit() throws InterruptedException, FileNotFoundException
    {
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();

        JqmClientFactory.resetClient(null);
        Helpers.resetEmf();
        CreationTools.reset();
    }

    @AfterClass
    public static void stop()
    {
        JqmClientFactory.resetClient(null);
        s.shutdown();
        s.stop();
    }

    @Test
    public void jmxTestEnvt() throws InterruptedException
    {
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo",
                null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, null, "jqm-tests/jqm-test-kill/target/test.jar", TestHelpers.qVip, 42,
                "KillApp", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        Main.main(new String[] { "-startnode", "localhost" });
        Main.main(new String[] { "-startnode", "localhost4" });

        JobRequest form = new JobRequest("Geo", "test");
        form.addParameter("nbJob", "1");
        JqmClientFactory.getClient().enqueue(form);

        form = new JobRequest("Fibo", "test");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");
        JqmClientFactory.getClient().enqueue(form);

        form = new JobRequest("KillApp", "test");
        JqmClientFactory.getClient().enqueue(form);

        Thread.sleep(Integer.MAX_VALUE);
    }
}
