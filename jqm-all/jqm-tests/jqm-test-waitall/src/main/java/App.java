import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.api.JobManager;

public class App implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        String child = jm.parameters().get("child");

        if (child != null)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                // Do nothing. Just a test.
            }
        }
        else
        {
            Map<String, String> p = new HashMap<String, String>();
            p.put("child", "yep");
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.enqueue(jm.applicationName(), jm.userName(), null, jm.sessionID(), jm.application(), jm.module(), jm.keyword1(),
                    jm.keyword2(), jm.keyword3(), p);
            jm.waitChildren();
        }

    }
}
