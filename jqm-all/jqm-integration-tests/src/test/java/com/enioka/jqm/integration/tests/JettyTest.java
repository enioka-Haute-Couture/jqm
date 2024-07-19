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

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.pki.JdbcCa;
import com.enioka.jqm.repository.UserManagementRepository;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JettyTest extends JqmBaseTest
{
    private int port;

    private void waitStartup()
    {
        port = Node.select_single(cnx, "node_select_by_id", TestHelpers.node.getId()).getPort();
        jqmlogger.info("Jetty port seen by client is {}", port);
    }

    @Test
    public void testSslStartup()
    {
        GlobalParameter.setParameter(cnx, "enableWsApiSsl", "true");
        GlobalParameter.setParameter(cnx, "disableWsApi", "false");
        GlobalParameter.setParameter(cnx, "enableWsApiAuth", "false");
        cnx.commit();

        addAndStartEngine();
        waitStartup();
    }

    @Test
    public void testSslServices() throws Exception
    {
        GlobalParameter.setParameter(cnx, "enableWsApiSsl", "true");
        GlobalParameter.setParameter(cnx, "disableWsApi", "false");
        GlobalParameter.setParameter(cnx, "enableWsApiAuth", "false");
        cnx.commit();

        addAndStartEngine();
        waitStartup();

        // Launch a job so as to be able to query its status later
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        long i = jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();
        TestHelpers.waitFor(1, 10000, cnx);

        // HTTPS client - with
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (var instream = new FileInputStream(new File("./target/server/conf/trusted.jks")))
        {
            trustStore.load(instream, "SuperPassword".toCharArray());
        }

        var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, tmf.getTrustManagers(), null);

        var cl = HttpClient.newBuilder().sslContext(sslcontext).build();

        int port = Node.select_single(cnx, "node_select_by_id", TestHelpers.node.getId()).getPort();

        var uri = new URI("https://" + TestHelpers.node.getDns() + ":" + port + "/ws/simple/status?id=" + i);
        HttpRequest rq = HttpRequest.newBuilder(uri).GET().build();
        jqmlogger.debug("{}", uri);

        var rs = cl.send(rq, BodyHandlers.ofString(StandardCharsets.UTF_8));

        Assert.assertEquals(200, rs.statusCode());
        jqmlogger.debug(rs.body());
    }

    @Test
    public void testSslClientCert() throws Exception
    {
        GlobalParameter.setParameter(cnx, "enableWsApiSsl", "true");
        GlobalParameter.setParameter(cnx, "disableWsApi", "false");
        GlobalParameter.setParameter(cnx, "enableWsApiAuth", "true");
        UserManagementRepository.createUserIfMissing(cnx, "testuser", null, "test user", "client read only");
        cnx.commit();

        addAndStartEngine();
        waitStartup();

        // Launch a job so as to be able to query its status later
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        long i = jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();
        TestHelpers.waitFor(1, 10000, cnx);

        // Server auth against trusted CA root certificate
        KeyStore trustStore = KeyStore.getInstance("JKS");
        FileInputStream instream = new FileInputStream(new File("./target/server/conf/trusted.jks"));
        try
        {
            trustStore.load(instream, "SuperPassword".toCharArray());
        }
        finally
        {
            instream.close();
        }

        // Client auth
        JdbcCa.prepareClientStore(cnx, "CN=testuser", "./target/server/conf/client.pfx", "SuperPassword", "client-cert",
                "./target/server/conf/client.cer");
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        instream = new FileInputStream(new File("./target/server/conf/client.pfx"));
        try
        {
            clientStore.load(instream, "SuperPassword".toCharArray());
        }
        finally
        {
            instream.close();
        }

        // Create client
        var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, "SuperPassword".toCharArray());

        SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        var cl = HttpClient.newBuilder().sslContext(sslcontext).build();

        int port = Node.select_single(cnx, "node_select_by_id", TestHelpers.node.getId()).getPort();

        var uri = new URI("https://" + TestHelpers.node.getDns() + ":" + port + "/ws/simple/status?id=" + i);
        HttpRequest rq = HttpRequest.newBuilder(uri).GET().build();
        jqmlogger.debug("{}", uri);

        var rs = cl.send(rq, BodyHandlers.ofString(StandardCharsets.UTF_8));

        Assert.assertEquals(200, rs.statusCode());
        jqmlogger.debug(rs.body());
    }
}
