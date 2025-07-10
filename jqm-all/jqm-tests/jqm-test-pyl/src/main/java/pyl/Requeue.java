package pyl;

import com.enioka.jqm.api.JobManager;

/**
 * For some tests (especially Highlander) it is useful to have a job which simply requeues itself.
 */
public class Requeue implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(),
                jm.keyword1(), jm.keyword2(), jm.keyword3(), null);
    }
}
