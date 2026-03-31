package com.enioka.jqm.integration.tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.enioka.jqm.client.api.JqmDbClientFactory;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.updater.DbSchemaManager;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;
import com.enioka.jqm.test.helpers.TestHelpers;
import org.junit.After;
import org.junit.Before;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class JqmPerTestContainerBase extends JqmBaseTest
{
    private static final ReentrantLock TEST_INFRA_LOCK = new ReentrantLock();
    private static final String RESOURCE_FILE_PREFIX = "resources-testcontainer-";

    protected JdbcDatabaseContainer<?> dedicatedDbContainer;

    @Before
    @Override
    public void beforeEachTest() throws NamingException, SQLException
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test " + testName.getMethodName());

        prepareDatabaseEnvironment();

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
    @Override
    public void afterEachTest()
    {
        super.afterEachTest();
        cleanupDatabaseEnvironment();
    }

    @Override
    protected void prepareDatabaseEnvironment() throws NamingException
    {
        TEST_INFRA_LOCK.lock();
        try
        {
            super.prepareDatabaseEnvironment();

            String dbType = System.getenv("DB");
            if (dbType == null || dbType.isEmpty() || "hsqldb".equalsIgnoreCase(dbType))
            {
                return;
            }

            String dbVersion = System.getenv("DB_VERSION");
            dedicatedDbContainer = createTestContainer(dbType, dbVersion);
            dedicatedDbContainer.start();
            configureDedicatedResourceFiles(dbType);
            resetDatabaseState();
        }
        catch (RuntimeException | NamingException e)
        {
            cleanupDedicatedContainerQuietly();
            TEST_INFRA_LOCK.unlock();
            throw e;
        }
    }

    @Override
    protected void cleanupDatabaseEnvironment()
    {
        try
        {
            if (dedicatedDbContainer != null)
            {
                try
                {
                    resetDatabaseState();
                }
                catch (NamingException e)
                {
                    throw new IllegalStateException("Could not reset dedicated DB test state", e);
                }
                cleanupDedicatedContainerQuietly();
                configureDefaultResourceFiles();
            }
        }
        finally
        {
            TEST_INFRA_LOCK.unlock();
        }
    }

    @Override
    protected void simulateDbFailure(int delay)
    {
        if (db.getProduct().contains("hsql"))
        {
            super.simulateDbFailure(delay);
            return;
        }

        if (dedicatedDbContainer == null || !dedicatedDbContainer.isRunning())
        {
            throw new IllegalStateException("simulateDbFailure requires a running dedicated testcontainer");
        }

        jqmlogger.info("DB is going down (pausing container)");
        jqmlogger.info(dedicatedDbContainer.getJdbcUrl());
        String containerId = dedicatedDbContainer.getContainerId();
        DockerClientFactory.instance().client().pauseContainerCmd(containerId).exec();
        this.sleep(delay);
        DockerClientFactory.instance().client().unpauseContainerCmd(containerId).exec();
        this.sleep(delay);
        jqmlogger.info("DB is now fully up");
    }

    private static JdbcDatabaseContainer<?> createTestContainer(String dbType, String dbVersion)
    {
        switch (dbType.toLowerCase())
        {
        case "postgresql":
            if (dbVersion == null || dbVersion.isEmpty())
            {
                dbVersion = "15-alpine";
            }
            return new PostgreSQLContainer<>("postgres:" + dbVersion).withDatabaseName("jqm").withUsername("jqm").withPassword("jqm");
        case "mysql":
            if (dbVersion == null || dbVersion.isEmpty())
            {
                dbVersion = "8";
            }
            return new MySQLContainer<>("mysql:" + dbVersion).withDatabaseName("jqm").withUsername("jqm").withPassword("jqm");
        case "mariadb":
            if (dbVersion == null || dbVersion.isEmpty())
            {
                dbVersion = "10";
            }
            return new MariaDBContainer<>("mariadb:" + dbVersion).withDatabaseName("jqm").withUsername("jqm").withPassword("jqm");
        default:
            throw new IllegalArgumentException("Unsupported database type provided: " + dbType);
        }
    }

    private void configureDedicatedResourceFiles(String dbType)
    {
        if (dedicatedDbContainer == null || !dedicatedDbContainer.isRunning())
        {
            throw new IllegalStateException("Cannot configure dedicated DB resources without a running dedicated testcontainer");
        }

        String resourceFileName = RESOURCE_FILE_PREFIX + getClass().getSimpleName() + ".xml";
        try
        {
            writeTestcontainerResourceFile(dbType.toLowerCase(), dedicatedDbContainer, resourceFileName);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not write dedicated JNDI resource file for testcontainers", e);
        }

        System.setProperty("com.enioka.jqm.resourceFiles", "resources.xml," + resourceFileName);
    }

    private void resetDatabaseState() throws NamingException
    {
        JqmDbClientFactory.reset();
        DbManager.reset();
        db = null;
        InitialContext.doLookup("internal://reset");
    }

    private void cleanupDedicatedContainerQuietly()
    {
        if (dedicatedDbContainer != null)
        {
            dedicatedDbContainer.stop();
            dedicatedDbContainer = null;
        }
    }

    private static void writeTestcontainerResourceFile(String dbType, JdbcDatabaseContainer<?> container, String resourceFileName)
            throws IOException
    {
        String validationQuery;
        String driverClassName;
        switch (dbType)
        {
        case "postgresql":
            validationQuery = "SELECT 1";
            driverClassName = "org.postgresql.Driver";
            break;
        case "mysql":
            validationQuery = "SELECT version()";
            driverClassName = "com.mysql.cj.jdbc.Driver";
            break;
        case "mariadb":
            validationQuery = "SELECT version()";
            driverClassName = "org.mariadb.jdbc.Driver";
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
                "       testOnBorrow=\"false\"" +
                "       testOnReturn=\"true\"" +
                "       validationQuery=\"" + validationQuery + "\"" +
                "       validationInterval=\"1000\"" +
                "       timeBetweenEvictionRunsMillis=\"60000\"" +
                "       maxActive=\"100\"" +
                "       minIdle=\"2\"" +
                "       maxWait=\"30000\"" +
                "       initialSize=\"5\"" +
                "       removeAbandonedTimeout=\"3600\"" +
                "       removeAbandoned=\"true\"" +
                "       logAbandoned=\"true\"" +
                "       minEvictableIdleTimeMillis=\"60000\"" +
                "       jmxEnabled=\"true\"" +
                "       driverClassName=\"" + driverClassName + "\"" +
                "       username=\"" + escapeXmlAttribute(container.getUsername()) + "\"" +
                "       password=\"" + escapeXmlAttribute(container.getPassword()) + "\"" +
                "       url=\"" + escapeXmlAttribute(container.getJdbcUrl()) + "\"" +
                "       singleton=\"true\" />" +
                "</resources>\n";

        Path output = Path.of("target", "test-classes", resourceFileName);
        Files.createDirectories(output.getParent());
        Files.writeString(output, xml, StandardCharsets.UTF_8);
    }

    private static String escapeXmlAttribute(String value)
    {
        return value.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
