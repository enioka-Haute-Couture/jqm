package com.enioka.jqm.api;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.debug("Cleaning after Hibernate during WS application shutdown");
        JqmClientFactory.getClient().dispose();
    }
}
