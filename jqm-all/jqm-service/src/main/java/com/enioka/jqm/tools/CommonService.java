package com.enioka.jqm.tools;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

final class CommonService
{
    private static final Logger jqmlogger = Logger.getLogger(CommonService.class);

    static void setLogFileName(String name)
    {
        Appender a = Logger.getRootLogger().getAppender("rollingfile");
        if (a == null)
        {
            return;
        }
        RollingFileAppender r = (RollingFileAppender) a;
        r.setFile("./logs/jqm-" + name + ".log");
        r.activateOptions();
    }

    static void setLogLevel(String level)
    {
        try
        {
            Logger.getRootLogger().setLevel(Level.toLevel(level));
            Logger.getLogger("com.enioka").setLevel(Level.toLevel(level));
            jqmlogger.info("Setting general log level at " + level + " which translates as log4j level " + Level.toLevel(level));
        }
        catch (Exception e)
        {
            jqmlogger.warn("Log level could not be set", e);
        }
    }
}
