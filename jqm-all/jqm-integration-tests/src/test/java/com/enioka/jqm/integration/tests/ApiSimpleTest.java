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
package com.enioka.jqm.integration.tests;

import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JqmDbClientFactory;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests of the simple web API.
 *
 */
public class ApiSimpleTest extends JqmBaseTest
{
    private int port;

    @Before
    public void before() throws IOException
    {
        GlobalParameter.setParameter(cnx, "disableWsApi", "false");
        GlobalParameter.setParameter(cnx, "enableWsApiAuth", "false");
        cnx.commit();

        addAndStartEngine();

        port = Node.select_single(cnx, "node_select_by_id", TestHelpers.node.getId()).getPort();
        jqmlogger.info("Jetty port seen by client is {}", port);
    }

    @Test
    public void testHttpEnqueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSend3Msg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip,
                42, "Marsu-Application", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

        var url = "http://" + TestHelpers.node.getDns() + ":" + port + "/ws/simple/ji";
        var postData = List.of( // a form encoded body
                Map.entry("applicationname", "Marsu-Application"), //
                Map.entry("user", "testuser"), //
                Map.entry("module", "testuser"), //
                Map.entry("parameterNames", "arg"), //
                Map.entry("parameterValues", "overridevalue"), //
                Map.entry("parameterNames", "arg2"), //
                Map.entry("parameterValues", "newvalue2")).stream()
                .map(entry -> Stream.of(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("=")))
                .collect(Collectors.joining("&"));

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData)).build();

        var res = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, res.statusCode());

        String result = res.body();

        long jid = 0L;
        try
        {
            jid = Long.parseLong(result);
        }
        catch (Exception e)
        {
            Assert.fail("result was not an integer " + e.getMessage());
        }

        TestHelpers.waitFor(1, 10000, cnx);

        // Check run is OK & parameters have been correctly processed
        JobInstance ji = JqmDbClientFactory.getClient().getJob(jid);
        Assert.assertEquals(com.enioka.jqm.client.api.State.ENDED, ji.getState());
        Assert.assertEquals(2, ji.getParameters().size());
        Assert.assertEquals("newvalue2", ji.getParameters().get("arg2"));
        Assert.assertEquals("overridevalue", ji.getParameters().get("arg"));
    }

    @Test
    public void testHttpStatus() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiSend3Msg", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip,
                42, "Marsu-Application", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

        var url = "http://" + TestHelpers.node.getDns() + ":" + port + "/ws/simple/ji";
        var postData = List.of( // a form encoded body
                Map.entry("applicationname", "Marsu-Application"), //
                Map.entry("user", "testuser"), //
                Map.entry("module", "testuser"), //
                Map.entry("param_1", "arg"), //
                Map.entry("paramvalue_1", "newvalue") //
        ).stream()
                .map(entry -> Stream.of(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8),
                        URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("=")))
                .collect(Collectors.joining("&"));

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(postData)).build();

        var res = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, res.statusCode());

        String result = res.body();

        long jid = 0;
        try
        {
            jid = Long.parseLong(result);
        }
        catch (Exception e)
        {
            Assert.fail("result was not an integer " + e.getMessage());
        }

        TestHelpers.waitFor(1, 10000, cnx);

        // Now test get status API
        url = "http://" + TestHelpers.node.getDns() + ":" + port + "/ws/simple/status?id=" + jid;
        request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        res = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assert.assertEquals(200, res.statusCode());

        var currentState = State.valueOf(res.body());

        Assert.assertEquals(State.ENDED, currentState);
    }
}
