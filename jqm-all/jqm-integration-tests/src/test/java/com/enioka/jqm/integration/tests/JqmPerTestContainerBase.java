package com.enioka.jqm.integration.tests;

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
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
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
    private static final int FIXED_DB_HOST_PORT = 18532;
    private static final String FIXED_PORT_RESOURCE_FILE = "resources-dbfail.xml";

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

            // Use static resource file with fixed port - no dynamic generation needed
            System.setProperty("com.enioka.jqm.resourceFiles", "resources.xml," + FIXED_PORT_RESOURCE_FILE);
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
            if (TEST_INFRA_LOCK.isHeldByCurrentThread())
            {
                TEST_INFRA_LOCK.unlock();
            }
        }
    }

    @Override
    protected void configureDefaultResourceFiles()
    {
        System.setProperty("com.enioka.jqm.resourceFiles", "resources.xml");
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

        String containerId = dedicatedDbContainer.getContainerId();
        jqmlogger.info("DB container {} is going down (killing container)", containerId);
        DockerClientFactory.instance().client().killContainerCmd(containerId).exec();
        this.sleep(delay);
        jqmlogger.info("DB container {} is going up", containerId);
        DockerClientFactory.instance().client().startContainerCmd(containerId).exec();
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
            return new PostgreSQLContainer<>("postgres:" + dbVersion)
                .withDatabaseName("jqm").withUsername("jqm").withPassword("jqm")
                .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(FIXED_DB_HOST_PORT),
                                new ExposedPort(5432)))));
        case "mysql":
            if (dbVersion == null || dbVersion.isEmpty())
            {
                dbVersion = "8";
            }
            return new MySQLContainer<>("mysql:" + dbVersion)
                .withDatabaseName("jqm").withUsername("jqm").withPassword("jqm")
                .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(FIXED_DB_HOST_PORT),
                                new ExposedPort(3306)))));
        case "mariadb":
            if (dbVersion == null || dbVersion.isEmpty())
            {
                dbVersion = "10";
            }
            return new MariaDBContainer<>("mariadb:" + dbVersion)
                .withDatabaseName("jqm").withUsername("jqm").withPassword("jqm")
                .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(FIXED_DB_HOST_PORT),
                                new ExposedPort(3306)))));
        default:
            throw new IllegalArgumentException("Unsupported database type provided: " + dbType);
        }
    }

    private void resetDatabaseState() throws NamingException
    {
        JqmDbClientFactory.reset();
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
}
