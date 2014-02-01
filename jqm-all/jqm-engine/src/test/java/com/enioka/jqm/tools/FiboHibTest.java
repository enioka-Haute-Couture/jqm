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

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.jqm.test.helpers.CreationTools;
import org.jqm.test.helpers.TestHelpers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;

public class FiboHibTest
{
    public static Server s;
    public static Logger jqmlogger = Logger.getLogger(FiboHibTest.class);

    @BeforeClass
    public static void testInit() throws InterruptedException
    {
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();

        JqmClientFactory.resetClient(null);
        Helpers.resetEmf();
        CreationTools.reset();
    }

    @AfterClass
    public static void end()
    {
        s.shutdown();
        s.stop();
    }

    @Test
    public void testFiboHib() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testFiboHib");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, "jqm-test-fibohib/",
                "jqm-test-fibohib/jqm-test-fibohib.jar", TestHelpers.qVip, 42, "FiboHib", null, "Franquin", "ModuleMachin", "other1",
                "other2", false, em);

        JobRequest form = new JobRequest("FiboHib", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");
        JqmClientFactory.getClient().enqueue(form);

        // Create JNDI connection to write inside the engine database
        em.getTransaction().begin();
        em.createQuery("DELETE FROM DatabaseProp");
        CreationTools.createDatabaseProp("jdbc/jqm", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/testdbengine", "SA", "", em);
        em.getTransaction().commit();

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(11, 30000);
        engine1.stop();

        long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
        Assert.assertTrue(i > 2);
    }
}
