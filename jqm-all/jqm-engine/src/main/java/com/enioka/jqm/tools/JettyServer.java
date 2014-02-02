package com.enioka.jqm.tools;

import java.net.BindException;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.enioka.jqm.jpamodel.Node;

class JettyServer
{
    private static Logger jqmlogger = Logger.getLogger(JettyServer.class);

    private Server server = null;

    void start(Node node)
    {
        server = new Server(new InetSocketAddress(node.getDns(), node.getPort()));

        // Servlets (basic script API + files)
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new ServletFile()), "/getfile");
        context.addServlet(new ServletHolder(new ServletEnqueue()), "/enqueue");
        context.addServlet(new ServletHolder(new ServletStatus()), "/status");

        jqmlogger.info("Starting Jetty (port " + node.getPort() + ")");
        try
        {
            server.start();
        }
        catch (BindException e)
        {
            // JETTY-839: threadpool not daemon nor close on exception. Explicit closing required as workaround.
            this.stop();
            throw new JqmInitError("Could not start web server - check there is no other process binding on this port & interface", e);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not start web server - not a port issue, but a generic one", e);
        }
        jqmlogger.info("Jetty has started on port " + getActualPort());
    }

    int getActualPort()
    {
        return server.getConnectors()[0].getLocalPort();
    }

    void stop()
    {
        jqmlogger.debug("Jetty will be stopped");
        try
        {
            this.server.stop();
            this.server.join();
        }
        catch (Exception e)
        {
            // Who cares, we are dying.
        }
    }

}
