package pyl;

import com.enioka.jqm.api.JobManager;

public class EngineApiDefCon implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        try
        {
            System.out.println(jm.getDefaultConnection().getClass());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("arg", e);
        }
    }
}
