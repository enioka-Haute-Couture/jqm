package pyl;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.enioka.jqm.api.JobManager;

public class EngineJmxLeak implements Runnable, EngineJmxLeakMBean
{
    JobManager jm;

    @Override
    public void run()
    {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            ObjectName name = new ObjectName("com.test:type=Node,name=test");
            mbs.registerMBean(this, name);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        int i = 0;
        while (true)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            jm.sendProgress(i);
            i++;
        }
    }

    @Override
    public Integer getOne()
    {
        return 1;
    }
}
