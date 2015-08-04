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
package com.enioka.jqm.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JqmBaseTest
{
    public static Logger jqmlogger = Logger.getLogger(JqmBaseTest.class);
    public static Server s;
    public Map<String, JqmEngine> engines = new HashMap<String, JqmEngine>();
    public List<EntityManager> ems = new ArrayList<EntityManager>();

    protected EntityManager em;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void testInit() throws Exception
    {
        if (s == null)
        {
            JndiContext.createJndiContext();
            s = new Server();
            s.setDatabaseName(0, "testdbengine");
            s.setDatabasePath(0, "mem:testdbengine");
            s.setLogWriter(null);
            s.setSilent(true);
            s.start();
        }
    }

    @Before
    public void beforeTest()
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test " + testName.getMethodName());

        try
        {
            ((JndiContext) NamingManager.getInitialContext(null)).resetSingletons();
        }
        catch (NamingException e)
        {
            jqmlogger.warn("Could not purge test JNDI context", e);
        }
        JqmClientFactory.resetClient(null);
        em = getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createTestData(em);
    }

    @After
    public void afterTest()
    {
        jqmlogger.debug("*** Cleaning after test " + testName.getMethodName());
        for (String k : engines.keySet())
        {
            JqmEngine e = engines.get(k);
            e.stop();
        }
        engines.clear();
        for (EntityManager em : ems)
        {
            Helpers.closeQuietly(em);
        }
        ems.clear();

        // Java 6 GC being rather inefficient, we must run it multiple times to correctly collect Jetty-created class loaders and avoid
        // permgen issues
        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();
        System.gc();
    }

    protected JqmEngine addAndStartEngine()
    {
        return addAndStartEngine("localhost");
    }

    protected JqmEngine addAndStartEngine(String nodeName)
    {
        JqmEngine e = new JqmEngine();
        engines.put(nodeName, e);
        e.start(nodeName);
        return e;
    }

    protected void stopAndRemoveEngine(String nodeName)
    {
        JqmEngine e = engines.get(nodeName);
        e.stop();
        engines.remove(nodeName);
    }

    protected EntityManager getNewEm()
    {
        EntityManager em = Helpers.getNewEm();
        ems.add(em);
        return em;
    }

    protected void sleep(int s)
    {
        this.sleepms(1000 * s);
    }

    protected void sleepms(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            // not an issue in tests
        }
    }

    protected void waitDbStop()
    {
        while (s.getState() != 16)
        {
            this.sleepms(1);
        }
    }
}
