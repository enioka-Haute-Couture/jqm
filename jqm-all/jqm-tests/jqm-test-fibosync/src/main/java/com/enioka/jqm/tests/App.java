package com.enioka.jqm.tests;

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.api.JobBase;

public class App extends JobBase
{
    @Override
    public void start()
    {
        System.out.println("PARAMETRE FIBO 2: " + this.getParameters().get("p2"));

        Map<String, String> p = new HashMap<String, String>();
        p.put("p1", this.getParameters().get("p2"));
        p.put("p2", (Integer.parseInt(this.getParameters().get("p1")) + Integer.parseInt(this.getParameters().get("p2")) + ""));

        if (Integer.parseInt(this.getParameters().get("p1")) <= 100)
        {
            int i = enQueueSynchronously("FiboSync", "Luke", null, null, null, null, null, null, null, p);
            System.out.println("Synchronous job finished: " + i);
        }

        System.out.println("FIBO FINISHED");
    }
}
