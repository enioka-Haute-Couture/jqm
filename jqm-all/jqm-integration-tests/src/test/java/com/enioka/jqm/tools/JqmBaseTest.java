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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.apache.commons.io.IOUtils;
import org.hsqldb.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JqmBaseTest
{
    public static Logger jqmlogger = LoggerFactory.getLogger(JqmBaseTest.class);
    public static Server s;
    protected static Db db;
    public Map<String, JqmEngineOperations> engines = new HashMap<String, JqmEngineOperations>();
    public List<DbConn> cnxs = new ArrayList<DbConn>();

    protected DbConn cnx;

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void testInit() throws Exception
    {
        if (db == null)
        {
            JndiContext.createJndiContext();

            // If needed, create an HSQLDB server.
            InputStream fis = null;
            Properties p = new Properties();
            fis = Helpers.class.getClassLoader().getResourceAsStream("jqm.properties");
            if (fis != null)
            {
                p.load(fis);
                IOUtils.closeQuietly(fis);
                fis.close();
            }
            if (p.isEmpty() || !p.containsKey("com.enioka.jqm.jdbc.datasource") || (p.containsKey("com.enioka.jqm.jdbc.datasource")
                    && p.getProperty("com.enioka.jqm.jdbc.datasource").contains("hsql")))
            {
                s = new Server();
                s.setDatabaseName(0, "testdbengine");
                s.setDatabasePath(0, "mem:testdbengine");
                s.setLogWriter(null);
                s.setSilent(true);
                s.start();
            }

            // In all cases load the datasource. (the helper itself will load the property file if any).
            db = Helpers.getDb();
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
        cnx = getNewDbSession();
        TestHelpers.cleanup(cnx);
        TestHelpers.createTestData(cnx);
        cnx.commit();
    }

    @After
    public void afterTest()
    {
        jqmlogger.debug("*** Cleaning after test " + testName.getMethodName());
        for (String k : engines.keySet())
        {
            JqmEngineOperations e = engines.get(k);
            e.stop();
        }
        engines.clear();
        for (DbConn em : cnxs)
        {
            em.close();
        }
        cnxs.clear();

        // Java 6 GC being rather inefficient, we must run it multiple times to correctly collect Jetty-created class loaders and avoid
        // permgen issues
        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();
        System.gc();
    }

    protected JqmEngineOperations addAndStartEngine()
    {
        return addAndStartEngine("localhost");
    }

    protected JqmEngineOperations addAndStartEngine(String nodeName)
    {
        beforeStartEngine(nodeName);
        JqmEngineOperations e = JqmEngineFactory.startEngine(nodeName, new EngineCallback());
        engines.put(nodeName, e);
        afterStartEngine(nodeName);
        return e;
    }

    protected void beforeStartEngine(String nodeName)
    {
        // For overrides.
    }

    protected void afterStartEngine(String nodeName)
    {
        // For overrides.
    }

    protected void stopAndRemoveEngine(String nodeName)
    {
        JqmEngineOperations e = engines.get(nodeName);
        e.stop();
        engines.remove(nodeName);
    }

    protected DbConn getNewDbSession()
    {
        DbConn cnx = db.getConn();
        cnxs.add(cnx);
        return cnx;
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

    protected void simulateDbFailure()
    {
        if (db.getProduct().contains("hsql"))
        {
            jqmlogger.info("DB is going down");
            s.stop();
            this.waitDbStop();
            jqmlogger.info("DB is now fully down");
            this.sleep(1);
            jqmlogger.info("Restarting DB");
            s.start();
        }
        else if (db.getProduct().contains("postgresql"))
        {
            try
            {
                // update pg_database set datallowconn = false where datname = 'jqm' // Cannot run, as we cannot reconnect afterward!
                cnx.runRawSelect("select pg_terminate_backend(pid) from pg_stat_activity where datname='jqm';");
            }
            catch (Exception e)
            {
                // Do nothing - the query is a suicide so it cannot work fully.
            }
            Helpers.closeQuietly(cnx);
            cnx = getNewDbSession();
        }
    }

    protected void displayAllHistoryTable()
    {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
        jqmlogger.debug("==========================================================================================");
        for (JobInstance h : Query.create().run())
        {
            jqmlogger.debug("JobInstance Id: " + h.getId() + " | " + h.getState() + " | JD: " + h.getApplicationName() + " | "
                    + h.getQueueName() + " | enqueue: " + format.format(h.getEnqueueDate().getTime()) + " | exec: "
                    + (h.getBeganRunningDate() != null ? format.format(h.getBeganRunningDate().getTime()) : null) + " | end: "
                    + (h.getEndDate() != null ? format.format(h.getEndDate().getTime()) : null));
        }
        jqmlogger.debug("==========================================================================================");
    }
}
