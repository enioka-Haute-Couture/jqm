package pyl;

import com.enioka.jqm.api.JobManager;

/**
 * Test payload used to enqueue a fixed application from inside a running job.
 */
public class EnqueueMarsuApplication implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        int burst = 1;
        String burstParam = jm.parameters().get("burst");
        if (burstParam != null)
        {
            try
            {
                burst = Integer.parseInt(burstParam);
            }
            catch (NumberFormatException e)
            {
                burst = 1;
            }
        }

        System.out.println(
                "[EnqueueMarsuApplication] Parent JI " + jm.jobInstanceID() + " will enqueue MarsuApplication " + burst + " time(s)");
        for (int i = 0; i < burst; i++)
        {
            long childId = jm.enqueue("MarsuApplication", jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), null);
            System.out.println("[EnqueueMarsuApplication] Parent JI " + jm.jobInstanceID() + " enqueued child MarsuApplication JI "
                    + childId + " (" + (i + 1) + "/" + burst + ")");
        }
    }
}
