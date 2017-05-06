package pyl;

import com.enioka.jqm.api.JobManager;

public class EngineApiStaticInjection implements Runnable
{
    private static JobManager jm;

    @Override
    public void run()
    {
        // Nothing to do.
        jm.application();
    }

}
