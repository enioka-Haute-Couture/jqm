package com.enioka.jqm.tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;

import com.enioka.jqm.client.api.Deliverable;
import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JqmInvalidRequestException;
import com.enioka.jqm.client.api.State;
import com.enioka.jqm.client.jdbc.api.JqmClientFactory;
import com.enioka.jqm.configservices.DefaultConfigurationService;
import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.engine.api.lifecycle.JqmEngineOperations;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.updater.api.DbSchemaManager;
import com.enioka.jqm.tester.api.JqmAsynchronousTester;
import com.enioka.jqm.tester.api.TestJobDefinition;

import org.apache.commons.io.FilenameUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Actual implementation of {@link JqmAsynchronousTester}. Interface/implementation split only exists in order to allow easier interaction
 * from outside the OSGi world.
 */
@Component(service = JqmAsynchronousTester.class, scope = ServiceScope.SINGLETON, immediate = false)
public class JqmAsynchronousTesterOsgi implements JqmAsynchronousTester
{
    private Map<String, JqmEngineOperations> engines = new HashMap<>();
    private Map<String, Node> nodes = new HashMap<>();
    private Map<String, Integer> queues = new HashMap<>();

    private DbConn cnx = null;

    private boolean hasStarted = false;
    private String logLevel = "DEBUG";
    private boolean oneQueueDeployed = false;

    @Reference
    private ServiceReference<JqmEngineOperations> srEngine;
    private BundleContext bundleContext;

    // Just for dependency.
    @Reference
    InitialContext jndiInitialContext;

    @Activate
    public JqmAsynchronousTesterOsgi(@Reference ConfigurationAdmin configurationAdmin, @Reference DbSchemaManager dbSchemaManager,
            BundleContext bundleContext)
    {
        // Main resource is jdbc/jqm and uses a memory url, meaning db is created on first use.
        System.setProperty("com.enioka.jqm.resourceFiles", "resources_internal.xml");

        // Context
        this.bundleContext = bundleContext;

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
        resetAllData();
    }

    @Override
    public JqmAsynchronousTester addNode(String nodeName)
    {
        if (hasStarted)
        {
            throw new IllegalStateException("tester has already started");
        }

        File resDirectoryPath = Common.createTempDirectory();

        Node node = Node.create(cnx, nodeName, 12, resDirectoryPath.getAbsolutePath(), ".", resDirectoryPath.getAbsolutePath(), "test",
                logLevel);
        cnx.commit();
        nodes.put(nodeName, node);

        setNodesLogLevel(logLevel);

        return this;
    }

    @Override
    public JqmAsynchronousTester setNodesLogLevel(String level)
    {
        logLevel = level;
        cnx.runUpdate("node_update_all_log_level", level);
        cnx.commit();
        return this;
    }

    @Override
    public JqmAsynchronousTester addGlobalParameter(String key, String value)
    {
        GlobalParameter.setParameter(cnx, key, value);
        cnx.commit();
        return this;
    }

    @Override
    public JqmAsynchronousTester addQueue(String name)
    {
        if (hasStarted)
        {
            throw new IllegalStateException("tester has already started");
        }

        int q = Queue.create(cnx, name, "test queue", queues.size() == 0);
        cnx.commit();

        queues.put(name, q);

        return this;
    }

    @Override
    public JqmAsynchronousTester deployQueueToNode(String queueName, int maxJobsRunning, int pollingIntervallMs, String... nodeName)
    {
        if (hasStarted)
        {
            throw new IllegalStateException("tester has already started");
        }

        for (String name : nodeName)
        {
            DeploymentParameter.create(cnx, nodes.get(name).getId(), maxJobsRunning, pollingIntervallMs, queues.get(queueName));
            cnx.commit();
        }

        oneQueueDeployed = true;
        return this;
    }

    JqmAsynchronousTester addJobDefinition(TestJobDefinitionImpl description)
    {
        Common.createJobDef(cnx, description, queues);
        cnx.commit();
        return this;
    }

    @Override
    public JqmAsynchronousTester addSimpleJobDefinitionFromClasspath(Class<? extends Object> classToRun)
    {
        return createJobDefinitionFromClassPath(classToRun).addJobDefinition();
    }

    @Override
    public TestJobDefinition createJobDefinitionFromClassPath(Class<? extends Object> classToRun)
    {
        return TestJobDefinitionImpl.createFromClassPath(classToRun.getSimpleName(), "test payload " + classToRun.getName(), classToRun,
                this);
    }

    @Override
    public JqmAsynchronousTester addSimpleJobDefinitionFromLibrary(String name, String className, String jarPath)
    {
        return createJobDefinitionFromLibrary(name, className, jarPath).addJobDefinition();
    }

    @Override
    public TestJobDefinition createJobDefinitionFromLibrary(String name, String className, String jarPath)
    {
        return TestJobDefinitionImpl.createFromJar(name, name, className, jarPath, this);
    }

