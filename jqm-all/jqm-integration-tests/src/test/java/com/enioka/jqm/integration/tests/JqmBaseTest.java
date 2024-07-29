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
package com.enioka.jqm.integration.tests;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.enioka.jqm.model.updater.DbSchemaManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JqmClient;
import com.enioka.jqm.client.api.JqmDbClientFactory;
import com.enioka.jqm.clusternode.EngineCallback;
import com.enioka.jqm.engine.api.lifecycle.JqmEngineOperations;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.jndi.api.JqmJndiContextControlService;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;
import com.enioka.jqm.test.helpers.DebugHsqlDbServer;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JqmBaseTest
{
    public static Logger jqmlogger = LoggerFactory.getLogger(JqmBaseTest.class);

    protected static Db db;

    public Map<String, JqmEngineOperations> engines = new HashMap<>();
    public List<DbConn> cnxs = new ArrayList<>();
    protected DbConn cnx;

    protected static DebugHsqlDbServer s;

    JqmClient jqmClient;

    @Rule
    public TestName testName = new TestName();

    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @BeforeClass
    public static void beforeClass()
    {
        if (s == null)
        {
            s = new DebugHsqlDbServer();
            s.start();

            System.setProperty("com.enioka.jqm.alternateJqmRoot", "./target/server");
            ServiceLoaderHelper.getService(ServiceLoader.load(JqmJndiContextControlService.class)).registerIfNeeded();
        }
    }

    @Before
    public void beforeEachTest() throws NamingException, SQLException
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test " + testName.getMethodName());

        JqmDbClientFactory.reset();
        jqmClient = JqmDbClientFactory.getClient();

        if (db == null)
        {
            // In all cases load the datasource. (the helper itself will load the property file if any).
            Properties p = new Properties();
            p.put("com.enioka.jqm.jdbc.waitForConnectionValid", "false");
            p.put("com.enioka.jqm.jdbc.waitForSchemaValid", "false");
            db = DbManager.getDb(p);
            var dbSchemaManager = ServiceLoaderHelper.getService(ServiceLoader.load(DbSchemaManager.class));
            try (var cnx = db.getDataSource().getConnection())
            {
                dbSchemaManager.updateSchema(cnx);
            }
        }

        cnx = getNewDbSession();
        TestHelpers.cleanup(cnx);
        TestHelpers.createTestData(cnx);
        cnx.commit();

        // Force JNDI directory loading
        InitialContext.doLookup("string/debug");
    }

    @After
    public void afterEachTest()
    {
        jqmlogger.debug("*** Cleaning after test " + testName.getMethodName());
        for (String k : engines.keySet())
        {
            JqmEngineOperations e = engines.get(k);
            e.stop();
        }
        engines.clear();
        for (DbConn cnx : cnxs)
        {
            cnx.close();
        }
        cnxs.clear();

        // Reset the caches - no side effect between tests?
        try
        {
            InitialContext.doLookup("internal://reset");
        }
        catch (NamingException e)
        {
            // jqmlogger.warn("Could not purge test JNDI context", e);
        }

        // Java 6 GC being rather inefficient, we must run it multiple times to correctly collect Jetty-created class loaders and avoid
        // permgen issues
        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();
        System.gc();
    }

    protected void AssumeWindows()
    {
        Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
    }

    protected void AssumeNotWindows()
    {
        Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));
    }

    protected void AssumeHsqldb()
    {
        Assume.assumeTrue(s.isHsqldb());
    }

    protected void assumeJavaVersionStrictlyGreaterThan(double version)
    {
        Assume.assumeTrue(getJavaVersion() > version);
    }

    protected void assumeJavaVersionStrictlyLowerThan(double version)
    {
        Assume.assumeTrue(getJavaVersion() < version);
    }

    protected double getJavaVersion()
    {
        String ver = System.getProperty("java.version");
        return Double.parseDouble(ver.substring(0, ver.indexOf('.') + 2));
    }

    protected boolean onWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    protected JqmEngineOperations addAndStartEngine()
    {
        return addAndStartEngine("localhost");
    }

    protected JqmEngineOperations addAndStartEngine(String nodeName)
    {
        beforeStartEngine(nodeName);

        var engine = ServiceLoaderHelper.getService(ServiceLoader.load(JqmEngineOperations.class));

        engine.start(nodeName, new EngineCallback());
        engines.put(nodeName, engine);
        afterStartEngine(nodeName);
        return engine;
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
        sleepms(1000 * s);
    }

    protected static void sleepms(int ms)
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

    protected void simulateDbFailure()
    {
        if (db.getProduct().contains("hsql"))
        {
            jqmlogger.info("DB is going down");
            s.close();
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
            cnx.close();
            cnx = getNewDbSession();
        }
    }

    protected void displayAllHistoryTable()
    {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
        jqmlogger.debug("==========================================================================================");
        for (JobInstance h : jqmClient.newQuery().invoke())
        {
            jqmlogger.debug("JobInstance Id: " + h.getId() + " | " + h.getState() + " | JD: " + h.getApplicationName() + " | "
                    + h.getQueueName() + " | enqueue: " + format.format(h.getEnqueueDate().getTime()) + " | exec: "
                    + (h.getBeganRunningDate() != null ? format.format(h.getBeganRunningDate().getTime()) : null) + " | end: "
                    + (h.getEndDate() != null ? format.format(h.getEndDate().getTime()) : null));
        }
        jqmlogger.debug("==========================================================================================");
    }

    protected void displayAllQueueTable()
    {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
        jqmlogger.debug("==========================================================================================");
        for (JobInstance h : jqmClient.newQuery().setQueryHistoryInstances(false).setQueryLiveInstances(true).invoke())
        {
            jqmlogger.debug("JobInstance Id: " + h.getId() + " | " + h.getState() + " | JD: " + h.getApplicationName() + " | "
                    + h.getQueueName() + " | enqueue: " + format.format(h.getEnqueueDate().getTime()) + " | exec: "
                    + (h.getBeganRunningDate() != null ? format.format(h.getBeganRunningDate().getTime()) : null) + " | position: "
                    + h.getPosition());
        }
        jqmlogger.debug("==========================================================================================");
    }

    /**
     * This test simply tests junit works.
     */
    @Test
    public void testContainerStarts()
    {
        Assert.assertTrue(true);
    }
}
