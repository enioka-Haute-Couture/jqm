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
import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JmxTest extends JqmBaseTest
{
    @Test
    public void jmxRemoteTest() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "KillApp", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        int i = JobRequest.create("KillApp", "TestUser").submit();

        // Get free ports
        ServerSocket s1 = new ServerSocket(0);
        int port1 = s1.getLocalPort();
        ServerSocket s2 = new ServerSocket(0);
        int port2 = s2.getLocalPort();
        s1.close();
        s2.close();
        String hn = InetAddress.getLocalHost().getHostName();

        em.getTransaction().begin();
        TestHelpers.node.setJmxRegistryPort(port1);
        TestHelpers.node.setJmxServerPort(port2);
        em.getTransaction().commit();

        addAndStartEngine();
        Thread.sleep(1000);

        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hn + ":" + port1 + "/jndi/rmi://" + hn + ":" + port2 + "/jmxrmi");
        JMXConnector cntor = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = cntor.getMBeanServerConnection();
        int count = mbsc.getMBeanCount();
        System.out.println(count);

        String[] domains = mbsc.getDomains();
        for (String d : domains)
        {
            System.out.println(d);
        }
        Set<ObjectInstance> mbeans = mbsc.queryMBeans(new ObjectName("com.enioka.jqm:*"), null);
        for (ObjectInstance oi : mbeans)
        {
            System.out.println(oi.getObjectName());
        }
        Assert.assertEquals(6, mbeans.size());

        // /////////////////
        // Loader beans
        ObjectName killBean = new ObjectName("com.enioka.jqm:type=Node.Queue.JobInstance,Node=" + TestHelpers.node.getName() + ",Queue="
                + TestHelpers.qVip.getName() + ",name=" + i);
        System.out.println(killBean.toString());
        mbeans = mbsc.queryMBeans(killBean, null);
        if (mbeans.isEmpty())
        {
            Assert.fail("could not find the JMX Mbean of the launched job");
        }
        LoaderMBean proxy = JMX.newMBeanProxy(mbsc, killBean, LoaderMBean.class);
        Assert.assertEquals("KillApp", proxy.getApplicationName());
        Assert.assertEquals((Integer) i, proxy.getId());
        Assert.assertEquals("TestUser", proxy.getUser());

        // Elements that are not set or testable reproductibly, but should not raise any exception
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
        ObjectName poller = new ObjectName("com.enioka.jqm:type=Node.Queue,Node=" + TestHelpers.node.getName() + ",name="
                + TestHelpers.qVip.getName());
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

}
