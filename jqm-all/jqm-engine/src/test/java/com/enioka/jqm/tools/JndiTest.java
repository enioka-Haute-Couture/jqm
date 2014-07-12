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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JndiTest extends JqmBaseTest
{
    // @Test
    // NOT AN AUTO TEST: this requires to have MQ Series jars which are not libre software!
    public void testJmsWmq() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("starting test testJmsWmq");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", jdargs,
                "jqm-tests/jqm-test-jndijms-wmq/target/test.jar", TestHelpers.qVip, 42, "Jms", "Franquin", "ModuleMachin", "other1",
                "other2", "other3", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");
        JqmClientFactory.getClient().enqueue(form);

        // Create JMS JNDI references for use by the test jar
        em.getTransaction().begin();
        em.persist(CreationTools.createJndiQueueMQSeries(em, "jms/testqueue", "test Queue", "Q.GEO.OUT", null));
        em.persist(CreationTools.createJndiQcfMQSeries(em, "jms/qcf", "test QCF", "10.0.1.90", "QM.TEC1", 1414, "WASCHANNEL"));
        em.getTransaction().commit();

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
        Assert.assertTrue(i == 1);

        History h = null;
        try
        {
            h = (History) em.createQuery("SELECT h FROM History h").getSingleResult();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("History object was not created");
            throw e;
        }

        Assert.assertEquals(State.ENDED, h.getStatus()); // Exception in jar => CRASHED
    }

    @Test
    public void testJmsAmq() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting testJmqAmq");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", jdargs,
                "jqm-tests/jqm-test-jndijms-amq/target/test.jar", TestHelpers.qVip, 42, "Jms", null, "Franquin", "ModuleMachin", "other1",
                "other2", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        JqmClientFactory.getClient().enqueue(form);

        // Create JMS JNDI references for use by the test jar
        CreationTools.createJndiQueueActiveMQ(em, "jms/testqueue", "test queue", "Q.TEST", null);
        CreationTools.createJndiQcfActiveMQ(em, "jms/qcf", "test QCF", "vm:broker:(tcp://localhost:1234)?persistent=false&useJmx=false",
                null);

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        History h = null;
        try
        {
            h = (History) em.createQuery("SELECT h FROM History h").getSingleResult();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("History object was not created");
            throw e;
        }

        Assert.assertEquals(State.ENDED, h.getStatus()); // Exception in jar => CRASHED
    }

    @Test
    public void testJmsAmqWrongAlias() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting testJmsAmqWrongAlias");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", jdargs,
                "jqm-tests/jqm-test-jndijms-amq/target/test.jar", TestHelpers.qVip, 42, "Jms", null, "Franquin", "ModuleMachin", "other1",
                "other2", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        JqmClientFactory.getClient().enqueue(form);

        // Create JMS JNDI references for use by the test jar
        CreationTools.createJndiQueueActiveMQ(em, "jms/testqueue", "test queue", "Q.TEST", null);
        CreationTools.createJndiQcfActiveMQ(em, "jms/qcf2", "test QCF", "vm:broker:(tcp://localhost:1234)?persistent=false&useJmx=false",
                null);

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        History h = null;
        try
        {
            h = (History) em.createQuery("SELECT h FROM History h").getSingleResult();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("History object was not created");
            throw e;
        }

        Assert.assertEquals(State.CRASHED, h.getStatus()); // Exception in jar => CRASHED
    }

    @Test
    public void testDefCon() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting testDefCon");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-defcon/target/test.jar", TestHelpers.qVip,
                42, "Jms", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        JqmClientFactory.getClient().enqueue(form);

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        History h = null;
        try
        {
            h = (History) Helpers.getNewEm().createQuery("SELECT h FROM History h").getSingleResult();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("History object was not created");
            throw e;
        }

        Assert.assertEquals(State.ENDED, h.getStatus()); // Exception in jar => CRASHED
    }

    @Test
    public void testFileJndi() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting testFileJndi");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        // Create JMS JNDI references for use by the test jar
        String path = "./testdir";
        CreationTools.createJndiFile(em, "fs/testdirectory", "test directory", path);

        // Create the directory...
        (new File(path)).mkdir();

        // Test
        try
        {
            File f = (File) NamingManager.getInitialContext(null).lookup("fs/testdirectory");
            Assert.assertTrue(f.isDirectory());
            f.delete();
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testUrlJndi() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting testUrlJndi");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        // Create JMS JNDI references for use by the test jar
        String url = "http://www.marsupilami.com";
        CreationTools.createJndiUrl(em, "url/testurl", "test directory", url);

        try
        {
            URL f = (URL) NamingManager.getInitialContext(null).lookup("url/testurl");
            Assert.assertEquals(url, f.toString());
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testJndiFile() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting testJndiFile");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", jdargs,
                "jqm-tests/jqm-test-jndifile/target/test.jar", TestHelpers.qVip, 42, "Jms", null, "Franquin", "ModuleMachin", "other1",
                "other2", false, em);

        JobRequest form = new JobRequest("Jms", "MAG");
        JqmClientFactory.getClient().enqueue(form);

        // Create JMS JNDI references for use by the test jar
        CreationTools.createJndiFile(em, "fs/test", "test resource", "/tmp");

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);
        engine1.stop();

        History h = null;
        try
        {
            h = (History) em.createQuery("SELECT h FROM History h").getSingleResult();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail("History object was not created");
            throw e;
        }

        Assert.assertEquals(State.ENDED, h.getStatus()); // Exception in jar => CRASHED
    }
}
