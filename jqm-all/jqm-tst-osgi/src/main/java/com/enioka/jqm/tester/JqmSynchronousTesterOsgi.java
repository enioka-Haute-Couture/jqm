package com.enioka.jqm.tester;

import java.io.File;
import java.sql.SQLException;

import javax.naming.InitialContext;

import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.jdbc.api.JqmClientFactory;
import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.engine.api.lifecycle.JqmSingleRunnerOperations;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Instruction;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.RuntimeParameter;
import com.enioka.jqm.model.State;
import com.enioka.jqm.model.updater.api.DbSchemaManager;
import com.enioka.jqm.tester.api.JqmSynchronousTester;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Actual implementation of {@link JqmSynchronousTester}. Interface/implementation split only exists in order to allow easier interaction
 * from outside the OSGi world.
 */
@Component(service = JqmSynchronousTester.class, scope = ServiceScope.SINGLETON, immediate = false)
public class JqmSynchronousTesterOsgi implements JqmSynchronousTester
{
    private DbConn cnx = null;
    private File resDirectoryPath;

    private JqmSingleRunnerOperations runner;

    private Node node = null;
    private Integer jd = null;
    private Integer q = null;
    private Integer ji = null;

    // Just for dependency.
    @Reference
    InitialContext jndiInitialContext;

    @Activate
    public JqmSynchronousTesterOsgi(@Reference JqmSingleRunnerOperations runner, @Reference DbSchemaManager dbSchemaManager,
            BundleContext bundleContext)
    {
        // Main resource is jdbc/jqm and uses a memory url, meaning db is created on first use.
        System.setProperty("com.enioka.jqm.resourceFiles", "resources_internal.xml");

        // Db connexion should now work.
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

        // Needed parameters
        GlobalParameter.setParameter(cnx, "defaultConnection", "");
        cnx.commit();

        // Create node
        resDirectoryPath = Common.createTempDirectory();
        node = Node.create(cnx, "testtempnode", 12, resDirectoryPath.getAbsolutePath(), resDirectoryPath.getAbsolutePath(),
                resDirectoryPath.getAbsolutePath(), "test", "INFO");

        q = Queue.create(cnx, "default", "default test queue", true); // Only useful because JobDef.queue is non-null

        cnx.commit();

        // Finaly get the runner
        this.runner = runner;
    }

    @Deactivate
    public void deactivate(BundleContext bundleContext)
    {
        if (cnx != null)
        {
            cnx.close();
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
