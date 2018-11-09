package com.enioka.jqm.tools;

import java.sql.ResultSet;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.ResourceManager;

/**
 * Highlander is when only a single instance of the same job definition can run at the same time on all queues.<br>
 * The resource is therefore defined here as "a single slot per job definition".<br>
 * <br>
 * This RM does not actually change anything in database - it simply holds a lock during job analysis.
 */
class HighlanderResourceManager extends ResourceManagerBase
{
    HighlanderResourceManager(ResourceManager rm)
    {
        super(rm);
    }

    @Override
    BookingStatus bookResource(JobInstance ji, DbConn cnx)
    {
        if (!ji.getJD().isHighlander())
        {
            // Non-highlander JI do not need anything from this RM.
            return BookingStatus.BOOKED;
        }

        // Lock the definition in the DB - this is a convention for highlander JI between clients and engine.
        ResultSet rs = cnx.runSelect(true, "jd_select_by_id", ji.getJD().getId());

        // We have the lock. Check if there are already running instances.
        Integer runningCount = cnx.runSelectSingle("ji_select_existing_highlander_2", Integer.class, ji.getJD().getId());
        if (runningCount != 0)
        {
            // Already running, so skip this JI.
            cnx.closeQuietly(rs); // release the lock.
            return BookingStatus.FAILED;
        }

        // If here, no running instance and lock is held (release by commit done in the QP, rs is closed with connection).
        return BookingStatus.BOOKED;
    }
}
