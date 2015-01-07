package pyl;

import com.enioka.jqm.api.JobManager;

public class EngineApiProgress
{
    static JobManager jm;

    public static void main(String[] args)
    {
        for (int i = 0; i <= 50; i++)
        {
            if (i == 5 || i == 10 || i == 35 || i == 50)
            {
                jm.sendProgress(i);
                System.out.println("Progress: " + i);
            }
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // Nothing
            }
        }
    }
}
