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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class ApiSimpleTest extends JqmBaseTest
{
    @Before
    public void before() throws IOException
    {
        Helpers.setSingleParam("disableWsApi", "false", em);
        Helpers.setSingleParam("enableWsApiAuth", "false", em);

        File jar = FileUtils.listFiles(new File("../jqm-ws/target/"), new String[] { "war" }, false).iterator().next();
        FileUtils.copyFile(jar, new File("./webapp/jqm-ws.war"));

        addAndStartEngine();
    }

    @Test
    public void testHttpEnqueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSend3Msg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip,
                42, "Marsu-Application", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        em.refresh(TestHelpers.node);
        HttpPost post = new HttpPost("http://" + TestHelpers.node.getDns() + ":" + TestHelpers.node.getPort() + "/ws/simple/ji");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("applicationname", "Marsu-Application"));
        nvps.add(new BasicNameValuePair("user", "testuser"));
        nvps.add(new BasicNameValuePair("module", "testuser"));
        nvps.add(new BasicNameValuePair("parameterNames", "arg"));
        nvps.add(new BasicNameValuePair("parameterValues", "overridevalue"));
        nvps.add(new BasicNameValuePair("parameterNames", "arg2"));
        nvps.add(new BasicNameValuePair("parameterValues", "newvalue2"));
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

        // Check run is OK & parameters have been correctly processed
        JobInstance ji = JqmClientFactory.getClient().getJob(jid);
        Assert.assertEquals(com.enioka.jqm.api.State.ENDED, ji.getState());
        Assert.assertEquals(2, ji.getParameters().size());
        Assert.assertEquals("newvalue2", ji.getParameters().get("arg2"));
        Assert.assertEquals("overridevalue", ji.getParameters().get("arg"));
    }

    @Test
    public void testHttpStatus() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSend3Msg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip,
                42, "Marsu-Application", null, "Franquin", "ModuleMachin", "other", "other", true, em);

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
