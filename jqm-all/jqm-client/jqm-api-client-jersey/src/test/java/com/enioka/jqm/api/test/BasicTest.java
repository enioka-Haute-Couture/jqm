package com.enioka.jqm.api.test;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.JqmInvalidRequestException;
import com.enioka.jqm.api.Queue;
import com.enioka.jqm.api.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class BasicTest
{
    public static Server server;

    public static void startJettyServer() throws Exception
    {
        // Reset existing envt
        if (server != null)
        {
            stopJettyServer();
        }
        JqmClientFactory.resetClient();

        // Generic Jetty configuration
        System.out.println("Starting Jetty");
        server = new Server(0);
        server.setSendServerVersion(true);
        // QueuedThreadPool threadPool = new QueuedThreadPool();
        // threadPool.setMaxThreads(Math.max(10, (Runtime.getRuntime().availableProcessors())));
        // server.setThreadPool(threadPool);

        // Load web application.
        WebAppContext webAppContext = new WebAppContext("../../jqm-ws/target/jqm-ws.war", "/marsu");
        webAppContext.setDisplayName("JettyContextForWsTest");

        // Add all libraries (packaged by project WS - these are the 'provided' dependencies)
        String classpath = "";
        for (File f : (new File("../../jqm-ws/target/test-libs").listFiles()))
        {
            if (f.isFile() && f.getPath().endsWith(".jar"))
            {
                classpath += f.getPath() + ",";
            }
        }
        webAppContext.setExtraClasspath(classpath);

        // Hide server classes from the web app
        final int nbEx = 9;
        String[] defExcl = webAppContext.getDefaultServerClasses();
        String[] exclusions = new String[defExcl.length + nbEx];
        for (int i = nbEx; i <= defExcl.length; i++)
        {
            exclusions[i] = defExcl[i - nbEx];
        }

        exclusions[0] = "com.enioka.";
        exclusions[1] = "org.hibernate.";
        exclusions[2] = "org.jboss.";
        exclusions[3] = "org.slf4j.";
        exclusions[4] = "org.apache.log4j.";
        exclusions[5] = "org.glassfish."; // Jersey
        exclusions[6] = "org.junit.";
        exclusions[7] = "org.jvnet.";
        // exclusions[8] = "javax.persistence.";
        webAppContext.setServerClasses(exclusions);

        // Set configurations (order is important: need to unpack war before reading web.xml)
        webAppContext.setConfigurations(new Configuration[] { new WebInfConfiguration(), new WebXmlConfiguration(),
                new MetaInfConfiguration(), new FragmentConfiguration(), // new EnvConfiguration(), new PlusConfiguration(),
                new AnnotationConfiguration(), new TagLibConfiguration() });

        server.setHandler(webAppContext);

        server.start();
        System.out.println("Jetty has started");

        Properties p = new Properties();
        System.out.println("jetty on " + getServerPort());
        p.put("com.enioka.ws.url", "http://localhost:" + getServerPort() + "/marsu/ws");
        JqmClientFactory.setProperties(p);
    }

    public static void stopJettyServer()
    {
        try
        {
            System.out.println("Stopping Jetty");
            for (org.eclipse.jetty.server.Handler h : server.getHandlers())
            {
                h.stop();
                h.destroy();
            }
            server.stop();
            server.join();
            server.destroy();
            server = null;
            System.out.println("Jetty has stopped");
        }
        catch (Exception e)
        {
            // Nothing to do - error on exit = no issue.
        }
        server = null;
    }

    public static int getServerPort()
    {
        return server.getConnectors()[0].getLocalPort();
    }

    @Test
    public void testStartServer() throws Exception
    {
        // Just test no exception
        startJettyServer();
        stopJettyServer();
    }

    @Test
    public void testList() throws Exception
    {
        startJettyServer();
        CreationTools.reinitHsqldbServer();
        List<JobInstance> res = JqmClientFactory.getClient().getJobs();
        stopJettyServer();

        Assert.assertEquals(0, res.size());
    }

    @Test
    public void testListQueue() throws Exception
    {
        // Create environment
        CreationTools.reinitHsqldbServer();
        TestHelpers.createLocalNode(CreationTools.emf.createEntityManager());
        startJettyServer();

        // Metadata query
        List<Queue> q = JqmClientFactory.getClient().getQueues();

        // Done
        stopJettyServer();
        CreationTools.stopHsqldbServer();

        // Test
        Assert.assertEquals(9, q.size());
        for (Queue qu : q)
        {
            System.out.println(qu.getName());
        }
    }

    // Enqueue, list all jobs, delete job
    @Test
    public void testEnqueue() throws Exception
    {
        // Create environment
        EntityManager em = CreationTools.emf.createEntityManager();
        CreationTools.reinitHsqldbServer();
        TestHelpers.createLocalNode(em);
        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", null, "jqm-test-jndijms-wmq/",
                "jqm-test-jndijms-wmq/jqm-test-jndijms-wmq.jar", TestHelpers.qVip, 42, "Jms", "Franquin", "ModuleMachin", "other1",
                "other2", "other3", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");

        startJettyServer();

        // Metadata query
        int i = JqmClientFactory.getClient().enqueue(form);

        Assert.assertEquals(1, JqmClientFactory.getClient().getJobs().size());

        JqmClientFactory.getClient().deleteJob(i);

        Assert.assertEquals(0, JqmClientFactory.getClient().getJobs().size());

        // Done
        stopJettyServer();
        CreationTools.stopHsqldbServer();

        // Test

    }

    // enqueue, list, cancel
    @Test
    public void testCancel() throws Exception
    {
        // Create environment
        EntityManager em = CreationTools.emf.createEntityManager();
        CreationTools.reinitHsqldbServer();
        TestHelpers.createLocalNode(em);
        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", null, "jqm-test-jndijms-wmq/",
                "jqm-test-jndijms-wmq/jqm-test-jndijms-wmq.jar", TestHelpers.qVip, 42, "Jms", "Franquin", "ModuleMachin", "other1",
                "other2", "other3", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");

        startJettyServer();

        // Metadata query
        int i = JqmClientFactory.getClient().enqueue(form);
        Assert.assertEquals(1, JqmClientFactory.getClient().getJobs().size());

        JqmClientFactory.getClient().cancelJob(i);
        Assert.assertEquals(1, JqmClientFactory.getClient().getJobs().size());

        // Done
        stopJettyServer();
        CreationTools.stopHsqldbServer();
    }

    // enqueue, list, cancel
    @Test
    public void testKill() throws Exception
    {
        // Create environment
        EntityManager em = CreationTools.emf.createEntityManager();
        CreationTools.reinitHsqldbServer();
        TestHelpers.createLocalNode(em);
        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", null, "jqm-test-jndijms-wmq/",
                "jqm-test-jndijms-wmq/jqm-test-jndijms-wmq.jar", TestHelpers.qVip, 42, "Jms", "Franquin", "ModuleMachin", "other1",
                "other2", "other3", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");

        startJettyServer();

        // Metadata query
        int i = JqmClientFactory.getClient().enqueue(form);
        Assert.assertEquals(1, JqmClientFactory.getClient().getJobs().size());

        JqmClientFactory.getClient().killJob(i);
        Assert.assertEquals(1, JqmClientFactory.getClient().getJobs().size());
        Assert.assertEquals(State.KILLED, JqmClientFactory.getClient().getJobs().get(0).getState());

        // Done
        stopJettyServer();
        CreationTools.stopHsqldbServer();
    }

    @Test
    public void testMiscApi() throws Exception
    {
        // Create environment
        EntityManager em = CreationTools.emf.createEntityManager();
        CreationTools.reinitHsqldbServer();
        TestHelpers.createLocalNode(em);
        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", null, "jqm-test-jndijms-wmq/",
                "jqm-test-jndijms-wmq/jqm-test-jndijms-wmq.jar", TestHelpers.qVip, 42, "Jms", "Franquin", "ModuleMachin", "other1",
                "other2", "other3", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");

        startJettyServer();

        // Metadata query
        int i = JqmClientFactory.getClient().enqueue(form);
        Assert.assertEquals(1, JqmClientFactory.getClient().getJobs().size());
        Assert.assertEquals(1, JqmClientFactory.getClient().getActiveJobs().size());
        Assert.assertEquals(1, JqmClientFactory.getClient().getJobs().size());
        JqmClientFactory.getClient().pauseQueuedJob(i);
        Assert.assertEquals(State.HOLDED, JqmClientFactory.getClient().getJobs().get(0).getState());
        JqmClientFactory.getClient().resumeJob(i);
        Assert.assertEquals(1, JqmClientFactory.getClient().getActiveJobs().size());

        Queue q = JqmClientFactory.getClient().getQueues().get(2);
        JqmClientFactory.getClient().setJobQueue(i, q);
        Assert.assertEquals(q.getId(), JqmClientFactory.getClient().getJobs().get(0).getQueue().getId());

        Assert.assertEquals(1, JqmClientFactory.getClient().getUserActiveJobs("MAG").size());

        // Done
        stopJettyServer();
        CreationTools.stopHsqldbServer();
    }

    @Test
    public void testException() throws Exception
    {
        // Create environment
        EntityManager em = CreationTools.emf.createEntityManager();
        CreationTools.reinitHsqldbServer();
        TestHelpers.createLocalNode(em);
        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", null, "jqm-test-jndijms-wmq/",
                "jqm-test-jndijms-wmq/jqm-test-jndijms-wmq.jar", TestHelpers.qVip, 42, "Jms", "Franquin", "ModuleMachin", "other1",
                "other2", "other3", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");

        startJettyServer();

        // Metadata query
        try
        {
            JqmClientFactory.getClient().enqueueFromHistory(999); // Does not exist!
            Assert.fail("an exception should have been thrown");
        }
        catch (JqmInvalidRequestException e)
        {
            // Good!
        }
        Assert.assertEquals(0, JqmClientFactory.getClient().getActiveJobs().size());

        // Done
        stopJettyServer();
        CreationTools.stopHsqldbServer();
    }
}
