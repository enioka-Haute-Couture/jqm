package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class DeliverableTest extends JqmBaseTest
{
    EntityManager em;

    @Before
    public void before() throws IOException
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Test prepare");

        em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        File jar = FileUtils.listFiles(new File("../jqm-ws/target/"), new String[] { "war" }, false).iterator().next();
        FileUtils.copyFile(jar, new File("./webapp/jqm-ws.war"));

        // We need to reset credentials used inside the client
        JqmClientFactory.resetClient(null);
    }

    @After
    public void after()
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Test cleanup");

        em.close();

        // Java 6 GC being rather inefficient, we must run it multiple times to correctly collect Jetty-created class loaders and avoid
        // permgen issues
        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();
        System.gc();
    }

    /**
     * Retrieve all the files created by a job, with auth, without SSL
     */
    @Test
    public void testGetDeliverables() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetDeliverables");
        Helpers.setSingleParam("noHttp", "false", em);
        Helpers.setSingleParam("useAuth", "true", em);
        Helpers.setSingleParam("useSsl", "false", em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", TestHelpers.node.getDlRepo(), em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable1.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-deliverable/target/test.jar", TestHelpers.qVip, 42,
                "getDeliverables", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("getDeliverables", "MAG");
        int id = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000, em);

        List<InputStream> tmp = JqmClientFactory.getClient().getJobDeliverablesContent(id);
        engine1.stop();
        Assert.assertEquals(1, tmp.size());

        // Assert.assertTrue(tmp.get(0).available() > 0);
        String res = IOUtils.toString(tmp.get(0));
        Assert.assertTrue(res.startsWith("Hello World!"));

        tmp.get(0).close();
    }

    /**
     * Retrieve a remote file with authentication, without SSL.
     */
    @Test
    public void testGetOneDeliverableWithAuth() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetOneDeliverableWithAuth");
        Helpers.setSingleParam("noHttp", "false", em);
        Helpers.setSingleParam("useAuth", "true", em);
        Helpers.setSingleParam("useSsl", "false", em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", TestHelpers.node.getDlRepo(), em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable2.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-deliverable/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "Franquin");

        int jobId = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);

        File f = new File(TestHelpers.node.getDlRepo() + "jqm-test-deliverable2.txt");
        Assert.assertEquals(false, f.exists()); // file should have been moved

        List<com.enioka.jqm.api.Deliverable> files = JqmClientFactory.getClient().getJobDeliverables(jobId);
        Assert.assertEquals(1, files.size());

        InputStream tmp = JqmClientFactory.getClient().getDeliverableContent(files.get(0));
        engine1.stop();

        Assert.assertTrue(tmp.available() > 0);
        String res = IOUtils.toString(tmp);
        Assert.assertTrue(res.startsWith("Hello World!"));

        tmp.close();
    }

    /**
     * Same as above, except authentication is disabled as well as SSL.
     */
    @Test
    public void testGetOneDeliverableWithoutAuth() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetOneDeliverableWithoutAuth");
        Helpers.setSingleParam("noHttp", "false", em);
        Helpers.setSingleParam("useAuth", "false", em);
        Helpers.setSingleParam("useSsl", "false", em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", TestHelpers.node.getDlRepo(), em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable3.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-deliverable/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "Franquin");

        int jobId = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);

        File f = new File(TestHelpers.node.getDlRepo() + "jqm-test-deliverable2.txt");
        Assert.assertEquals(false, f.exists()); // file should have been moved

        List<com.enioka.jqm.api.Deliverable> files = JqmClientFactory.getClient().getJobDeliverables(jobId);
        Assert.assertEquals(1, files.size());

        InputStream tmp = JqmClientFactory.getClient().getDeliverableContent(files.get(0));
        engine1.stop();

        Assert.assertTrue(tmp.available() > 0);
        String res = IOUtils.toString(tmp);
        Assert.assertTrue(res.startsWith("Hello World!"));

        tmp.close();
    }
    
    /**
     * Retrieve a remote file with authentication, with SSL.
     */
    @Test
    public void testGetOneDeliverableWithAuthWithSsl() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetOneDeliverableWithAuthWithSsl");
        Helpers.setSingleParam("noHttp", "false", em);
        Helpers.setSingleParam("useAuth", "true", em);
        Helpers.setSingleParam("useSsl", "true", em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", TestHelpers.node.getDlRepo(), em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable2.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-deliverable/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "Franquin");

        int jobId = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);

        File f = new File(TestHelpers.node.getDlRepo() + "jqm-test-deliverable2.txt");
        Assert.assertEquals(false, f.exists()); // file should have been moved

        List<com.enioka.jqm.api.Deliverable> files = JqmClientFactory.getClient().getJobDeliverables(jobId);
        Assert.assertEquals(1, files.size());

        InputStream tmp = JqmClientFactory.getClient().getDeliverableContent(files.get(0));
        engine1.stop();

        Assert.assertTrue(tmp.available() > 0);
        String res = IOUtils.toString(tmp);
        Assert.assertTrue(res.startsWith("Hello World!"));

        tmp.close();
    }

    /**
     * This test is DB only - no simple service use
     */
    @Test
    public void testGetAllDeliverables() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetAllDeliverables");

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", TestHelpers.node.getDlRepo(), em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable4.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-deliverable/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "Franquin");

        int id = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000, em);

        List<com.enioka.jqm.api.Deliverable> tmp = JqmClientFactory.getClient().getJobDeliverables(id);
        engine1.stop();

        Assert.assertEquals(1, tmp.size());
    }
}
