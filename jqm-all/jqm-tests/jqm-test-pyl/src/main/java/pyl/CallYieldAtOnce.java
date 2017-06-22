package pyl;

import com.enioka.jqm.api.JobManager;

public class CallYieldAtOnce implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        jm.yield();
    }

}
