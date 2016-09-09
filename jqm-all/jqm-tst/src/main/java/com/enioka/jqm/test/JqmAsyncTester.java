package com.enioka.jqm.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.io.FilenameUtils;
import org.hsqldb.Server;

import com.enioka.jqm.api.Deliverable;
import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClient;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.State;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.JqmEngineOperations;
import com.enioka.jqm.tools.Main;

/**
 * An asynchronous tester for JQM payloads. It allows to configure and start one or more embedded JQM engines and run payloads against them.
 * It is most suited for integration tests.<br>
 * <br>
 * It starts full JQM nodes running on an in-memory embedded database. They are started with all web API disabled.<br>
 * The user should handle interactions with the nodes through the normal client APIs. See {@link JqmClient} and {@link JqmClientFactory}. As
 * the web services are not loaded, the file retrieval methods of these APIs will not work, so the tester provides a
 * {@link #getDeliverableContent(Deliverable)} method to compensate. The tester also provides a few helper methods (accelerators) that
 * encapsulate the client API.<br>
 * 
 * If using resources (JNDI), they must be put inside a resource.xml file at the root of class loader search.<br>
 * Note that tester instances are not thread safe.
 */
public class JqmAsyncTester
{
    private Map<String, JqmEngineOperations> engines = new HashMap<String, JqmEngineOperations>();
    private Map<String, Node> nodes = new HashMap<String, Node>();
    private Map<String, Queue> queues = new HashMap<String, Queue>();

    private EntityManagerFactory emf = null;
    private EntityManager em = null;
    private Server s = null;

    private boolean hasStarted = false;
    private String logLevel = "DEBUG";
    private boolean oneQueueDeployed = false;

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTION
    ///////////////////////////////////////////////////////////////////////////

    public JqmAsyncTester()
    {
        // Ext dir
        File extDir = new File("./ext");
        if (!extDir.exists() && !extDir.mkdir())
        {
            throw new RuntimeException(new IOException("./ext directory does not exist and cannot create it"));
        }

        s = Common.createHsqlServer();
        s.start();

        emf = Persistence.createEntityManagerFactory("jobqueue-api-pu", Common.jpaProperties(s));
        em = emf.createEntityManager();

        Properties p2 = new Properties();
        p2.put("emf", emf);
        JqmClientFactory.setProperties(p2);
        Main.setEmf(emf);

        // Minimum parameters
        Main.main(new String[] { "-u" });
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Queue").executeUpdate();
        em.getTransaction().commit();

        // Needed parameters
        addGlobalParameter("defaultConnection", "");
        addGlobalParameter("disableWsApi", "true");
    }

    /**
     * Equivalent to simply calling the constructor. Present for consistency.
     */
    public static JqmAsyncTester create()
    {
        return new JqmAsyncTester();
    }

    /**
     * A helper method which creates a preset environment with a single node called 'node1' and a single queue named 'queue1' being polled
     * every 100ms by the node with at most 10 parallel running job instances..
     */
    public static JqmAsyncTester createSingleNodeOneQueue()
    {
        return new JqmAsyncTester().addNode("node1").addQueue("queue1").deployQueueToNode("queue1", 10, 100, "node1");
    }

    ///////////////////////////////////////////////////////////////////////////
    // TEST PREPARATION
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a new node. It is not started by this method.<br>
     * This must be called before starting the tester.
     * 
     * @param nodeName
     *            the name of the node. Must be unique.
     */
    public JqmAsyncTester addNode(String nodeName)
    {
        if (hasStarted)
        {
            throw new IllegalStateException("tester has already started");
        }

        File resDirectoryPath = Common.createTempDirectory();
        Node node = new Node();
        node.setDlRepo(resDirectoryPath.getAbsolutePath());
        node.setDns("test");
        node.setName(nodeName);
        node.setRepo(".");
        node.setTmpDirectory(resDirectoryPath.getAbsolutePath());
        node.setPort(12);
        node.setRootLogLevel(logLevel);

        em.getTransaction().begin();
        em.persist(node);
        em.getTransaction().commit();
        nodes.put(nodeName, node);

        return this;
    }

    /**
     * Changes the log level of existing and future nodes.
     * 
     * @param level
     *            TRACE, DEBUG, INFO, WARNING, ERROR (or anything, which is interpreted as INFO)
     */
    public JqmAsyncTester setNodesLogLevel(String level)
    {
        logLevel = level;
        em.getTransaction().begin();
        em.createQuery("UPDATE Node n set n.rootLogLevel = :l").setParameter("l", level).executeUpdate();
        em.getTransaction().commit();
        return this;
    }

    /**
     * Create a new queue. After creation, it is not polled by any node - see {@link #deployQueueToNode(String, int, int, String...)} for
     * this.<br>
     * The first queue created is considered to be the default queue.<br>
     * This must be called before starting the engines.
     * 
     * @param name
     *            must be unique.
     */
    public JqmAsyncTester addQueue(String name)
    {
        if (hasStarted)
        {
            throw new IllegalStateException("tester has already started");
        }

        Queue q = new Queue();
        q.setName(name);
        q.setDescription("test queue");
        if (queues.size() == 0)
        {
            q.setDefaultQueue(true);
        }

        em.getTransaction().begin();
        em.persist(q);
        em.getTransaction().commit();
        queues.put(name, q);

        return this;
    }

