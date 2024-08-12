package com.enioka.jqm.tests.jpms;

import com.enioka.jqm.api.JobManager;

public class SimpleJpmsPayload implements Runnable
{
    public JobManager jobManager;

    public void run()
    {
        System.out.println("Hello world!");
        System.out.println("JI is " + jobManager.jobInstanceID());
    }
}
