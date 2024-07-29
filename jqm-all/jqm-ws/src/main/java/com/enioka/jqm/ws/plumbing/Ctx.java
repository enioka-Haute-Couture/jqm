package com.enioka.jqm.ws.plumbing;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.enioka.jqm.client.api.JqmClientFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs cleanup code when the web application is stopped.
 */
@WebListener
public class Ctx implements ServletContextListener
{
    static Logger log = LoggerFactory.getLogger(Ctx.class);

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        // nothing for now
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        log.debug("Cleaning connections during WS application shutdown");
        JqmClientFactory.reset();
    }
}
