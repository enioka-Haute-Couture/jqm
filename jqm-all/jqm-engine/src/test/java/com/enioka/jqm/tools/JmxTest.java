package com.enioka.jqm.tools;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JmxTest extends JqmBaseTest
{
    @Test
    public void jmxRemoteTest() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test jmxRemoteTest");

        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo",
                null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, null, "jqm-tests/jqm-test-kill/target/test.jar", TestHelpers.qVip, 42,
                "KillApp", null, "Franquin", "ModuleMachin", "other", "other", false, em);

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

        em.close();

        JqmEngine e1 = new JqmEngine();
        e1.start("localhost");

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

        cntor.close();

        e1.stop();

        Assert.assertEquals(5, mbeans.size());
    }

}
