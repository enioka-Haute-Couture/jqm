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
import java.lang.management.ManagementFactory;
import java.net.URL;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.spi.NamingManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JndiTest extends JqmBaseTest
{
    // @Test
    // NOT AN AUTO TEST: this requires to have MQ Series jars which are not libre software!
    public void testJmsWmq() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", null,
                "jqm-tests/jqm-test-jndijms-wmq/target/test.jar", TestHelpers.qVip, 42, "Jms", "Franquin", "ModuleMachin", "other1",
                "other2", "other3", false, em);
        JobRequest.create("Jms", "TestUser").addParameter("p1", "1").addParameter("p2", "2").submit();

        // Create JMS JNDI references for use by the test jar
        CreationTools.createJndiQueueMQSeries(em, "jms/testqueue", "test Queue", "Q.GEO.OUT", null);
        CreationTools.createJndiQcfMQSeries(em, "jms/qcf", "test QCF", "10.0.1.90", "QM.TEC1", 1414, "WASCHANNEL");

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em)); // Exception in jar => CRASHED
    }

    @Test
    public void testJmsAmq() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", null,
                "jqm-tests/jqm-test-jndijms-amq/target/test.jar", TestHelpers.qVip, 42, "Jms", null, "Franquin", "ModuleMachin", "other1",
                "other2", false, em);
        JobRequest.create("Jms", "TestUser").submit();

        // Create JMS JNDI references for use by the test jar
        CreationTools.createJndiQueueActiveMQ(em, "jms/testqueue", "test queue", "Q.TEST", null);
        CreationTools.createJndiQcfActiveMQ(em, "jms/qcf", "test QCF", "vm:broker:(tcp://localhost:1234)?persistent=false&useJmx=false",
                null);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em)); // Exception in jar => CRASHED
    }

    @Test
    public void testJmsAmqWrongAlias() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.testpackages.SuperTestPayload", null,
                "jqm-tests/jqm-test-jndijms-amq/target/test.jar", TestHelpers.qVip, 42, "Jms", null, "Franquin", "ModuleMachin", "other1",
                "other2", false, em);
        JobRequest.create("Jms", "TestUser").submit();

        // Create JMS JNDI references for use by the test jar
        CreationTools.createJndiQueueActiveMQ(em, "jms/testqueue", "test queue", "Q.TEST", null);
        CreationTools.createJndiQcfActiveMQ(em, "jms/qcf2", "test QCF", "vm:broker:(tcp://localhost:1234)?persistent=false&useJmx=false",
                null);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

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
        JqmSimpleTest.create(em, "pyl.EngineApiDefCon").run(this);
    }

    @Test
    public void testFileJndi() throws Exception
    {
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
        // Create JMS JNDI references for use by the test jar
        CreationTools.createJndiFile(em, "fs/test", "test resource", "/tmp");

        JqmSimpleTest.create(em, "pyl.JndiFile", "jqm-test-pyl-nodep").run(this);
    }

    @Test
    public void testJndiServerName() throws Exception
    {
        addAndStartEngine();

        String s = (String) InitialContext.doLookup("serverName");
        Assert.assertEquals("localhost", s);
    }

    @Test
    public void testJndiJdbcPool() throws Exception
    {
        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        em.getTransaction().begin();
        em.createQuery("UPDATE JndiObjectResourceParameter p SET value='true' WHERE key='jmxEnabled'").executeUpdate();
        em.getTransaction().commit();

        addAndStartEngine();

        CreationTools.createJobDef(null, true, "pyl.JndiDb", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip, 42,
                "TestApp", null, "Test", "ModuleTest", "other", "other", false, em);
        JobRequest.create("TestApp", "TestUser").submit();
        TestHelpers.waitFor(1, 5000, em);

        TestHelpers.testOkCount(1, em);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        int nb = (Integer) mbs.getAttribute(new ObjectName("com.enioka.jqm:type=JdbcPool,name=jdbc/test"), "Active");
        Assert.assertEquals(0, nb);
    }

    @Test
    public void testJndiJdbcPoolLeakWithoutHunter() throws Exception
    {
        // Sanity check - our test DOES leak connections
        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", em,
                "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        em.getTransaction().begin();
        em.createQuery("UPDATE JndiObjectResourceParameter p SET value='true' WHERE key='jmxEnabled'").executeUpdate();
        em.getTransaction().commit();

        addAndStartEngine();

        // Run the payload, it should leave an open conn at the end
        CreationTools.createJobDef(null, true, "pyl.JndiDbLeak", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip,
                42, "TestApp", null, "Test", "ModuleTest", "other", "other", false, em);
        JobRequest.create("TestApp", "TestUser").submit();
        TestHelpers.waitFor(1, 5000, em);

        TestHelpers.testOkCount(1, em);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        int nb = (Integer) mbs.getAttribute(new ObjectName("com.enioka.jqm:type=JdbcPool,name=jdbc/test"), "Active");
        Assert.assertEquals(1, nb);

        // Clean the pool forcefully so as not to impact other tests
        JndiContext.createJndiContext().resetSingletons();
    }

    @Test
    public void testJndiJdbcPoolLeakWithHunter() throws Exception
    {
        // Create a connection with our custom interceptor
        JndiObjectResource j = CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA",
                "", em, "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", null);
        em.getTransaction().begin();
        JndiObjectResourceParameter p = new JndiObjectResourceParameter();
        p.setKey("jdbcInterceptors");
        p.setValue("com.enioka.jqm.providers.PayloadInterceptor");
        p.setResource(j);
        em.persist(p);
        em.createQuery("UPDATE JndiObjectResourceParameter p SET value='true' WHERE key='jmxEnabled'").executeUpdate();
        em.getTransaction().commit();

        addAndStartEngine();

        // Sanity check: the leak hunter does not harm normal payloads.
        CreationTools.createJobDef(null, true, "pyl.JndiDb", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip, 42,
                "TestApp", null, "Test", "ModuleTest", "other", "other", false, em);
        JobRequest.create("TestApp", "TestUser").submit();
        TestHelpers.waitFor(1, 5000, em);
        TestHelpers.testOkCount(1, em);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        int nb = (Integer) mbs.getAttribute(new ObjectName("com.enioka.jqm:type=JdbcPool,name=jdbc/test"), "Active");
        Assert.assertEquals(0, nb);

        // Run the payload, it should leave an open conn that should be forcibly closed.
        CreationTools.createJobDef(null, true, "pyl.JndiDbLeak", null, "jqm-tests/jqm-test-pyl-nodep/target/test.jar", TestHelpers.qVip,
                42, "TestApp2", null, "Test", "ModuleTest", "other", "other", false, em);
        JobRequest.create("TestApp2", "TestUser").submit();
        TestHelpers.waitFor(1, 5000, em);

        // Test
        TestHelpers.testOkCount(2, em);
        nb = (Integer) mbs.getAttribute(new ObjectName("com.enioka.jqm:type=JdbcPool,name=jdbc/test"), "Active");
        Assert.assertEquals(0, nb);
    }
}