    @Override
    public JqmAsynchronousTester start()
    {
        if (hasStarted)
        {
            throw new IllegalStateException("cannot start twice");
        }
        if (nodes.isEmpty())
        {
            throw new IllegalStateException("no engines defined");
        }
        if (queues.isEmpty())
        {
            throw new IllegalStateException("no queues defined");
        }
        if (!oneQueueDeployed)
        {
            throw new IllegalStateException("no queue was ever deployed to any node");
        }
        hasStarted = true;

        ServiceObjects<JqmEngineOperations> so = this.bundleContext.getServiceObjects(srEngine);
        for (Node n : nodes.values())
        {
            JqmEngineOperations engine = so.getService();
            engine.start(n.getName(), null);
            engines.put(n.getName(), engine);
        }
        return this;
    }

    @Override
    public Long enqueue(String name)
    {
        return JqmClientFactory.getClient().newJobRequest(name, "test").enqueue();
    }

    @Override
    public void waitForResults(int nbResult, int timeoutMs, int waitAdditionalMs)
    {
        Calendar start = Calendar.getInstance();
        while (JqmClientFactory.getClient().newQuery().invoke().size() < nbResult
                && Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis() <= timeoutMs)
        {
            sleepms(100);
        }
        if (JqmClientFactory.getClient().newQuery().invoke().size() < nbResult)
        {
            throw new RuntimeException("expected result count was not reached in specified timeout");
        }
        sleepms(waitAdditionalMs);
    }

    private void sleepms(int ms)
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

    @Override
    public void waitForResults(int nbResult, int timeoutMs)
    {
        waitForResults(nbResult, timeoutMs, 0);
    }

    @Override
    public void stop()
    {
        if (hasStarted)
        {
            for (JqmEngineOperations op : this.engines.values())
            {
                op.stop();
            }
        }

        hasStarted = false;
        this.engines.clear();
    }

    @Deactivate
    public void deactivate()
    {
        if (cnx != null)
        {
            cnx.close();
        }
        if (srEngine != null)
        {
            bundleContext.ungetService(srEngine);
        }
        JqmClientFactory.resetClient();
    }

    @Override
    public void cleanupOperationalDbData()
    {
        cnx.runUpdate("deliverable_delete_all");
        cnx.runUpdate("message_delete_all");
        cnx.runUpdate("history_delete_all");
        cnx.runUpdate("jiprm_delete_all");
        cnx.runUpdate("ji_delete_all");
        cnx.commit();
    }

    @Override
    public void cleanupAllJobDefinitions()
    {
        cleanupOperationalDbData();

        cnx.runUpdate("jdprm_delete_all");
        cnx.runUpdate("jd_delete_all");
        cnx.runUpdate("cl_delete_all");
        cnx.commit();
    }

    @Override
    public void resetAllData()
    {
        stop();
        cleanupAllJobDefinitions();

        cnx.runUpdate("dp_delete_all");
        cnx.runUpdate("q_delete_all");
        cnx.runUpdate("node_delete_all");

        DefaultConfigurationService.updateConfiguration(cnx);
        cnx.runUpdate("q_delete_all"); // remove default queue created by DefaultConfigurationService
        GlobalParameter.setParameter(cnx, "defaultConnection", "");
        addGlobalParameter("disableWsApi", "true");
        addGlobalParameter("logFilePerLaunch", "false");

        this.queues.clear();
        this.nodes.clear();

        cnx.commit();
    }

    @Override
    public int getHistoryAllCount()
    {
        return JqmClientFactory.getClient().newQuery().invoke().size();
    }

    @Override
    public int getQueueAllCount()
    {
        return JqmClientFactory.getClient().newQuery().setQueryHistoryInstances(false).setQueryLiveInstances(true).invoke().size();
    }

    @Override
    public int getOkCount()
    {
        return JqmClientFactory.getClient().newQuery().addStatusFilter(State.ENDED).invoke().size();
    }

    @Override
    public int getNonOkCount()
    {
        return JqmClientFactory.getClient().newQuery().addStatusFilter(State.CRASHED).addStatusFilter(State.KILLED).invoke().size();
    }

    @Override
    public boolean testOkCount(long expectedOkCount)
    {
        return getOkCount() == expectedOkCount;
    }

    @Override
    public boolean testKoCount(long expectedKoCount)
    {
        return getNonOkCount() == expectedKoCount;
    }

    @Override
    public boolean testCounts(long expectedOkCount, long expectedKoCount)
    {
        return testOkCount(expectedOkCount) && testKoCount(expectedKoCount);
    }

    @Override
    public InputStream getDeliverableContent(Deliverable file) throws FileNotFoundException
    {
        List<com.enioka.jqm.model.Deliverable> dd = com.enioka.jqm.model.Deliverable.select(cnx, "deliverable_select_by_id", file.getId());
        if (dd.isEmpty())
        {
            throw new JqmInvalidRequestException("no deliverable with this ID");
        }

        com.enioka.jqm.model.Deliverable d = dd.get(0);
        JobInstance ji = JqmClientFactory.getClient().newQuery().setJobInstanceId(d.getJobId()).invoke().get(0);
        String nodeName = ji.getNodeName();
        Node n = nodes.get(nodeName);

        return new FileInputStream(FilenameUtils.concat(n.getDlRepo(), file.getFilePath()));
    }
}
