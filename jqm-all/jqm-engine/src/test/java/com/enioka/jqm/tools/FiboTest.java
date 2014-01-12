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

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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
import com.enioka.jqm.jpamodel.State;

public class FiboTest
{
    public static Server s;
    public static Logger jqmlogger = Logger.getLogger(FiboTest.class);

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
    }

    @AfterClass
    public static void end()
    {
        s.shutdown();
        s.stop();
    }

    @Test
    public void testFibo() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testFibo");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, "jqm-test-fibo/",
                "jqm-test-fibo/jqm-test-fibo.jar", TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2",
                false, em);

        JobDefinition form = new JobDefinition("Fibo", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");
        Dispatcher.enQueue(form);

        // Create JNDI connection to write inside the engine database
        em.getTransaction().begin();
        CreationTools.createDatabaseProp("jdbc/jqm", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", em);
        em.getTransaction().commit();

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        Thread.sleep(15000);
        engine1.stop();

        long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
        Assert.assertTrue(i > 2);
    }

    @Test
    public void testEnqueueSynchronously() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testEnqueueSynchronously");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, "jqm-test-fibosync/",
                "jqm-test-fibosync/jqm-test-fibosync.jar", TestHelpers.qVip, 42, "FiboSync", null, "Franquin", "ModuleMachin", "other",
                "other", false, em);

        JobDefinition j = new JobDefinition("FiboSync", "MAG");
        j.addParameter("p1", "1");
        j.addParameter("p2", "2");
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        Thread.sleep(25000);

        engine1.stop();
        long ii = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
        Assert.assertTrue(ii > 2);
        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.endDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();
        for (History history : res)
        {
            Assert.assertEquals(State.ENDED, history.getState());
        }
        TestHelpers.printJobInstanceTable();
        Assert.assertEquals(i, (int) res.get(res.size() - 1).getId());
    }
}
