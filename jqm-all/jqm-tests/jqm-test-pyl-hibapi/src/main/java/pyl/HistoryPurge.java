package pyl;

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.api.JobManager;

/**
 * This purge is very inefficient as it uses the ORM to access the database. This is provided as a purge that will work on every database
 * out of the box for small databases. Production purges may be better in native SQL and have multiple commits.<br>
 * This only purges the database. Log files and Deliverable files should be purged through a shell script or equivalent.
 */
public class HistoryPurge implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        boolean purgeDeletedNodes = Boolean.parseBoolean(jm.parameters().get("purgeDeletedNodes"));
        boolean purgeDeletedQueues = Boolean.parseBoolean(jm.parameters().get("purgeDeletedQueues"));
        int historyRetentionInDays = Integer.parseInt(jm.parameters().get("historyRetentionInDays"));
        // int batchSize = Integer.parseInt(jm.parameters().get("batchSize"));

        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, -historyRetentionInDays);

        String wh = " WHERE h.endDate < :l ";
        if (purgeDeletedNodes)
        {
            wh += " OR h.node IS NULL ";
        }
        if (purgeDeletedQueues)
        {
            wh += " OR h.queue IS NULL ";
        }

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try
        {
            emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
            em = emf.createEntityManager();

            em.getTransaction().begin();
            jm.sendProgress(0);
            em.createQuery("DELETE Deliverable o WHERE o.jobId IN (SELECT h.id FROM History h " + wh + ")").setParameter("l", limit)
                    .executeUpdate();
            jm.sendProgress(10);
            em.createQuery("DELETE Message o WHERE o.ji IN (SELECT h.id FROM History h " + wh + ")").setParameter("l", limit)
                    .executeUpdate();
            jm.sendProgress(30);
            em.createQuery("DELETE RuntimeParameter o WHERE o.ji IN (SELECT h.id FROM History h " + wh + ")").setParameter("l", limit)
                    .executeUpdate();
            jm.sendProgress(60);
            int i = em.createQuery("DELETE History h  " + wh).setParameter("l", limit).executeUpdate();
            jm.sendProgress(98);
            em.getTransaction().commit();
            jm.sendProgress(100);
            jm.sendMsg(i + " lines were purged");
        }
        catch (Exception e)
        {
            throw new RuntimeException("purge has failed", e);
        }
        finally
        {
            try
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
            }
            catch (Exception e)
            {
                // Nothing
            }
            try
            {
                em.close();
            }
            catch (Exception e)
            {
                // Nothing
            }
            try
            {
                emf.close();
            }
            catch (Exception e)
            {
                // Nothing
            }
        }
    }
}
