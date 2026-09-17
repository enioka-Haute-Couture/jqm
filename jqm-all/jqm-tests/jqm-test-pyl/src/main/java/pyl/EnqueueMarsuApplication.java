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
        System.out.println("[EnqueueMarsuApplication] Parent JI " + jm.jobInstanceID() + " will enqueue MarsuApplication");
        long childId = jm.enqueue("MarsuApplication", jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                jm.keyword2(), jm.keyword3(), null);
        System.out.println("[EnqueueMarsuApplication] Parent JI " + jm.jobInstanceID() + " enqueued child MarsuApplication JI " + childId);
    }
}
