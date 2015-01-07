package pyl;

import java.util.Calendar;

import com.enioka.jqm.api.JobManager;

public class EngineApiSendMsg implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        jm.sendMsg("DateTime is " + Calendar.getInstance());
    }

}
