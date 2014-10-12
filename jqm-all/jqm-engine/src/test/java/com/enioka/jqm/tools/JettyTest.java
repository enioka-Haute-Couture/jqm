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
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.pki.JpaCa;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JettyTest extends JqmBaseTest
{
    @Before
    public void before() throws IOException
    {
        File jar = FileUtils.listFiles(new File("../jqm-ws/target/"), new String[] { "war" }, false).iterator().next();
        FileUtils.copyFile(jar, new File("./webapp/jqm-ws.war"));
    }

    @Test
    public void testSslStartup()
    {
        Helpers.setSingleParam("enableWsApiSsl", "true", em);
        Helpers.setSingleParam("disableWsApi", "false", em);
        Helpers.setSingleParam("enableWsApiAuth", "false", em);

        addAndStartEngine();
    }

    @Test
    public void testSslServices() throws Exception
    {
        Helpers.setSingleParam("enableWsApiSsl", "true", em);
        Helpers.setSingleParam("disableWsApi", "false", em);
        Helpers.setSingleParam("enableWsApiAuth", "false", em);

        addAndStartEngine();

        // Launch a job so as to be able to query its status later
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        JobRequest j = new JobRequest("MarsuApplication", "TestUser");
        int i = JqmClientFactory.getClient().enqueue(j);
        TestHelpers.waitFor(1, 10000, em);

        // HTTPS client - with
        KeyStore trustStore = KeyStore.getInstance("JKS");
        FileInputStream instream = new FileInputStream(new File("./conf/trusted.jks"));
        try
        {
            trustStore.load(instream, "SuperPassword".toCharArray());
        }
        finally
        {
            instream.close();
        }

        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        CloseableHttpClient cl = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        int port = em.createQuery("SELECT q.port FROM Node q WHERE q.id = :i", Integer.class).setParameter("i", TestHelpers.node.getId())
                .getSingleResult();
        HttpUriRequest rq = new HttpGet("https://" + TestHelpers.node.getDns() + ":" + port + "/ws/simple/status?id=" + i);
        jqmlogger.debug(rq.getURI());
        CloseableHttpResponse rs = cl.execute(rq);
        Assert.assertEquals(200, rs.getStatusLine().getStatusCode());

        rs.close();
        cl.close();
    }

    @Test
    public void testSslClientCert() throws Exception
    {
        Helpers.setSingleParam("enableWsApiSsl", "true", em);
        Helpers.setSingleParam("disableWsApi", "false", em);
        Helpers.setSingleParam("enableWsApiAuth", "false", em);

        addAndStartEngine();

        // Launch a job so as to be able to query its status later
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        JobRequest j = new JobRequest("MarsuApplication", "TestUser");
        int i = JqmClientFactory.getClient().enqueue(j);
        TestHelpers.waitFor(1, 10000, em);

        // Server auth against trusted CA root certificate
        KeyStore trustStore = KeyStore.getInstance("JKS");
        FileInputStream instream = new FileInputStream(new File("./conf/trusted.jks"));
        try
        {
            trustStore.load(instream, "SuperPassword".toCharArray());
        }
        finally
        {
            instream.close();
        }

        // Client auth
        JpaCa.prepareClientStore(em, "CN=testuser", "./conf/client.pfx", "SuperPassword", "client-cert", "./conf/client.cer");
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        instream = new FileInputStream(new File("./conf/client.pfx"));
        try
        {
            clientStore.load(instream, "SuperPassword".toCharArray());
        }
        finally
        {
            instream.close();
        }

        SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(trustStore)
                .loadKeyMaterial(clientStore, "SuperPassword".toCharArray()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);

        CloseableHttpClient cl = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        int port = em.createQuery("SELECT q.port FROM Node q WHERE q.id = :i", Integer.class).setParameter("i", TestHelpers.node.getId())
                .getSingleResult();
        HttpUriRequest rq = new HttpGet("https://" + TestHelpers.node.getDns() + ":" + port + "/ws/simple/status?id=" + i);
        CloseableHttpResponse rs = cl.execute(rq);
        Assert.assertEquals(200, rs.getStatusLine().getStatusCode());

        rs.close();
        cl.close();
    }
}
