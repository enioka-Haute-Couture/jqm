package com.enioka.jqm.tools;

import java.net.ServerSocket;

import javax.persistence.EntityManager;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

/**
 * These are not automated tests, but helpers during dev time. They are not meant to complete successfully.
 * 
 * @author Marc-Antoine
 * 
 */
public class ManualTests extends JqmBaseTest
{
    // @Test
    public void jmxTestEnvt() throws Exception
    {
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
        em.getTransaction().begin();
        TestHelpers.node.setJmxRegistryPort(port1);
        TestHelpers.node.setJmxServerPort(port2);
        em.getTransaction().commit();
        em.close();

        Main.main(new String[] { "-startnode", "localhost" });
        Main.main(new String[] { "-startnode", "localhost4" });

        JobRequest form = new JobRequest("Geo", "test");
        form.addParameter("nbJob", "1");
        JqmClientFactory.getClient().enqueue(form);

        form = new JobRequest("Fibo", "test");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");
        JqmClientFactory.getClient().enqueue(form);

        form = new JobRequest("KillApp", "test");
        JqmClientFactory.getClient().enqueue(form);

        Thread.sleep(Integer.MAX_VALUE);
    }
}