    /**
     * Set one or more nodes to poll a queue for new job instances.<br>
     * This must be called before starting the engines.
     */
    public JqmAsyncTester deployQueueToNode(String queueName, int maxJobsRunning, int pollingIntervallMs, String... nodeName)
    {
        if (hasStarted)
        {
            throw new IllegalStateException("tester has already started");
        }

        for (String name : nodeName)
        {
            DeploymentParameter dp = new DeploymentParameter();
            dp.setNbThread(maxJobsRunning);
            dp.setNode(nodes.get(name));
            dp.setPollingInterval(pollingIntervallMs);
            dp.setQueue(queues.get(queueName));

            em.getTransaction().begin();
            em.persist(dp);
            em.getTransaction().commit();
        }

        oneQueueDeployed = true;
        return this;
    }

    /**
     * Set or update a global parameter.
     */
    public void addGlobalParameter(String key, String value)
    {
        em.getTransaction().begin();
        int i = em.createQuery("UPDATE GlobalParameter p SET p.value = :val WHERE p.key = :key").setParameter("key", key)
                .setParameter("val", value).executeUpdate();

        if (i == 0)
        {
            GlobalParameter gp = new GlobalParameter();
            gp.setKey(key);
            gp.setValue(value);
            em.persist(gp);
        }
        em.getTransaction().commit();
    }

    /**
     * Add a new job definition (see documentation) to the database.<br>
     * This can be called at any time (even after engine(s) start).
     */
    public JqmAsyncTester addJobDefinition(TestJobDefinition description)
    {
        JobDef jd = Common.createJobDef(description, queues);
        em.getTransaction().begin();
        em.persist(jd);
        em.getTransaction().commit();

        return this;
    }

    /**
     * A helper method to create a job definition from a class <strong>which is present inside the current class path</strong>.<br>
     * The job description and name will be the class name (simple name, not the fully qualified name).<br>
     * If you need further customisation, directly create your {@link TestJobDefinition} and call
     * {@link #addJobDefinition(TestJobDefinition)} instead of using this method.
     * 
     * @param classToRun
     *            a class present inside the class path which should be launched by JQM.
     * @return the tester itself to allow fluid API behaviour.
     */
    public JqmAsyncTester addSimpleJobDefinitionFromClasspath(Class<? extends Object> classToRun)
    {
        TestJobDefinition jd = TestJobDefinition.createFromClassPath(classToRun.getSimpleName(), "test payload " + classToRun.getName(),
                classToRun);
        return this.addJobDefinition(jd);
    }

    /**
     * A helper method to create a job definition from a class <strong>which is present inside an existing jar file</strong>.<br>
     * The job description and name will be identical<br>
     * If you need further customisation, directly create your {@link TestJobDefinition} and call
     * {@link #addJobDefinition(TestJobDefinition)} instead of using this method.
     * 
     * @param name
     *            name of the new job definition (as used in the enqueue methods)
     * @param className
     *            the full canonical name of the the class to run inside the jar
     * @param jarPath
     *            path to the jar. Relative to current directory.
     * @return the tester itself to allow fluid API behaviour.
     */
    public JqmAsyncTester addSimpleJobDefinitionFromLibrary(String name, String className, String jarPath)
    {
        TestJobDefinition jd = TestJobDefinition.createFromJar(name, name, className, jarPath);
        return this.addJobDefinition(jd);
    }

