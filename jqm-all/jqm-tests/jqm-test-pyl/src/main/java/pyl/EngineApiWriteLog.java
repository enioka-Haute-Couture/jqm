package pyl;

import com.enioka.jqm.api.JobManager;

public class EngineApiWriteLog
{
    static JobManager jm;

    public static void main(String[] args)
    {
        int i = 0;
        while (true)
        {
            i++;
            System.out.println("This is a text line. This job won't end on its own and must be killed. " + i);
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            jm.yield();
        }
    }
}
