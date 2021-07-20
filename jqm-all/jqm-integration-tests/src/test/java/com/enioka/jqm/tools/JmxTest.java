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

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.pki.JdbcCa;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JmxTest extends JqmBaseTest
{

    /**
     * Test registration of a remote JMX using default settings (no SSL) and test
     * connection to this remote JMX.
     */
    @Test
    public void jmxRemoteTest() throws Exception
    {
        JmxAgent.unregisterAgent();

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42, "KillApp", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        int i = JobRequest.create("KillApp", "TestUser").submit();

        // Set JMX server on free ports
        ServerSocket s1 = new ServerSocket(0);
        int port1 = s1.getLocalPort();
        ServerSocket s2 = new ServerSocket(0);
        int port2 = s2.getLocalPort();
        s1.close();
        s2.close();
        String hn = InetAddress.getLocalHost().getHostName();

        cnx.runUpdate("node_update_jmx_by_id", port1, port2, TestHelpers.node.getId());
        cnx.commit();

        // Go
        addAndStartEngine();
        TestHelpers.waitForRunning(1, 10000, cnx);
        this.sleep(1); // time to actually run, not only Loader start.

        // Connect to JMX server
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hn + ":" + port1 + "/jndi/rmi://" + hn + ":" + port2 + "/jmxrmi");
        JMXConnector cntor = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = cntor.getMBeanServerConnection();
        int count = mbsc.getMBeanCount();
        System.out.println(count);

        String[] domains = mbsc.getDomains();
        System.out.println("*** domains:");
        for (String d : domains)
        {
            System.out.println(d);
        }
        Set<ObjectInstance> mbeans = mbsc.queryMBeans(new ObjectName("com.enioka.jqm:*"), null);
        System.out.println("*** beans in com.enioka.jqm:*: ");
        for (ObjectInstance oi : mbeans)
        {
            System.out.println(oi.getObjectName());
        }
        Assert.assertTrue(mbeans.size() >= 5);
        // 1 node, 3 pollers, 1 running instance, 1 JDBC pool. The pool may not be
        // visible due to a call to resetSingletons.

        // /////////////////
        // Loader beans
        ObjectName killBean = new ObjectName("com.enioka.jqm:type=Node.Queue.JobInstance,Node=" + TestHelpers.node.getName() + ",Queue=VIPQueue,name=" + i);
        System.out.println("Name to kill: " + killBean.toString());
        mbeans = mbsc.queryMBeans(killBean, null);
        if (mbeans.isEmpty())
        {
            Assert.fail("could not find the JMX Mbean of the launched job");
        }
        JavaJobInstanceTrackerMBean proxy = JMX.newMBeanProxy(mbsc, killBean, JavaJobInstanceTrackerMBean.class);
        Assert.assertEquals("KillApp", proxy.getApplicationName());
        Assert.assertEquals((Integer) i, proxy.getId());
        Assert.assertEquals("TestUser", proxy.getUser());

        // Elements that are not set or testable reproductibly, but should not raise any
        // exception
        proxy.getEnqueueDate();
        proxy.getKeyword1();
        proxy.getKeyword2();
        proxy.getKeyword3();
        proxy.getModule();
        proxy.getRunTimeSeconds();
        proxy.getRunTimeSeconds(); // Twice for all code paths.
        proxy.getSessionId();

        // Kill it though JMX
        proxy.kill();
        Thread.sleep(4000);

        // //////////////////
        // Engine bean
        ObjectName engine = new ObjectName("com.enioka.jqm:type=Node,name=" + TestHelpers.node.getName());
        JqmEngineMBean proxyEngine = JMX.newMBeanProxy(mbsc, engine, JqmEngineMBean.class);
        Assert.assertEquals(1, proxyEngine.getCumulativeJobInstancesCount() + proxyEngine.getCurrentlyRunningJobCount());
        Assert.assertTrue(proxyEngine.getUptime() > 0);
        proxyEngine.getVersion();
        Assert.assertTrue(proxyEngine.isAllPollersPolling());
        Assert.assertTrue(!proxyEngine.isFull());

        // //////////////////
        // Poller bean
        ObjectName poller = new ObjectName("com.enioka.jqm:type=Node.Queue,Node=" + TestHelpers.node.getName() + ",name=VIPQueue");
        QueuePollerMBean proxyPoller = JMX.newMBeanProxy(mbsc, poller, QueuePollerMBean.class);
        Assert.assertEquals(1, proxyPoller.getCumulativeJobInstancesCount() + proxyPoller.getCurrentActiveThreadCount());
        proxyPoller.getCurrentlyRunningJobCount();
        proxyPoller.getJobsFinishedPerSecondLastMinute();
        Assert.assertEquals((Integer) 40, proxyPoller.getMaxConcurrentJobInstanceCount());
        Assert.assertEquals((Integer) 1, proxyPoller.getPollingIntervalMilliseconds());

        Assert.assertTrue(proxyPoller.isActuallyPolling());
        Assert.assertTrue(!proxyPoller.isFull());

        proxyPoller.stop();

        // Done
        cntor.close();
    }

    /**
     * Test registration of a remote JMX with or without SSL and users
     * authentication and test connection to this remote JMX with or without a trust
     * store (containing the certificate of the internal PKI certification authority
     * which signed the certificate of the remote JMX) and a key store (containing
     * the certificate of a test user trusted by the previous certification
     * authority).
     * 
     * Each test must be executed in its own JVM. Indeed, the connection to JMX uses
     * the default SSL Context manager because it doesn't seem that it is (anymore
     * ?) possible to use a custom one despite informations that we can find on the
     * internet. This default SSL Context saves trust and key stores in cache and
     * never reloads the files where they come from. This cache is lost when using a
     * new JVM. That is the reason why this test must be executed in its own JVM.
     * 
     * @param testInstance        the instance of the running test
     * @param enableJmxSsl        = true if the test must register the remote JMX
     *                            Agent with SSL enabled
     * @param enableJmxSslAuth    = true if the test must register the remote JMX
     *                            Agent with SSL authentication
     * @param useClientTrustStore = true if the test must use the valid client trust
     *                            store when connecting to the JMX Agent "remotely"
     * @param useClientKeyStore   = true if the test must use the valid client key
     *                            store when connecting to the JMX Agent "remotely"
     * @throws Exception
     */
    public static void jmxRemoteSslTest(JqmBaseTest testInstance, boolean enableJmxSsl, boolean enableJmxSslAuth, boolean useClientTrustStore, boolean useClientKeyStore)
            throws Exception
    {
        DbConn cnx = testInstance.cnx;
        JmxAgent.unregisterAgent();

        System.out.println("Starting a JMX Remote SSL Test with enableJmxSsl: " + enableJmxSsl + ", enableJmxSslAuth: " + enableJmxSslAuth + ", useClientTrustStore: "
                + useClientTrustStore + ", useClientKeyStore: " + useClientKeyStore);
        Helpers.setSingleParam("enableJmxSsl", Boolean.toString(enableJmxSsl), cnx);
        Helpers.setSingleParam("enableJmxSslAuth", Boolean.toString(enableJmxSslAuth), cnx);

        String userName = "testuser";
        Helpers.createUserIfMissing(cnx, userName, "password", "test user", "client read only");

        // System.setProperty("javax.net.debug", "all");

        // From JmxTest#jmxRemoteTest method:
        // Set JMX server on free ports
        ServerSocket s1 = new ServerSocket(0);
        int port1 = s1.getLocalPort();
        ServerSocket s2 = new ServerSocket(0);
        int port2 = s2.getLocalPort();
        s1.close();
        s2.close();
        String hn = InetAddress.getLocalHost().getHostName();

        cnx.runUpdate("node_update_jmx_by_id", port1, port2, TestHelpers.node.getId());
        cnx.commit();

        // Go
        testInstance.addAndStartEngine();
        TestHelpers.waitForRunning(1, 10000, cnx);
        testInstance.sleep(1); // time to actually run, not only Loader start.

        // Connect to JMX server
        // following instructions of
        // https://db.apache.org/derby/docs/10.9/adminguide/radminjmxcode.html#radminjmxcode__connmbeanserver
        // and
        // https://www.cleantutorials.com/jconsole/jconsole-ssl-with-password-authentication:
        HashMap<String, Object> env = new HashMap<String, Object>();
        env.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());

        String pfxPassword = GlobalParameter.getParameter(cnx, "pfxPassword", "SuperPassword");

        // From JettyTest class:
        JdbcCa.prepareClientStore(cnx, "CN=" + userName, "./conf/client.pfx", pfxPassword, "client-cert", "./conf/client.cer");

        String trustStorePath = "./conf/trusted.jks";
        String keyStorePath = "./conf/client.pfx";

        // System properties are JVM related, they are lost at the end of execution
        // (https://stackoverflow.com/questions/21204334/system-setproperty-and-system-getproperty):
        if (useClientTrustStore)
        {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", pfxPassword);
        }
        else
        {
            System.clearProperty("javax.net.ssl.trustStore");
            System.clearProperty("javax.net.ssl.trustStorePassword");
        }

        if (useClientKeyStore)
        {
            System.setProperty("javax.net.ssl.keyStore", keyStorePath);
            System.setProperty("javax.net.ssl.keyStorePassword", pfxPassword);
        }
        else
        {
            System.clearProperty("javax.net.ssl.keyStore");
            System.clearProperty("javax.net.ssl.keyStorePassword");
        }

        // From JmxTest#jmxRemoteTest method:
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hn + ":" + port1 + "/jndi/rmi://" + hn + ":" + port2 + "/jmxrmi");
        JMXConnector cntor = JMXConnectorFactory.connect(url, env);
        MBeanServerConnection mbsc = cntor.getMBeanServerConnection();

        int count = mbsc.getMBeanCount();
        System.out.println(count);

        // Create the job only if the connection to "remote" JMX is succesful.
        // Otherwise, if the job is created and the remote JMX doesn't receive the kill
        // order because the connection isn't established, the job takes a lot of time
        // to stop by himself.
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42, "KillApp", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        int i = JobRequest.create("KillApp", "TestUser").submit();

        TestHelpers.waitForRunning(1, 10000, cnx);
        testInstance.sleep(1); // time to actually run, not only Loader start.

        String[] domains = mbsc.getDomains();
        System.out.println("*** domains:");
        for (String d : domains)
        {
            System.out.println(d);
        }
        Set<ObjectInstance> mbeans = mbsc.queryMBeans(new ObjectName("com.enioka.jqm:*"), null);
        System.out.println("*** beans in com.enioka.jqm:*: ");
        for (ObjectInstance oi : mbeans)
        {
            System.out.println(oi.getObjectName());
        }
        Assert.assertTrue(mbeans.size() >= 5);
        // 1 node, 3 pollers, 1 running instance, 1 JDBC pool. The pool may not be
        // visible due to a call to resetSingletons.

        // /////////////////
        // Loader beans
        ObjectName killBean = new ObjectName("com.enioka.jqm:type=Node.Queue.JobInstance,Node=" + TestHelpers.node.getName() + ",Queue=VIPQueue,name=" + i);
        System.out.println("Name to kill: " + killBean.toString());
        mbeans = mbsc.queryMBeans(killBean, null);
        if (mbeans.isEmpty())
        {
            Assert.fail("could not find the JMX Mbean of the launched job");
        }
        JavaJobInstanceTrackerMBean proxy = JMX.newMBeanProxy(mbsc, killBean, JavaJobInstanceTrackerMBean.class);
        Assert.assertEquals("KillApp", proxy.getApplicationName());
        Assert.assertEquals((Integer) i, proxy.getId());
        Assert.assertEquals("TestUser", proxy.getUser());

        // Elements that are not set or testable reproductibly, but should not raise any
        // exception
        proxy.getEnqueueDate();
        proxy.getKeyword1();
        proxy.getKeyword2();
        proxy.getKeyword3();
        proxy.getModule();
        proxy.getRunTimeSeconds();
        proxy.getRunTimeSeconds(); // Twice for all code paths.
        proxy.getSessionId();

        // Kill it though JMX
        proxy.kill();
        Thread.sleep(4000);

        // //////////////////
        // Engine bean
        ObjectName engine = new ObjectName("com.enioka.jqm:type=Node,name=" + TestHelpers.node.getName());
        JqmEngineMBean proxyEngine = JMX.newMBeanProxy(mbsc, engine, JqmEngineMBean.class);
        Assert.assertEquals(1, proxyEngine.getCumulativeJobInstancesCount() + proxyEngine.getCurrentlyRunningJobCount());
        Assert.assertTrue(proxyEngine.getUptime() > 0);
        proxyEngine.getVersion();
        Assert.assertTrue(proxyEngine.isAllPollersPolling());
        Assert.assertTrue(!proxyEngine.isFull());

        // //////////////////
        // Poller bean
        ObjectName poller = new ObjectName("com.enioka.jqm:type=Node.Queue,Node=" + TestHelpers.node.getName() + ",name=VIPQueue");
        QueuePollerMBean proxyPoller = JMX.newMBeanProxy(mbsc, poller, QueuePollerMBean.class);

        Assert.assertEquals(1, proxyPoller.getCumulativeJobInstancesCount() + proxyPoller.getCurrentActiveThreadCount());
        proxyPoller.getCurrentlyRunningJobCount();
        proxyPoller.getJobsFinishedPerSecondLastMinute();
        Assert.assertEquals((Integer) 40, proxyPoller.getMaxConcurrentJobInstanceCount());
        Assert.assertEquals((Integer) 1, proxyPoller.getPollingIntervalMilliseconds());

        Assert.assertTrue(proxyPoller.isActuallyPolling());
        Assert.assertTrue(!proxyPoller.isFull());

        proxyPoller.stop();

        // Done
        cntor.close();
    }

    /**
     * Test registration of a remote JMX using SSL and authentication of users for
     * connections and test connection to this remote JMX with a client having valid
     * stuff to connect (the client trusts the server and the server trusts him).
     */
    @Test
    public void jmxRemoteSslWithAuthWithTrustStoreAndKeyStoreTest() throws Exception
    {
        jmxRemoteSslTest(this, true, true, true, true);
    }

}
