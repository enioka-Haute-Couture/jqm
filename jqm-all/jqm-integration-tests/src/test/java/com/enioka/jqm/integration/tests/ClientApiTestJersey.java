package com.enioka.jqm.integration.tests;

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.client.api.JqmClient;
import com.enioka.jqm.client.api.JqmClientException;
import com.enioka.jqm.client.api.JqmInvalidRequestException;
import com.enioka.jqm.client.api.JqmWsClientFactory;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.repository.UserManagementRepository;
import com.enioka.jqm.test.helpers.TestHelpers;

import jakarta.ws.rs.NotAuthorizedException;

/**
 * Run all the client tests, but this time with a WS client.
 */
public class ClientApiTestJersey extends ClientApiTestJdbc
{
    private Node n;
    private Properties p;

    @Before
    public void before() throws IOException
    {
        // Create a node without pollers which will host all WS calls.
        n = Node.create(cnx, "wsnode", 0, "./target/outputfiles/", "./../", "./target/tmp", TestHelpers.getLocalHostName(), "DEBUG");
        GlobalParameter.setParameter(cnx, "disableWsApi", "false");
        GlobalParameter.setParameter(cnx, "enableWsApiAuth", "true");
        GlobalParameter.setParameter(cnx, "enableWsApiSsl", "false");
        GlobalParameter.setParameter(cnx, "logFilePerLaunch", "both");
        cnx.runUpdate("node_update_all_disable_all_ws");
        cnx.runUpdate("node_update_enable_ws_by_id", n.getId());
        cnx.commit();

        addAndStartEngine("wsnode");

        n = Node.select_single(cnx, "node_select_by_key", "wsnode");

        // Set client properties to use this node.
        p = new Properties();
        p.put("com.enioka.jqm.ws.url",
                "http://" + (n.getDns().equals("0.0.0.0") ? "localhost" : n.getDns()) + ":" + n.getPort() + "/ws/client");
        p.put("com.enioka.jqm.ws.login", "test");
        p.put("com.enioka.jqm.ws.password", "testpassword");
        JqmWsClientFactory.setProperties(p);

        UserManagementRepository.createUserIfMissing(cnx, "test", "testpassword", "test user for WS Junit tests", "client power user");
        cnx.commit();

        JqmWsClientFactory.reset();
        jqmClient = JqmWsClientFactory.getClient();
    }

    @Test(expected = JqmInvalidRequestException.class)
    public void testBug292()
    {
        jqmClient.newQuery().setQueryHistoryInstances(false).setQueryLiveInstances(false).invoke();
    }

    @Test
    @Override
    public void testEnqueueWithQueue() throws Exception
    {
        super.testEnqueueWithQueue();
    }

    @Test
    public void testWithWrongPassword()
    {
        Properties temp = new Properties();
        temp.putAll(p);
        temp.put("com.enioka.jqm.ws.password", "testpasswordXXX");

        JqmClient wrongClient = JqmWsClientFactory.getClient("name", temp, false);

        try
        {
            wrongClient.getQueues();
            Assert.fail("JqmClientException was expected - this has worked by mistake!");
        }
        catch (JqmClientException e)
        {
            Assert.assertNotNull(e.getCause());
            Assert.assertTrue(e.getMessage() + " - " + e.getCause().getMessage(), e.getCause() instanceof NotAuthorizedException);
        }
    }
}
