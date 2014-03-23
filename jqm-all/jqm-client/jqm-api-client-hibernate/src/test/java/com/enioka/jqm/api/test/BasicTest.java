package com.enioka.jqm.api.test;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.State;

public class BasicTest
{
    private static Logger jqmlogger = Logger.getLogger(BasicTest.class);

    @Test
    public void testChain()
    {
        // No exception allowed!
        JqmClientFactory.getClient().getQueues();
        jqmlogger.info("q1");
        JqmClientFactory.getClient().getQueues();
        jqmlogger.info("q2");
    }

    @Test
    public void testQuery()
    {
        Query q = new Query("toto", null);
        q.setInstanceApplication("marsu");
        q.setInstanceKeyword2("pouet");
        q.setInstanceModule("module");
        q.setParentId(12);
        q.setJobInstanceId(132);
        q.setQueryLiveInstances(true);

        q.setJobDefKeyword2("pouet2");

        JqmClientFactory.getClient().getJobs(q);
    }

    @Test
    public void testQueryDate()
    {
        Query q = new Query("toto", null);
        q.setInstanceApplication("marsu");
        q.setInstanceKeyword2("pouet");
        q.setInstanceModule("module");
        q.setParentId(12);
        q.setJobInstanceId(132);
        q.setQueryLiveInstances(true);

        q.setEnqueuedBefore(Calendar.getInstance());
        q.setEndedAfter(Calendar.getInstance());
        q.setBeganRunningAfter(Calendar.getInstance());
        q.setBeganRunningBefore(Calendar.getInstance());
        q.setEnqueuedAfter(Calendar.getInstance());
        q.setEnqueuedBefore(Calendar.getInstance());

        q.setJobDefKeyword2("pouet2");

        JqmClientFactory.getClient().getJobs(q);
    }

    @Test
    public void testQueryStatusOne()
    {
        Query q = new Query("toto", null);
        q.setQueryLiveInstances(true);
        q.setInstanceApplication("marsu");
        q.addStatusFilter(State.CRASHED);

        JqmClientFactory.getClient().getJobs(q);
    }

    @Test
    public void testQueryStatusTwo()
    {
        Query q = new Query("toto", null);
        q.setQueryLiveInstances(true);
        q.setInstanceApplication("marsu");
        q.addStatusFilter(State.CRASHED);
        q.addStatusFilter(State.HOLDED);

        JqmClientFactory.getClient().getJobs(q);
    }

    @Test
    public void testQueryNull()
    {
        JqmClientFactory.getClient().getJobs(new Query("", null));
    }
}
