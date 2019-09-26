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
import java.util.*;

import java.sql.ResultSet;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.apache.commons.lang.NotImplementedException;
import org.hsqldb.Server;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JqmBaseTest
{
    public static Logger jqmlogger = LoggerFactory.getLogger(JqmBaseTest.class);
    public static Server s;
    protected static Db db;
    public Map<String, JqmEngineOperations> engines = new HashMap<>();
    public List<DbConn> cnxs = new ArrayList<>();

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
            String dbName = System.getenv("DB");
            if (dbName == null || "hsqldb".equals(dbName))
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
        for (DbConn cnx : cnxs)
        {
            cnx.close();
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

    protected void AssumeWindows()
    {
        Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
    }

    protected void AssumeNotWindows()
    {
        Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));
    }

    protected boolean onWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    protected void assumeNotDb2()
    {
        String dbName = System.getenv("DB");
        if (dbName != null)
        {
            Assume.assumeFalse( "Test not implement for db2.", dbName.contains("db2"));
        }
    }

    protected void assumeNotOracle()
    {
        String dbName = System.getenv("DB");
        if (dbName != null)
        {
            Assume.assumeFalse("Test not implement for oracle.", dbName.contains("oracle"));
        }
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

    protected void simulateDbFailure(int waitTimeBeforeRestart)
    {
        if (db.getProduct().contains("hsql"))
        {
            jqmlogger.info("DB is going down");
            s.stop();
            this.waitDbStop();
            jqmlogger.info("DB is now fully down");
            this.sleep(waitTimeBeforeRestart);
            jqmlogger.info("Restarting DB");
            s.start();
        }
        else if (db.getProduct().contains("postgresql"))
        {
            try
            {
                jqmlogger.info("Send suicide query");
                cnx.runRawCommand("select pg_terminate_backend(pid) from pg_stat_activity where datname='jqm';");
                this.sleep(waitTimeBeforeRestart);
                Helpers.closeQuietly(cnx);
            }
            catch (Exception e)
            {
                jqmlogger.warn("Failed to kill postgresql : " + e.getMessage());
            }
        }
        else if (db.getProduct().contains("mariadb"))
        {
            try
            {
                jqmlogger.info("Send suicide query");
                cnx.runRawCommand("KILL USER jqm;");
                this.sleep(waitTimeBeforeRestart);
                Helpers.closeQuietly(cnx);
            }
            catch (Exception e)
            {
                jqmlogger.warn("Failed to kill mariadb : " + e.getMessage());
            }
        }
        else if (db.getProduct().contains("mysql"))
        {
            ResultSet res = null;
            try
            {
                // mysql 5.7, 8.0
                jqmlogger.info("Retrieve process id list.");
                res = cnx.runRawSelect("SELECT ID FROM INFORMATION_SCHEMA.PROCESSLIST WHERE USER = 'jqm'");

                ArrayList<Integer> processIdList = new ArrayList<Integer>();
                while (res.next())
                {
                    processIdList.add(res.getInt("ID"));
                }

                jqmlogger.info("Kill all connection (" + processIdList.size() + ").");
                Iterator it = processIdList.iterator();
                while (it.hasNext())
                {
                    String query = "KILL CONNECTION " + it.next();
                    jqmlogger.debug(query);
                    cnx.runRawCommand(query);
                }

                this.sleep(waitTimeBeforeRestart);
                Helpers.closeQuietly(cnx);
            }
            catch (Exception e)
            {
                jqmlogger.warn("Failed to kill mysql connections : " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                if (res != null)
                {
                    try
                    {
                        res.close();
                    }
                    catch (Exception e)
                    {
                        res = null;
                    }
                }
            }
        }
        else if (db.getProduct().contains("oracle"))
        {
            try
            {
                jqmlogger.info("Send select sid, serial query");
                ResultSet res = cnx.runRawSelect("SELECT SID,SERIAL# FROM GV$SESSION WHERE USERNAME = 'JQM'");

                while (res.next())
                {
                    String killReq = "ALTER SYSTEM DISCONNECT SESSION '" + res.getInt("SID") + "," + res.getInt("SERIAL#") + "' IMMEDIATE";
                    jqmlogger.debug(killReq);
                    cnx.runRawCommand(killReq);
                }
                this.sleep(waitTimeBeforeRestart);
                Helpers.closeQuietly(cnx);
            }
            catch (Exception e)
            {
                jqmlogger.warn("Failed to kill oracle session : " + e.getMessage());
                e.printStackTrace();
            }
        }
        else if (db.getProduct().contains("db2"))
        {
            try
            {
                jqmlogger.info("Send select application_handle query");
                ResultSet res = cnx.runRawSelect("SELECT APPLICATION_HANDLE, CLIENT_IPADDR, CLIENT_PORT_NUMBER, SESSION_AUTH_ID,\n" +
                        "CURRENT_SERVER, APPLICATION_NAME, CLIENT_PROTOCOL, CLIENT_PLATFORM, CLIENT_HOSTNAME, CONNECTION_START_TIME, APPLICATION_ID, EXECUTION_ID \n" +
                        "FROM TABLE(MON_GET_CONNECTION(cast(NULL as bigint), -2))\n");

                while (res.next())
                {
                    String query = "CALL SYSPROC.ADMIN_CMD('FORCE APPLICATION (" + res.getInt("APPLICATION_HANDLE") + ")')";
                    jqmlogger.debug(query);
                    cnx.runRawCommand(query);
                }
                this.sleep(waitTimeBeforeRestart);
                Helpers.closeQuietly(cnx);
            }
            catch (Exception e)
            {
                jqmlogger.warn("Failed to kill db2 session : " + e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            throw new NotImplementedException("Database not supported.");
        }
    }

    protected boolean waitForPollersArePolling()
    {
        int remainingAttempt = 10;
        while (!this.engines.get("localhost").areAllPollersPolling() && remainingAttempt > 0)
        {
            this.sleep(1);
            --remainingAttempt;
            jqmlogger.debug("waitFormPollersArePolling countdown : " + remainingAttempt);
        }
        return this.engines.get("localhost").areAllPollersPolling();
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

    protected void displayAllQueueTable()
    {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
        jqmlogger.debug("==========================================================================================");
        for (JobInstance h : Query.create().setQueryHistoryInstances(false).setQueryLiveInstances(true).run())
        {
            jqmlogger.debug("JobInstance Id: " + h.getId() + " | " + h.getState() + " | JD: " + h.getApplicationName() + " | "
                    + h.getQueueName() + " | enqueue: " + format.format(h.getEnqueueDate().getTime()) + " | exec: "
                    + (h.getBeganRunningDate() != null ? format.format(h.getBeganRunningDate().getTime()) : null) + " | position: "
                    + h.getPosition());
        }
        jqmlogger.debug("==========================================================================================");
    }
}
