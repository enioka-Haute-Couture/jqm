package com.enioka.jqm.ws;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.api.JobInstance;

public class BasicTest extends JerseyTest
{
    EntityManager em = null;

    @Override
    protected Application configure()
    {
        return new JqmWsApp();
    }

    @Before
    public void before()
    {
        em = TestHelpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);
    }

    @Test
    public void test()
    {
        final String hello = target("test/hello").request().get(String.class);
        Assert.assertEquals("Hi there!", hello);
    }

    @Test
    public void testX()
    {
        final JobDefinition hello = target("test/testx").request().get(JobDefinition.class);
        Assert.assertEquals("hhh", hello.getUser());
    }

    @Test
    public void testEnqueue()
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-test-datetimemaven/", "jqm-test-datetimemaven/jqm-test-datetimemaven.jar",
                TestHelpers.qVip, 42, "TestApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition jd = new JobDefinition("TestApplication", "SuperUser");
        int newJobId = target("ji").request().put(Entity.entity(jd, MediaType.APPLICATION_XML), Integer.class);
        Assert.assertEquals(1, newJobId);
    }

    @Test
    public void testList()
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-test-datetimemaven/", "jqm-test-datetimemaven/jqm-test-datetimemaven.jar",
                TestHelpers.qVip, 42, "TestApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition jd = new JobDefinition("TestApplication", "SuperUser");
        int newJobId = target("ji").request().put(Entity.entity(jd, MediaType.APPLICATION_XML), Integer.class);

        List<JobInstance> res = target("ji").request().get(new GenericType<List<JobInstance>>()
        {
        });

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(newJobId, (int) res.get(0).getId());
    }
}
