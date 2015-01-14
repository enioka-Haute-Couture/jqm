/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.jqm.api.test;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.Query.Sort;
import com.enioka.jqm.api.State;

/**
 * Simple tests for checking JPA query syntax (no data)
 */
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
    public void testFluentQuery()
    {
        Query q = new Query("toto", null);
        q.setQueryLiveInstances(true);
        q.setInstanceApplication("marsu");
        q.addStatusFilter(State.CRASHED);
        q.addStatusFilter(State.HOLDED);

        JqmClientFactory.getClient().getJobs(Query.create().addStatusFilter(State.RUNNING).setApplicationName("MARSU"));
    }

    @Test
    public void testQueryPercent()
    {
        JqmClientFactory.getClient().getJobs(Query.create().setApplicationName("%TEST"));
    }

    @Test
    public void testQueryNull()
    {
        JqmClientFactory.getClient().getJobs(new Query("", null));
    }

    @Test
    public void testQueueNameId()
    {
        Query.create().setQueueName("test").run();
        Query.create().setQueueId(12).run();
    }

    @Test
    public void testPaginationWithFilter()
    {
        Query.create().setQueueName("test").setPageSize(10).run();
        Query.create().setQueueId(12).setPageSize(10).run();
    }

    @Test
    public void testUsername()
    {
        Query.create().setUser("test").setPageSize(10).run();
    }

    @Test
    public void testSortHistory()
    {
        Query.create().setUser("test").setPageSize(10).addSortAsc(Sort.APPLICATIONNAME).addSortDesc(Sort.DATEATTRIBUTION)
                .addSortAsc(Sort.DATEEND).addSortDesc(Sort.DATEENQUEUE).addSortAsc(Sort.ID).addSortDesc(Sort.QUEUENAME)
                .addSortAsc(Sort.STATUS).addSortDesc(Sort.USERNAME).addSortAsc(Sort.PARENTID).run();
    }

    @Test
    public void testSortJi()
    {
        Query.create().setQueryHistoryInstances(false).setQueryLiveInstances(true).setUser("test").addSortAsc(Sort.APPLICATIONNAME)
                .addSortDesc(Sort.DATEATTRIBUTION).addSortAsc(Sort.DATEEND).addSortDesc(Sort.DATEENQUEUE).addSortAsc(Sort.ID)
                .addSortDesc(Sort.QUEUENAME).addSortAsc(Sort.STATUS).addSortDesc(Sort.USERNAME).addSortAsc(Sort.PARENTID).run();
    }

    @Test
    public void testOnlyQueue()
    {
        Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false).setUser("test").run();
    }

    @Test
    public void testBug159()
    {
        Query.create().setJobInstanceId(1234).setQueryLiveInstances(true).setQueryHistoryInstances(false).setPageSize(15).setFirstRow(0)
                .run();
    }
}
