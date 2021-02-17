package pyl;

import java.util.Random;

import com.enioka.jqm.api.JobManager;

public class WaitRandom implements Runnable
{
    JobManager jm;

    Random rand = new Random();

    @Override
    public void run()
    {
        String delayStr = jm.parameters().get("delay_ms");
        int delay = Integer.parseInt(delayStr);

        try
        {
            Thread.sleep(rand.nextInt(delay));
        }
        catch (InterruptedException e)
        {
            // Who cares its a test
            e.printStackTrace();
        }
    }
}
