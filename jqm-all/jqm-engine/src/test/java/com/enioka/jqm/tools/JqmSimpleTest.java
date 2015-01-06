package com.enioka.jqm.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.junit.Assert;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JqmSimpleTest
{
    private EntityManager em;
    private JobDef jd = null;
    private Map<String, String> runtimePrms = new HashMap<String, String>();
    private List<String> nodeNames = new ArrayList<String>();

    private int expectedOk = 1, expectedNonOk = 0;

    private JqmSimpleTest(EntityManager em, String className, String artifactName)
    {
        this.em = em;
        this.jd = CreationTools.createJobDef(null, true, className, new ArrayList<JobDefParameter>(), "jqm-tests/" + artifactName
                + "/target/test.jar", TestHelpers.qVip, -1, "TestJqmApplication", "appFreeName", "TestModule", "kw1", "kw2", "kw3", false,
                em);
        nodeNames.add("localhost");
    }

    public static JqmSimpleTest create(EntityManager em, String className)
    {
        return new JqmSimpleTest(em, className, "jqm-test-pyl");
    }

    public static JqmSimpleTest create(EntityManager em, String className, String artifact)
    {
        return new JqmSimpleTest(em, className, artifact);
    }

    public JqmSimpleTest addDefParameter(String key, String value)
    {
        em.getTransaction().begin();
        JobDefParameter jdp = CreationTools.createJobDefParameter(key, value, em);
        this.jd.getParameters().add(jdp);
        em.getTransaction().commit();
        return this;
    }

    public JqmSimpleTest addRuntimeParameter(String key, String value)
    {
        this.runtimePrms.put(key, value);
        return this;
    }

    public JqmSimpleTest addEngine(String nodeName)
    {
        nodeNames.add(nodeName);
        return this;
    }

    public JqmSimpleTest expectOk(int expected)
    {
        this.expectedOk = expected;
        return this;
    }

    public JqmSimpleTest expectNonOk(int expected)
    {
        this.expectedNonOk = expected;
        return this;
    }

    public Integer run(JqmBaseTest test)
    {
        int nbExpected = expectedNonOk + expectedOk;

        for (String nodeName : nodeNames)
        {
            test.addAndStartEngine(nodeName);
        }
        Integer i = JobRequest.create("TestJqmApplication", "TestUser").setParameters(runtimePrms).submit();
        TestHelpers.waitFor(nbExpected, 9000 + nbExpected * 1000, em);

        Assert.assertEquals(expectedOk, TestHelpers.getOkCount(em));
        Assert.assertEquals(expectedNonOk, TestHelpers.getNonOkCount(em));

        return i;
    }
}
