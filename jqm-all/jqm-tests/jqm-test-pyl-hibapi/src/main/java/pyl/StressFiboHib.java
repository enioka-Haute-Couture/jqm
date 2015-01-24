package pyl;

import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.api.JobRequest;

public class StressFiboHib implements Runnable
{
    JobManager jm;

    @Override
    public void run()
    {
        System.out.println("PARAMETRE FIBO 2: " + jm.parameters().get("p2"));

        if (Integer.parseInt(jm.parameters().get("p1")) <= 100)
        {
            System.out.println("BEFORE ENQUEUE");
            JobRequest.create(jm.applicationName(), jm.userName()).addParameter("p1", jm.parameters().get("p2"))
                    .addParameter("p2", "" + (Integer.parseInt(jm.parameters().get("p2")) + Integer.parseInt(jm.parameters().get("p1"))))
                    .submit();
        }
        System.out.println("QUIT FIBO");
    }
}