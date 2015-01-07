package pyl;

import com.enioka.jqm.api.JobManager;

public class EngineApiSend3Msg implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        jm.sendMsg("Les marsus sont nos amis, il faut les aimer aussi!");
        jm.sendMsg("Les marsus sont nos amis, il faut les aimer aussi!2");
        jm.sendMsg("Les marsus sont nos amis, il faut les aimer aussi!3");
    }
}
