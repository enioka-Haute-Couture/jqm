package com.enioka.jqm.api.test;

import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.jqm.test.helpers.CreationTools;
import org.jqm.test.helpers.TestHelpers;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Queue;
import com.enioka.jqm.api.State;

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
        server = new Server(0);
        server.setSendServerVersion(true);
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(100);
        server.setThreadPool(threadPool);

        // Load web application.
        WebAppContext webAppContext = new WebAppContext("../../jqm-ws/target/jqm-ws.war", "/marsu");
        webAppContext.setDisplayName("JettyContextForWsTest");

        // Hide server classes from the web app
        String[] defExcl = webAppContext.getDefaultServerClasses();
        String[] exclusions = new String[defExcl.length + 2];
        for (int i = 2; i <= defExcl.length; i++)
        {
            exclusions[i] = defExcl[i - 2];
        }
        exclusions[0] = "com.enioka.";
        // exclusions[1] = "org.hibernate.";
        webAppContext.setServerClasses(exclusions);

        // Set configurations (order is important: you want to unpack war before reading web.xml)
        webAppContext.setConfigurations(new Configuration[] { new WebInfConfiguration(), new WebXmlConfiguration(),
                new MetaInfConfiguration(), new FragmentConfiguration(), new EnvConfiguration(), new PlusConfiguration(),
                new AnnotationConfiguration(), new TagLibConfiguration() });

        server.setHandler(webAppContext);

        server.start();

        Properties p = new Properties();
        System.out.println("jetty on " + getServerPort());
        p.put("com.enioka.ws.url", "http://localhost:" + getServerPort() + "/marsu/ws");
        JqmClientFactory.setProperties(p);
    }

    public static void stopJettyServer()
    {
        try
        {
            server.stop();
            server.join();
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
}
