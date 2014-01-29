package com.enioka.jqm.api.test;

import java.util.List;
import java.util.Properties;

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
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;

public class BasicTest
{
    public static Server server;

    public static void startServer() throws Exception
    {
        if (server != null)
        {
            stopServer();
        }
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
        String[] exclusions = new String[defExcl.length + 1];
        for (int i = 1; i <= defExcl.length; i++)
        {
            exclusions[i] = defExcl[i - 1];
        }
        exclusions[0] = "com.enioka.";
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

    public static void stopServer()
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
        startServer();
        stopServer();
    }

    @Test
    public void testList() throws Exception
    {
        startServer();
        List<JobInstance> res = JqmClientFactory.getClient().getJobs();
        stopServer();

        Assert.assertEquals(0, res.size());
    }
}
