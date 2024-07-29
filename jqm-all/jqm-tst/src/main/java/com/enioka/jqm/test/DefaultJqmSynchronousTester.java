package com.enioka.jqm.test;

import java.io.File;
import java.util.ServiceLoader;
import java.sql.SQLException;

import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import org.kohsuke.MetaInfServices;

import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JqmClientFactory;
import com.enioka.jqm.engine.api.lifecycle.JqmSingleRunnerOperations;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.jndi.api.JqmJndiContextControlService;
import com.enioka.jqm.model.Instruction;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.RuntimeParameter;
import com.enioka.jqm.model.State;
import com.enioka.jqm.model.updater.DbSchemaManager;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;
import com.enioka.jqm.test.api.JqmSynchronousTester;

/**
 * Actual implementation of {@link JqmSynchronousTester}.
 */
@MetaInfServices(JqmSynchronousTester.class)
public class DefaultJqmSynchronousTester implements JqmSynchronousTester
{
    private DbConn cnx = null;
    private File resDirectoryPath;

    private JqmSingleRunnerOperations runner;

    private Node node = null;
    private Long jd = null;
    private Long q = null;
    private Long ji = null;

    public DefaultJqmSynchronousTester()
    {
        // Main resource is jdbc/jqm and uses a memory url, meaning db is created on first use.
        System.setProperty("com.enioka.jqm.resourceFiles", "resources.xml,resources_internal.xml");
        System.setProperty("com.enioka.jqm.cl.allow_system_cl", "true");

        // JNDI substrate
        ServiceLoaderHelper.getService(ServiceLoader.load(JqmJndiContextControlService.class)).registerIfNeeded();

        // Db connexion should now work.
        var dbSchemaManager = ServiceLoaderHelper.getService(ServiceLoader.load(DbSchemaManager.class));
        var db = DbManager.getDb(Common.dbProperties());
        try (var cnx = db.getDataSource().getConnection())
        {
            dbSchemaManager.updateSchema(cnx);
        }
        catch (SQLException e)
        {
            throw new JqmInitError("Could not create database", e);
        }
        cnx = db.getConn();

        // Just in case
        Common.resetAllData(cnx);

        // Db is ready
        cnx.commit();

        // Create node
        resDirectoryPath = Common.createTempDirectory();
        node = Node.create(cnx, "testtempnode", 12, resDirectoryPath.getAbsolutePath(), resDirectoryPath.getAbsolutePath(),
                resDirectoryPath.getAbsolutePath(), "test", "INFO");

        q = Queue.create(cnx, "default", "default test queue", true); // Only useful because JobDef.queue is non-null

        cnx.commit();

        // Finally get the runner
        this.runner = ServiceLoaderHelper.getService(ServiceLoader.load(JqmSingleRunnerOperations.class));
    }

    /**
     * Start of the fluent API to construct a test case.
     *
     * @param className
     * @return
     */
    public static JqmSynchronousTester create(String className)
    {
        return new DefaultJqmSynchronousTester().setJobClass(className);
    }

    /**
     * Start of the fluent API to construct a test case.
     *
     * @param clazz
     * @return
     */
    public static JqmSynchronousTester create(Class<?> clazz)
    {
        return new DefaultJqmSynchronousTester().setJobClass(clazz);
    }

    @Override
    public void close()
    {
        if (cnx != null)
        {
            cnx.close();
            cnx = null;
        }
    }

    @Override
    public JqmSynchronousTester addParameter(String key, String value)
    {
        if (this.ji == null)
        {
            throw new IllegalStateException("job class has not been set.");
        }
        RuntimeParameter.create(cnx, ji, key, value);
        cnx.commit();
        return this;
    }

    @Override
    public JqmSynchronousTester setJobClass(String className)
    {
        cnx.runUpdate("jd_delete_all");
        jd = JobDef.create(cnx, "test application", className, null, "/dev/null", q, 0, "TestApplication", null, null, null, null, null,
                false, null, PathType.MEMORY);

        ji = com.enioka.jqm.model.JobInstance.enqueue(cnx, State.SUBMITTED, q, jd, null, null, null, null, null, null, null, null, null,
                false, false, null, 0, Instruction.RUN, null);
        cnx.runUpdate("ji_update_status_by_id", node.getId(), ji);

        cnx.commit();
        return this;
    }

    @Override
    public JqmSynchronousTester setJobClass(Class<?> clazz)
    {
        setJobClass(clazz.getCanonicalName());
        return this;
    }

    @Override
    public JobInstance run()
    {
        if (this.ji == null)
        {
            throw new IllegalStateException("job class has not been set.");
        }

        com.enioka.jqm.model.JobInstance jiInternal = runner.runAtOnce(this.ji);
        return JqmClientFactory.getClient().getJob(jiInternal.getId());
    }
}
