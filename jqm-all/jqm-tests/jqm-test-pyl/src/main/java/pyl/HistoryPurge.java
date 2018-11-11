package pyl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.enioka.jqm.api.JobManager;

/**
 * This purge is inefficient as it uses a single transaction to purge everything, and uses sub queries rather than joins as DELETE on joins
 * are database-specific. This is provided as a purge that will work on every database out of the box for small databases. Production purges
 * may be better in native SQL and have multiple commits.<br>
 * This only purges the database. Log files and Deliverable files should be purged through a shell script or equivalent.
 */
public class HistoryPurge implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        // Get parameters
        boolean purgeDeletedNodes = Boolean.parseBoolean(jm.parameters().get("purgeDeletedNodes"));
        boolean purgeDeletedQueues = Boolean.parseBoolean(jm.parameters().get("purgeDeletedQueues"));
        int historyRetentionInDays = Integer.parseInt(jm.parameters().get("historyRetentionInDays"));
        String dataSourceName = jm.parameters().get("purgeDatasourceName");
        String tablePrefix = jm.parameters().get("tablePrefix");
        // int batchSize = Integer.parseInt(jm.parameters().get("batchSize"));

        // Defaults
        if (dataSourceName == null)
        {
            dataSourceName = "jdbc/jqm";
        }
        if (tablePrefix == null)
        {
            tablePrefix = "";
        }

        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, -historyRetentionInDays);

        String wh = " WHERE h.DATE_END < ? ";
        if (purgeDeletedNodes)
        {
            wh += " OR h.NODE IS NULL ";
        }
        if (purgeDeletedQueues)
        {
            wh += " OR h.QUEUE IS NULL ";
        }

        DataSource ds;
        Connection c = null;

        try
        {
            // OPEN CONNECTION
            ds = (DataSource) InitialContext.doLookup(dataSourceName);
            jm.sendProgress(0);
            c = ds.getConnection();
            c.setAutoCommit(false);

            // MESSAGE
            jm.sendProgress(20);
            PreparedStatement s2 = c.prepareStatement(String.format(
                    "DELETE FROM %sMESSAGE o WHERE o.JOB_INSTANCE IN (SELECT h.id FROM %sHISTORY h %s)", tablePrefix, tablePrefix, wh));
            s2.setTimestamp(1, new Timestamp(((Calendar) limit).getTimeInMillis()));
            int res = s2.executeUpdate();
            // Commit to avoid deadlock... as we write a message next line!
            c.commit();
            jm.sendMsg(String.format("%s messages purged", res));

            // DELIVERABLE
            jm.sendProgress(5);
            PreparedStatement s1 = c.prepareStatement(String.format(
                    "DELETE FROM %sDELIVERABLE o WHERE o.JOB_INSTANCE IN (SELECT h.id FROM %sHISTORY h %s)", tablePrefix, tablePrefix, wh));
            s1.setTimestamp(1, new Timestamp(((Calendar) limit).getTimeInMillis()));
            res = s1.executeUpdate();
            jm.sendMsg(String.format("%s deliverables purged", res));

            // PARAMETERS
            jm.sendProgress(40);
            PreparedStatement s3 = c.prepareStatement(
                    String.format("DELETE FROM %sJOB_INSTANCE_PARAMETER o WHERE o.JOB_INSTANCE IN (SELECT h.id FROM %sHISTORY h %s)",
                            tablePrefix, tablePrefix, wh));
            s3.setTimestamp(1, new Timestamp(((Calendar) limit).getTimeInMillis()));
            res = s3.executeUpdate();
            jm.sendMsg(String.format("%s parameters purged", res));

            // HISTORY ITSELF
            jm.sendProgress(60);
            PreparedStatement s4 = c.prepareStatement(String.format("DELETE FROM %sHISTORY h %s", tablePrefix, wh));
            s4.setTimestamp(1, new Timestamp(((Calendar) limit).getTimeInMillis()));
            res = s4.executeUpdate();
            jm.sendMsg(String.format("%s lines purged from history table", res));

            // COMMIT
            jm.sendProgress(95);
            c.commit();
            jm.sendMsg("Purge was committed to database");
            jm.sendProgress(100);
        }
        catch (Exception e)
        {
            if (c != null)
            {
                try
                {
                    c.rollback();
                }
                catch (SQLException e2)
                {
                    // Do nothing - connection is closed in the end anyway.
                }
            }
            throw new RuntimeException("Purge has failed", e);
        }
        finally
        {
            try
            {
                c.close();
            }
            catch (Exception e)
            {
                // Nothing
            }
        }
    }
}
