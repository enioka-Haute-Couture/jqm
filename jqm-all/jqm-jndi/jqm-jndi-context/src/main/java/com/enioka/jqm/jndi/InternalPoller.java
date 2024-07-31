package com.enioka.jqm.jndi;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.shared.threads.BaseSimplePoller;

public class InternalPoller extends BaseSimplePoller
{
    private static Logger jqmlogger = LoggerFactory.getLogger(InternalPoller.class);

    private Calendar lastJndiPurge = Calendar.getInstance();
    private final JndiContext jndiContext;

    public InternalPoller(JndiContext jndiContext)
    {
        this.jndiContext = jndiContext;
    }

    @Override
    protected void pollingLoopWork()
    {
        try (var cnx = DbManager.getDb().getConn())
        {
            // Should JNDI cache be purged?
            Calendar bflkpm = Calendar.getInstance();
            int i = cnx.runSelectSingle("jndi_select_count_changed", Integer.class, lastJndiPurge, lastJndiPurge);
            if (i > 0L)
            {
                try
                {
                    jndiContext.resetSingletons();
                    lastJndiPurge = bflkpm;
                }
                catch (Exception e)
                {
                    jqmlogger.warn("Could not reset JNDI singleton resources. New parameters won't be used. Restart engine to update them.",
                            e);
                }
            }
        }
    }

    @Override
    protected long getPeriod()
    {
        try (var cnx = DbManager.getDb().getConn())
        {
            return Long.parseLong(GlobalParameter.getParameter(cnx, "internalPollingPeriodMs", "60000"));
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not retrieve poller period: " + e.getMessage());
            throw new RuntimeException("Could not retrieve poller period", e);
        }
    }
}
