package com.enioka.jqm.tools;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class ClLeakTest extends JqmBaseTest
{
    @Test
    public void testJmxLeak() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testJmxLeak");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-jmxleak/target/test.jar", TestHelpers.qVip,
                42, "Test", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);

        JobRequest form = new JobRequest("Test", "MAG");
        int i = JqmClientFactory.getClient().enqueue(form);

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        Thread.sleep(2000);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.test:type=Node,name=test");

        mbs.getAttribute(name, "One");

        // Stop the job. Its MBean(s) should be cleaned up by the engine.
        JqmClientFactory.getClient().killJob(i);
        Thread.sleep(2000);

        // Check the bean is really dead
        try
        {
            mbs.getAttribute(name, "One");
            Assert.fail();
        }
        catch (InstanceNotFoundException e)
        {
            // It's OK!
        }

        engine1.stop();
    }
}
