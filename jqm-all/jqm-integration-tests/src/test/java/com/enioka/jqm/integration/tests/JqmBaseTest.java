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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Before;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.junit.Assert;
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
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.DockerClientFactory;

public class JqmBaseTest
{
    public static Logger jqmlogger = LoggerFactory.getLogger(JqmBaseTest.class);
    private static final String TESTCONTAINER_RESOURCE_FILE = "resources-testcontainer.xml";

    protected static Db db;

    public Map<String, JqmEngineOperations> engines = new HashMap<>();
    public List<DbConn> cnxs = new ArrayList<>();
    protected DbConn cnx;

    protected static DebugHsqlDbServer s;

    protected static JdbcDatabaseContainer<?> dbContainer;

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
        String dbType = System.getenv("DB");

        if ("hsqldb".equalsIgnoreCase(dbType) || dbType == null)
        {
            if (s == null)
            {
                s = new DebugHsqlDbServer();
                s.start();
                jqmlogger.info("Started local HSQLDB server.");
            }
        }
        else
        {
            initTestContainer();
        }

        System.setProperty("com.enioka.jqm.alternateJqmRoot", "./target/server");
        configureResourceFiles(dbType);
        ServiceLoaderHelper.getService(ServiceLoader.load(JqmJndiContextControlService.class)).registerIfNeeded();
    }

    @AfterClass
    public static void afterClass()
    {
        if (dbContainer != null && dbContainer.isRunning())
        {
            dbContainer.stop();
        }
        if (s != null)
        {
            s.close();
            s = null;
        }
    }

    private static void initTestContainer()
    {
        if (dbContainer != null && dbContainer.isRunning())
        {
            return;
        }

        String dbType = System.getenv("DB");
        String dbVersion = System.getenv("DB_VERSION");

        if (dbType == null || dbType.isEmpty())
        {
            dbType = "postgresql";
        }
        jqmlogger.info("Starting testcontainer for {} version {}", dbType, dbVersion);
        switch (dbType.toLowerCase())
        {
        case "postgresql":
            if (dbVersion == null || dbVersion.isEmpty())
            {
                dbVersion = "15-alpine";
            }
            dbContainer = new PostgreSQLContainer<>("postgres:" + dbVersion).withDatabaseName("jqm").withUsername("jqm")
                    .withPassword("jqm");
            break;
        case "mysql":
            if (dbVersion == null || dbVersion.isEmpty())
            {
                dbVersion = "8";
            }
            dbContainer = new MySQLContainer<>("mysql:" + dbVersion).withDatabaseName("jqm").withUsername("jqm").withPassword("jqm");
            break;
        case "mariadb":
            if (dbVersion == null || dbVersion.isEmpty())
            {
                dbVersion = "10";
            }
            dbContainer = new MariaDBContainer<>("mariadb:" + dbVersion).withDatabaseName("jqm").withUsername("jqm")
                    .withPassword("jqm");
            break;
        case "hsqldb":
            return;
        default:
            throw new IllegalArgumentException("Unsupported database type provided: " + dbType);
        }

        dbContainer.start();
        jqmlogger.info("Testcontainer started for {} version {}", dbType, dbVersion);
    }

    private static void configureResourceFiles(String dbType)
    {
        if (dbType == null || "hsqldb".equalsIgnoreCase(dbType))
        {
            System.setProperty("com.enioka.jqm.resourceFiles", "resources.xml");
            return;
        }

        try
        {
            writeTestcontainerResourceFile(dbType.toLowerCase());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not write dynamic JNDI resource file for testcontainers", e);
        }

        System.setProperty("com.enioka.jqm.resourceFiles", "resources.xml," + TESTCONTAINER_RESOURCE_FILE);
    }

    private static void writeTestcontainerResourceFile(String dbType) throws IOException
    {
        if (dbContainer == null || !dbContainer.isRunning())
        {
            throw new IllegalStateException("Cannot create dynamic JNDI resources without a running database container");
        }

        String validationQuery;
        switch (dbType)
        {
        case "postgresql":
            validationQuery = "SELECT 1";
            break;
        case "mysql":
        case "mariadb":
            validationQuery = "SELECT version()";
            break;
        default:
            throw new IllegalArgumentException("Unsupported database type provided: " + dbType);
        }

        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<resources>" +
                "   <resource" +
                "       name=\"jdbc/" + dbType + "\"" +
                "       auth=\"Container\"" +
                "       type=\"javax.sql.DataSource\"" +
                "       factory=\"org.apache.tomcat.jdbc.pool.DataSourceFactory\"" +
                "       testWhileIdle=\"true\"" +
                "       testOnBorrow=\"true\"" +
                "       testOnReturn=\"false\"" +
                "       validationQuery=\"" + validationQuery + "\"" +
                "       validationInterval=\"1000\"" +
                "       timeBetweenEvictionRunsMillis=\"1000\"" +
                "       maxActive=\"80\"" +
                "       maxIdle=\"40\"" +
                "       minIdle=\"3\"" +
                "       maxWait=\"10000\"" +
                "       initialSize=\"5\"" +
                "       removeAbandonedTimeout=\"3600\"" +
                "       removeAbandoned=\"true\"" +
                "       logAbandoned=\"true\"" +
                "       minEvictableIdleTimeMillis=\"60000\"" +
                "       jmxEnabled=\"true\"" +
                "       username=\"" + escapeXmlAttribute(dbContainer.getUsername()) + "\"" +
                "       password=\"" + escapeXmlAttribute(dbContainer.getPassword()) + "\"" +
                "       url=\"" + escapeXmlAttribute(dbContainer.getJdbcUrl()) + "\"" +
                "       singleton=\"true\" />" +
                "</resources>\n";

        Path output = Path.of("target", "test-classes", TESTCONTAINER_RESOURCE_FILE);
        Files.createDirectories(output.getParent());
        Files.writeString(output, xml, StandardCharsets.UTF_8);
    }

    private static String escapeXmlAttribute(String value)
    {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Before
    public void beforeEachTest() throws NamingException, SQLException
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test " + testName.getMethodName());

        if (db == null)
        {
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

        // Initialisation du client JQM après la base
        JqmDbClientFactory.reset();
        jqmClient = JqmDbClientFactory.getClient();

        cnx = getNewDbSession();
        TestHelpers.cleanup(cnx);
        TestHelpers.createTestData(cnx);
        cnx.commit();

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

    protected void assumeWindows()
    {
        Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
    }

    protected void assumeNotWindows()
    {
        Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));
    }

    protected void assumeHsqldb()
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

    protected boolean onMacOS()
    {
        return System.getProperty("os.name").toLowerCase().contains("mac");
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

    protected void simulateDbFailure(int delay)
    {
        if (db.getProduct().contains("hsql"))
        {
            jqmlogger.info("DB is going down");
            s.close();
            jqmlogger.info("DB is now fully down");
            this.sleep(delay);
            jqmlogger.info("Restarting DB");
            s.start();
            this.sleep(delay);
        }
        else
        {
            jqmlogger.info("DB is going down (pausing container)");
            jqmlogger.info(dbContainer.getJdbcUrl());
            String containerId = dbContainer.getContainerId();
            DockerClientFactory.instance().client().pauseContainerCmd(containerId).exec();
            this.sleep(delay);
            DockerClientFactory.instance().client().unpauseContainerCmd(containerId).exec();
            this.sleep(delay);
            jqmlogger.info("DB is now fully up");
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

    protected void assumeNotDb2()
    {
        String dbName = System.getenv("DB");
        if (dbName != null)
        {
            Assume.assumeFalse("Test not implemented for db2.", dbName.contains("db2"));
        }
    }

    protected void assumeNotOracle()
    {
        String dbName = System.getenv("DB");
        if (dbName != null)
        {
            Assume.assumeFalse("Test not implemented for oracle.", dbName.contains("oracle"));
        }
    }
}
