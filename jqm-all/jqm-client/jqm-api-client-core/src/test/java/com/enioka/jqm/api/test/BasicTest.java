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
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.BeforeClass;

import static org.ops4j.pax.exam.CoreOptions.*;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.Query.Sort;
import com.enioka.jqm.api.State;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Instruction;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Queue;

/**
 * Simple tests for checking query syntax (no data)
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class BasicTest
{
    private static Logger jqmlogger = Logger.getLogger(BasicTest.class);

    @Configuration
    public Option[] config()
    {
        return options(
            mavenBundle("org.osgi", "org.osgi.service.cm", "1.6.0"),
            wrappedBundle(mavenBundle("commons-codec", "commons-codec", "1.15")),
            wrappedBundle(mavenBundle("org.apache.httpcomponents", "httpcore", "4.4.11")),
            mavenBundle("org.apache.httpcomponents", "httpcore-osgi", "4.4.11"),
            wrappedBundle(mavenBundle("org.apache.httpcomponents", "httpmime", "4.5.7")),
            wrappedBundle(mavenBundle("org.apache.httpcomponents", "httpclient-cache", "4.5.7")),
            wrappedBundle(mavenBundle("org.apache.httpcomponents", "fluent-hc", "4.5.7")),
            wrappedBundle(mavenBundle("org.apache.httpcomponents", "httpclient", "4.5.7")),
            mavenBundle("org.apache.httpcomponents", "httpclient-osgi", "4.5.7"),
            wrappedBundle(mavenBundle("org.hsqldb", "hsqldb", "2.3.4")),
            wrappedBundle(mavenBundle("javax.servlet", "servlet-api", "2.5")),
            wrappedBundle(mavenBundle("org.apache.shiro", "shiro-core", "1.3.2")),
            wrappedBundle(mavenBundle("org.apache.shiro", "shiro-web", "1.3.2")),
            wrappedBundle(mavenBundle("javax.activation", "activation", "1.1.1")),
            mavenBundle("javax.xml.stream", "stax-api", "1.0-2"),
            mavenBundle("javax.xml.bind", "jaxb-api", "2.3.1"),
            mavenBundle("com.enioka.jqm", "jqm-loader", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-api-client-core", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-model", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-impl-hsql", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-impl-pg", "3.0.0-SNAPSHOT"),
            junitBundles()
            );
    }

    @BeforeClass
    public static void init()
    {
        systemProperty("org.ops4j.pax.url.mvn.repositories").value("https://repo1.maven.org/maven2@id=central");
        systemProperty("org.ops4j.pax.url.mvn.useFallbackRepositories").value("false");
    }

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
                .addSortDesc(Sort.DATEATTRIBUTION).addSortDesc(Sort.DATEENQUEUE).addSortAsc(Sort.ID).addSortDesc(Sort.QUEUENAME)
                .addSortAsc(Sort.STATUS).addSortDesc(Sort.USERNAME).addSortAsc(Sort.PARENTID).run();
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

    @Test
    public void testBug292()
    {
        Query.create().addSortDesc(Query.Sort.ID).setQueueName("QBATCH").setQueryHistoryInstances(true).setQueryLiveInstances(true).run();
    }

    @Test
    public void testBug305()
    {
        Properties p = new Properties();
        p.putAll(Db.loadProperties());
        Db db = new Db(p);
        try (DbConn cnx = db.getConn())
        {
            int qId = Queue.create(cnx, "q1", "q1 description", true);
            int jobDefdId = JobDef.create(cnx, "test description", "class", null, "jar", qId, 1, "appName", null, null, null, null, null,
                    false, null, PathType.FS);

            JobInstance.enqueue(cnx, com.enioka.jqm.model.State.RUNNING, qId, jobDefdId, null, null, null, null, null, null, null, null,
                    null, false, false, null, 1, Instruction.RUN, new HashMap<>());
            JobInstance.enqueue(cnx, com.enioka.jqm.model.State.RUNNING, qId, jobDefdId, null, null, null, null, null, null, null, null,
                    null, false, false, null, 1, Instruction.RUN, new HashMap<>());
            cnx.commit();

            Properties p2 = new Properties();
            p2.put("com.enioka.jqm.jdbc.contextobject", db);
            List<com.enioka.jqm.api.JobInstance> res = JqmClientFactory.getClient("test", p2, false)
                    .getJobs(Query.create().setQueryHistoryInstances(false).setQueryLiveInstances(true).addSortDesc(Query.Sort.ID)
                            .setPageSize(1).setApplicationName("appName"));

            Assert.assertEquals(1, res.size());
        }
    }
}