    ///////////////////////////////////////////////////////////////////////////
    // DURING TEST
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This actually starts the different engines configured with {@link #addNode(String)}.<br>
     * This can usually only be called once (it can actually be called again but only after calling {@link #stop()}).
     */
    public JqmAsyncTester start()
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
            engines.put(n.getName(), Main.startEngine(n.getName()));
        }
        return this;
    }

    /**
     * Helper method to enqueue a new launch request. Simple JqmClientFactory.getClient().enqueue wrapper.
     * 
     * @return the request ID.
     */
    public int enqueue(String name)
    {
        return JqmClientFactory.getClient().enqueue(JobRequest.create(name, "test"));
    }

    /**
     * Wait for a given amount of ended job instances (OK or KO).
     * 
     * @param nbResult
     *            the expected result count
     * @param timeoutMs
     *            give up after this (throws a RuntimeException)
     * @param waitAdditionalMs
     *            after reaching the expected nbResult count, wait a little more (for example to ensure there is no additonal unwanted
     *            launch). Will usually be zero.
     */
    public void waitForResults(int nbResult, int timeoutMs, int waitAdditionalMs)
    {
        Calendar start = Calendar.getInstance();
        while (Query.create().run().size() < nbResult && Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis() <= timeoutMs)
        {
            sleepms(100);
        }
        if (Query.create().run().size() < nbResult)
        {
            throw new RuntimeException("expected result count was not reached in specified timeout");
        }
        sleepms(waitAdditionalMs);
    }

    /**
     * Wait for a given amount of ended job instances (OK or KO). Shortcut for {@link #waitForResults(int, int, int)} with 0ms of additional
     * wait time.
     * 
     * @param nbResult
     *            the expected result count
     * @param timeoutMs
     *            give up after this (throws a RuntimeException)
     */
    public void waitForResults(int nbResult, int timeoutMs)
    {
        waitForResults(nbResult, timeoutMs, 0);
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

    /**
     * Stops all engines. Only returns when engines are fully stopped.
     */
    public void stop()
    {
        if (!hasStarted)
        {
            throw new IllegalStateException("cannot stop a tester which has not started");
        }
        for (JqmEngineOperations op : this.engines.values())
        {
            op.stop();
        }
        JqmClientFactory.resetClient();
        // No need to close EM & EMF - they were closed by the client reset.
        s.stop();
        waitDbStop();
        hasStarted = false;
        this.engines.clear();
    }

    private void waitDbStop()
    {
        while (s.getState() != 16)
        {
            this.sleepms(1);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CLEANUP
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Removes all job instances from the queues and the history.
     * 
     * @param em
     */
    public void cleanupOperationalDbData()
    {
        em.getTransaction().begin();
        em.createQuery("DELETE Deliverable WHERE 1=1").executeUpdate();
        em.createQuery("DELETE Message WHERE 1=1").executeUpdate();
        em.createQuery("DELETE History WHERE 1=1").executeUpdate();
        em.createQuery("DELETE RuntimeParameter WHERE 1=1").executeUpdate();
        em.createQuery("DELETE JobInstance WHERE 1=1").executeUpdate();

        em.getTransaction().commit();
    }

    /**
     * Deletes all job definitions. This calls {@link #cleanupOperationalDbData()}
     * 
     * @param em
     */
    public void cleanupAllJobDefinitions()
    {
        cleanupOperationalDbData();

        em.getTransaction().begin();
        em.createQuery("DELETE JobDefParameter WHERE 1=1").executeUpdate();
        em.createQuery("DELETE JobDef WHERE 1=1").executeUpdate();
        em.getTransaction().commit();
    }

    ///////////////////////////////////////////////////////////////////////////
    // TEST RESULT ANALYSIS HELPERS
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Helper query (directly uses {@link Query}). Gives the count of all ended (KO and OK) job instances.
     */
    public int getHistoryAllCount()
    {
        return Query.create().run().size();
    }

    /**
     * Helper query (directly uses {@link Query}). Gives the count of all non-ended (waiting in queue, running...) job instances.
     */
    public int getQueueAllCount()
    {
        return Query.create().setQueryHistoryInstances(false).setQueryLiveInstances(true).run().size();
    }

    /**
     * Helper query (directly uses {@link Query}). Gives the count of all OK-ended job instances.
     */
    public int getOkCount()
    {
        return Query.create().addStatusFilter(State.ENDED).run().size();
    }

    /**
     * Helper query (directly uses {@link Query}). Gives the count of all non-OK-ended job instances.
     */
    public int getNonOkCount()
    {
        return Query.create().addStatusFilter(State.CRASHED).addStatusFilter(State.KILLED).run().size();
    }

    /**
     * Helper method. Tests if {@link #getOkCount()} is equal to the given parameter.
     */
    public boolean testOkCount(long expectedOkCount)
    {
        return getOkCount() == expectedOkCount;
    }

    /**
     * Helper method. Tests if {@link #getNonOkCount()} is equal to the given parameter.
     */
    public boolean testKoCount(long expectedKoCount)
    {
        return getNonOkCount() == expectedKoCount;
    }

    /**
     * Helper method. Tests if {@link #getOkCount()} is equal to the first parameter and if {@link #getNonOkCount()} is equal to the second
     * parameter.
     */
    public boolean testCounts(long expectedOkCount, long expectedKoCount)
    {
        return testOkCount(expectedOkCount) && testKoCount(expectedKoCount);
    }

    /**
     * Version of {@link JqmClient#getDeliverableContent(Deliverable)} which does not require the web service APIs to be enabled to work.
     * Also, returned files do not self-destruct on stream close.<br>
     * See the javadoc of the original method for details.
     * 
     * @throws FileNotFoundException
     */
    public InputStream getDeliverableContent(Deliverable file) throws FileNotFoundException
    {
        com.enioka.jqm.jpamodel.Deliverable d = em
                .createQuery("SELECT d FROM Deliverable d WHERE d.id = :i", com.enioka.jqm.jpamodel.Deliverable.class)
                .setParameter("i", file.getId()).getSingleResult();
        JobInstance ji = Query.create().setJobInstanceId(d.getJobId()).run().get(0);
        String nodeName = ji.getNodeName();
        Node n = nodes.get(nodeName);

        return new FileInputStream(FilenameUtils.concat(n.getDlRepo(), file.getFilePath()));
    }
}
