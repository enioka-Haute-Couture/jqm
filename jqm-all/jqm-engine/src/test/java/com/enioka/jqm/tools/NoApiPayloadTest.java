/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

package com.enioka.jqm.tools;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;

public class NoApiPayloadTest
{
    public static Logger jqmlogger = Logger.getLogger(NoApiPayloadTest.class);
    public static Server s;

    @BeforeClass
    public static void testInit() throws InterruptedException
    {
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();

        Dispatcher.resetEM();
        Helpers.resetEmf();

        jqmlogger.debug("log init");
    }

    @AfterClass
    public static void stop()
    {
        Dispatcher.resetEM();
        s.shutdown();
        s.stop();
    }

    @Test
    public void testClassicPayload() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testClassicPayload");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "jqm-test-datetimemaven", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("jqm-test-datetimemaven", "MAG");
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        Thread.sleep(5000);
        engine1.stop();

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId order by id asc", History.class)
                .setParameter("myId", jd.getId()).getResultList();

        Assert.assertEquals(1, ji.size());
        Assert.assertEquals("ENDED", ji.get(0).getState());
    }

    @Test
    public void testRunnable() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testRunnable");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-runnable", "jqm-test-runnable/jqm-test-runnable.jar",
                TestHelpers.qVip, 42, "jqm-test-runnable", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("jqm-test-runnable", "MAG");
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        Thread.sleep(5000);
        engine1.stop();

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId order by id asc", History.class)
                .setParameter("myId", jd.getId()).getResultList();

        Assert.assertEquals(1, ji.size());
        Assert.assertEquals("ENDED", ji.get(0).getState());
    }

    @Test
    public void testRunnableInject() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testRunnable");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef("super app", true, "App", jdargs, "jqm-test-runnable-inject",
                "jqm-test-runnable-inject/jqm-test-runnable-inject.jar", TestHelpers.qVip, 42, "jqm-test-runnable-inject", "testapp",
                "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("jqm-test-runnable-inject", "MAG");
        j.setSessionID("123X");
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        Thread.sleep(5000);
        engine1.stop();

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId order by id asc", History.class)
                .setParameter("myId", jd.getId()).getResultList();

        Assert.assertEquals(3, ji.size());
        Assert.assertEquals("ENDED", ji.get(0).getState());
        Assert.assertEquals("ENDED", ji.get(1).getState());

        Assert.assertEquals(4, ji.get(0).getMessages().size()); // 3 auto messages + 1 message per run.
        Assert.assertEquals(100, (int) ji.get(0).getProgress());
    }
}
