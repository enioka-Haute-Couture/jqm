package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class ApiSimpleTest extends JqmBaseTest
{
    EntityManager em;
    JqmEngine engine1 = null;

    @Before
    public void before() throws IOException
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Test prepare");

        em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        Helpers.setSingleParam("noHttp", "false", em);
        Helpers.setSingleParam("useAuth", "false", em);

        File jar = FileUtils.listFiles(new File("../jqm-ws/target/"), new String[] { "war" }, false).iterator().next();
        FileUtils.copyFile(jar, new File("./webapp/jqm-ws.war"));

        // We need to reset credentials used inside the client
        JqmClientFactory.resetClient(null);

        // Start the engine
        engine1 = new JqmEngine();
        engine1.start("localhost");
    }

    @After
    public void after()
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Test cleanup");

        em.close();
        engine1.stop();

        // Java 6 GC being rather inefficient, we must run it multiple times to correctly collect Jetty-created class loaders and avoid
        // permgen issues
        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();
        System.gc();
    }

    @Test
    public void testHttpEnqueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHttpEnqueue");

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);
        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-sendmsg/target/test.jar", TestHelpers.qVip, 42,
                "Marsu-Application", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        em.refresh(TestHelpers.node);
        HttpPost post = new HttpPost("http://" + TestHelpers.node.getDns() + ":" + TestHelpers.node.getPort() + "/ws/simple/ji");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("applicationname", "Marsu-Application"));
        nvps.add(new BasicNameValuePair("user", "testuser"));
        nvps.add(new BasicNameValuePair("module", "testuser"));
        nvps.add(new BasicNameValuePair("param_1", "arg"));
        nvps.add(new BasicNameValuePair("paramvalue_1", "newvalue"));
        post.setEntity(new UrlEncodedFormEntity(nvps));

        HttpClient client = HttpClients.createDefault();
        HttpResponse res = client.execute(post);

        Assert.assertEquals(200, res.getStatusLine().getStatusCode());

        HttpEntity entity = res.getEntity();
        InputStream in = entity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer);
        String result = writer.toString();
        IOUtils.closeQuietly(in);

        Integer jid = 0;
        try
        {
            jid = Integer.parseInt(result);
        }
        catch (Exception e)
        {
            Assert.fail("result was not an integer " + e.getMessage());
        }

        TestHelpers.waitFor(1, 10000, em);

        // Check run is OK
        History h = em.createQuery("SELECT j FROM History j", History.class).getSingleResult();
        Assert.assertEquals(State.ENDED, h.getStatus());
        Assert.assertEquals(jid, h.getId());
    }

    @Test
    public void testHttpStatus() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHttpStatus");

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-sendmsg/target/test.jar",
                TestHelpers.qVip, 42, "Marsu-Application", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        em.refresh(TestHelpers.node);
        HttpPost post = new HttpPost("http://" + TestHelpers.node.getDns() + ":" + TestHelpers.node.getPort() + "/ws/simple/ji");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("applicationname", "Marsu-Application"));
        nvps.add(new BasicNameValuePair("user", "testuser"));
        nvps.add(new BasicNameValuePair("module", "testuser"));
        nvps.add(new BasicNameValuePair("param_1", "arg"));
        nvps.add(new BasicNameValuePair("paramvalue_1", "newvalue"));
        post.setEntity(new UrlEncodedFormEntity(nvps));

        HttpClient client = HttpClients.createDefault();
        HttpResponse res = client.execute(post);

        Assert.assertEquals(200, res.getStatusLine().getStatusCode());

        HttpEntity entity = res.getEntity();
        InputStream in = entity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer);
        String result = writer.toString();
        IOUtils.closeQuietly(in);

        Integer jid = 0;
        try
        {
            jid = Integer.parseInt(result);
        }
        catch (Exception e)
        {
            Assert.fail("result was not an integer " + e.getMessage());
        }

        TestHelpers.waitFor(1, 10000, em);

        HttpGet rq = new HttpGet("http://" + TestHelpers.node.getDns() + ":" + TestHelpers.node.getPort() + "/ws/simple/status?id=" + jid);
        res = client.execute(rq);
        Assert.assertEquals(200, res.getStatusLine().getStatusCode());

        entity = res.getEntity();
        in = entity.getContent();
        writer = new StringWriter();
        IOUtils.copy(in, writer);
        State currentState = State.valueOf(writer.toString());
        IOUtils.closeQuietly(in);

        Assert.assertEquals(State.ENDED, currentState);
    }

}
