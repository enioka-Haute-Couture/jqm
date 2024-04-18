package com.enioka.jqm.engine;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.ResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Highlander is when only a single instance of the same job definition can run at the same time on all queues.<br>
 * The resource is therefore defined here as "a single slot per job definition".<br>
 * <br>
 * This RM does not actually change anything in database - it simply holds a lock during job analysis. To hold this lock it must open a
 * connection of its own - that way locks can be granular. If it used the poller connection instead, locks would only be released when it is
 * committed as it is the only way to release them, and that could cause deadlocks.
 */
public class HighlanderResourceManager extends ResourceManagerBase
{
    private static Logger jqmlogger = LoggerFactory.getLogger(HighlanderResourceManager.class);

    private Map<JobInstance, DbConn> openLocks = new HashMap<JobInstance, DbConn>();

    HighlanderResourceManager(ResourceManager rm)
    {
        super(rm);
    }

    @Override
    void refreshConfiguration(ResourceManager configuration)
    {
        super.refreshConfiguration(configuration);
        jqmlogger.info("\tConfigured Highlander resource Manager");
    }

    @Override
    BookingStatus bookResource(JobInstance ji, DbConn cnx)
    {
        if (!ji.getJD().isHighlander())
        {
            // Non-highlander JI do not need anything from this RM.
            return BookingStatus.BOOKED;
        }
        cnx = Helpers.getNewDbSession();

        // Lock the definition in the DB - this is a convention for highlander JI between clients and engine.
        jqmlogger.trace("Locking JI ID {} of rank {} - {}", ji.getId(), ji.getInternalPosition(), ji.getJD().getApplicationName());
        ResultSet rs = cnx.runSelect(true, "jd_select_by_id_lock", ji.getJD().getId());

        // We have the lock. Check if there are already running instances.
        Integer runningCount = cnx.runSelectSingle("ji_select_existing_highlander_2", Integer.class, ji.getJD().getId());
        if (runningCount != 0)
        {
            // Already running, so skip this JI.
            cnx.closeQuietly(rs); // release the lock.
            cnx.rollback();
            cnx.close();
            jqmlogger.trace("Resource reservation KO for JI {} - {} - one instance is already running", ji.getId(),
                    ji.getJD().getApplicationName());
            return BookingStatus.FAILED;
        }

        // If here, no running instance and lock is held. It will only be released when commit or rollback is called on the connection.
        jqmlogger.trace("Resourced reserved for JI {} - {}", ji.getId(), ji.getJD().getApplicationName());
        openLocks.put(ji, cnx);
        return BookingStatus.BOOKED;
    }

    @Override
    void rollbackResourceBooking(JobInstance ji, DbConn cnx)
    {
        cnx = openLocks.remove(ji);
        if (cnx != null)
        {
            jqmlogger.trace("Rollbacking resource reservation for JI {} on app {}", ji.getId(), ji.getJD().getApplicationName());
            cnx.rollback();
            cnx.close();
        }
    }

    @Override
    void commitResourceBooking(JobInstance ji, DbConn cnx)
    {
        cnx = openLocks.remove(ji);
        if (cnx != null)
        {
            jqmlogger.trace("Committing resource reservation for JI {} on app {}", ji.getId(), ji.getJD().getApplicationName());
            cnx.commit();
            cnx.close();
        }
    }
}
