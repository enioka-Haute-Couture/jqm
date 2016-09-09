Testing payloads
#######################

.. highlight:: java

Unit testing
****************

By unit testing, we mean here running a single payload inside a JUnit test or any other form of test (including a 'main' Java program) without
needing a full JQM engine.

JQM provides a library named jqm-tst which allows tests that will run a **single job instance** in a stripped-down synchronous version of an embedded JQM engine requiring no configuration.
The engine is destroyed immediately after the run.
Single job instance also has for consequence that if your job enqueues new execution requests these will be ignored.

An example taken from JQM's own unit tests::

    @Test
    public void testOne()
    {
        JobInstance res = com.enioka.jqm.test.JqmTester.create("com.enioka.jqm.test.Payload1").addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

Here, we just have to give the class of the payload, optionally parameters and that's it. The result returned is from the client API.

Refer to JqmTester javadoc for further details, including how to specify JNDI resource if needed.

Integration tests
************************

If you have to test interactions between jobs (for example, one job instance queueing another), it may be necessary to use a full JQM engine. JQM provides another embedded 
tester class to do so. It is inside the same jqm-tst library.

These are the steps to follow to launch an integration test: 

* create the tester object
* add at least on node (engine)
* add at least one queue
* deploy a queue on a node (i.e. set a node to poll a queue)
* start the engines
* create a job definition (the equivalent of the deployment descriptor XML file, which describes where the class to launch is, its parameters...)
* launch a new job instance and other normal JQM client interactions using the client API.
* stop the engines.

When using test frameworks like JUnit, all the node creation stuff is usually inside @BeforeClass methods, like in the following example.::

    public class MyIntegrationTest
    {
        public static JqmAsyncTester tester;

        @BeforeClass
        public static void beforeClass()
        {
            // This creates a cluster with two JQM nodes, three queues (queue1 polled by node1, queue2 polled by node2, queue3 polled by both nodes).
            // The nodes are started at the end of this line.
            tester = JqmAsyncTester.create().addNode("node1").addNode("node2").addQueue("queue1").addQueue("queue2").addQueue("queue3")
                    .deployQueueToNode("queue1", 10, 100, "node1").deployQueueToNode("queue2", 10, 100, "node2")
                    .deployQueueToNode("queue3", 10, 100, "node1", "node2").start();
        }

        @AfterClass
        public static void afterClass()
        {
            // Only stop the cluster when all tests are done. This means there is no reboot or cleanup between tests if tester.cleanupAllJobDefinitions() is not explicitely called.
            tester.stop();
        }
        
        @Before
        public void before()
        {
            // A helper method to ensure there is no traces left of previous executions and job definitions from other tests
             tester.cleanupAllJobDefinitions();
        }

        @Test
        public void testOne()
        {
            // Quickly create a job definition from a class present in the test class path.
            tester.addSimpleJobDefinitionFromClasspath(Payload1.class);

            // Request a launch of this new job definition. Note we could simply use the JqmClient API.
            tester.enqueue("Payload1");
            
            // Launches are asynchronous, so wait for results (with a timeout).
            tester.waitForResults(1, 10000);

            // Actual tests
            Assert.assertEquals(1, tester.getOkCount());
            Assert.assertEquals(1, tester.getHistoryAllCount());
        }
        
        @Test
        public void testTwo()
        {
            // Quickly create a job definition from a class present in a jar. This is the way production JQM nodes really work - they load jar stored on the local file system. 
            tester.addSimpleJobDefinitionFromLibrary("payload1", "App", "../jqm-tests/jqm-test-datetimemaven/target/test.jar")

            tester.enqueue("payload1");
            tester.waitForResults(1, 10000, 0);

            Assert.assertTrue(tester.testCounts(1, 0));       
        }
    }

Refer to JqmAsyncTester javadoc for further details, including how to specify JNDI resource and retrieving files created by the job instances.

.. note:: the tester outputs logs on stdout using log4j. You can set set log level through a tester method. If you use other loggers, this may result in a mix of different logger outputs.

.. warning:: the nodes run inside the current JVM. So if you start too many nodes, or allow too many concurrent jobs to run, you may run out of memory and need to set higher JVM -Xmx parameters.
    If using Maven, you may for example set the environment variable `export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m"`
