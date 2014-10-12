package com.enioka.jqm.tools;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class ClLeakTest extends JqmBaseTest
{
    @Test
    public void testJmxLeak() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-jmxleak/target/test.jar", TestHelpers.qVip, 42, "Test",
                null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        int i = JobRequest.create("Test", "TestUser").submit();

        // Start the engine
        addAndStartEngine();
        Thread.sleep(5000);

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.test:type=Node,name=test");
        mbs.getAttribute(name, "One");

        // Stop the job. Its MBean(s) should be cleaned up by the engine.
        JqmClientFactory.getClient().killJob(i);
        Thread.sleep(3000);

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
    }
}
