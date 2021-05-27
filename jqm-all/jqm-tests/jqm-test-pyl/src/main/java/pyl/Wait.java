package pyl;

import com.enioka.jqm.api.JobManager;

public class Wait implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        String delayStr = jm.parameters().get("delay_ms");
        int delay = Integer.parseInt(delayStr);

        try
        {
            Thread.sleep(delay);
        }
        catch (InterruptedException e)
        {
            // Who cares its a test
            e.printStackTrace();
        }
    }
}
