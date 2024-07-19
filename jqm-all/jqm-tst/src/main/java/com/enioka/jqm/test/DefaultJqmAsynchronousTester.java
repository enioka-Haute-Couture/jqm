package com.enioka.jqm.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.MetaInfServices;

import com.enioka.jqm.client.api.Deliverable;
import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JqmClientFactory;
import com.enioka.jqm.client.api.JqmInvalidRequestException;
import com.enioka.jqm.client.api.State;
import com.enioka.jqm.engine.api.lifecycle.JqmEngineOperations;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.jndi.api.JqmJndiContextControlService;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.updater.api.DbSchemaManager;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;
import com.enioka.jqm.test.api.JqmAsynchronousTester;
import com.enioka.jqm.test.api.TestJobDefinition;

/**
 * Actual implementation of {@link JqmAsynchronousTester}.
 */
@MetaInfServices(JqmAsynchronousTester.class)
public class DefaultJqmAsynchronousTester implements JqmAsynchronousTester
{
    private Map<String, JqmEngineOperations> engines = new HashMap<>();
    private Map<String, Node> nodes = new HashMap<>();
    private Map<String, Long> queues = new HashMap<String, Long>();

    private DbConn cnx = null;

    private boolean hasStarted = false;
    private String logLevel = "DEBUG";
    private boolean oneQueueDeployed = false;

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTION
    ///////////////////////////////////////////////////////////////////////////

    public DefaultJqmAsynchronousTester()
    {
        // Main resource is jdbc/test and uses a memory url, meaning db is created on first use.
        System.setProperty("com.enioka.jqm.resourceFiles", "resources.xml,resources_internal.xml");
        System.setProperty("com.enioka.jqm.cl.allow_system_cl", "true");

        // JNDI substrate
        ServiceLoaderHelper.getService(ServiceLoader.load(JqmJndiContextControlService.class)).registerIfNeeded();

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

        resetAllData();
    }

    /**
     * Equivalent to simply calling the constructor. Present for consistency.
     */
    public static JqmAsynchronousTester create()
    {
        return new DefaultJqmAsynchronousTester();
    }

    @Override
    public JqmAsynchronousTester createSingleNodeOneQueue()
    {
        return this.addNode("node1").addQueue("queue1").deployQueueToNode("queue1", 10, 100, "node1");
    }

    //////////////////////////////////////////////////////////////////////////
    // TEST PREPARATION
    ///////////////////////////////////////////////////////////////////////////

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

        Long q = Queue.create(cnx, name, "test queue", queues.size() == 0);
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

    ///////////////////////////////////////////////////////////////////////////
    // DURING TEST
    ///////////////////////////////////////////////////////////////////////////

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

        for (Node n : nodes.values())
        {
            JqmEngineOperations engine = ServiceLoaderHelper.getService(ServiceLoader.load(JqmEngineOperations.class), false);
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
        this.queues.clear();
        this.nodes.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // CLEANUP
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close()
    {
        stop();
        if (cnx != null)
        {
            cnx.close();
            cnx = null;
        }
        JqmClientFactory.reset();
    }

    @Override
    public void cleanupOperationalDbData()
    {
        Common.cleanupOperationalDbData(cnx);
    }

    @Override
    public void cleanupAllJobDefinitions()
    {
        Common.cleanupAllJobDefinitions(cnx);
    }

    @Override
    public void resetAllData()
    {
        stop();
        Common.resetAllData(cnx);
    }

    ///////////////////////////////////////////////////////////////////////////
    // TEST RESULT ANALYSIS HELPERS
    ///////////////////////////////////////////////////////////////////////////

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
