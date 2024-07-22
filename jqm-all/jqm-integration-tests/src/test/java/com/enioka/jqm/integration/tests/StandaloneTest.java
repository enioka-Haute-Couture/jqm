package com.enioka.jqm.integration.tests;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Properties;

import com.enioka.jqm.client.jersey.api.JqmClientFactory;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.repository.UserManagementRepository;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;

import static com.enioka.jqm.shared.misc.StandaloneHelpers.idSequenceBaseFromIp;
import static com.enioka.jqm.shared.misc.StandaloneHelpers.ipFromId;

public class StandaloneTest extends JqmBaseTest {
    private Node n;
    private Properties p;
    private String localIp;

    @Override
    protected Option[] moreOsgiconfig()
    {
        return webConfig();
    }

    @Before
    public void before() throws UnknownHostException {
        // ///// Standalone setup

        localIp = Inet4Address.getLocalHost().getHostAddress();
        GlobalParameter.setParameter(cnx, "wsStandaloneMode", "true");
        cnx.commit();

        // ///// Jersey setup

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

        serviceWaiter.waitForService("[com.enioka.jqm.ws.api.ServiceClient]");
        serviceWaiter.waitForService("[javax.servlet.Servlet]"); // HTTP & JAX-RS whiteboard // FIXME: stuck

        n = Node.select_single(cnx, "node_select_by_key", "wsnode");

        // Set client properties to use this node.
        p = new Properties();
        p.put("com.enioka.jqm.ws.url",
            "http://" + (n.getDns().equals("0.0.0.0") ? "localhost" : n.getDns()) + ":" + n.getPort() + "/ws/client");
        p.put("com.enioka.jqm.ws.login", "test");
        p.put("com.enioka.jqm.ws.password", "testpassword");
        JqmClientFactory.setProperties(p);

        UserManagementRepository.createUserIfMissing(cnx, "test", "testpassword", "test user for WS Junit tests", "client power user");
        cnx.commit();

        JqmClientFactory.resetClient();
        jqmClient = JqmClientFactory.getClient(); // FIXME: requires service Servlet above
    }

    @Test
    public void testStandaloneJobIdMatchesIp() {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
            "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        long generatedId = jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();

        Assert.assertEquals("Generated ID was " + generatedId, localIp, ipFromId(generatedId));
    }

    // FIXME: requires Jersey client
    @Test
    public void testStandaloneGetJobRedirect() {
        addAndStartEngine();
        final var sampleId = idSequenceBaseFromIp(localIp) + 5_000_000;
        // FIXME refine expected error, we expect it to try and hit 127.0.0.6 assuming localIp is 127.0.0.1
        Assert.assertThrows(RuntimeException.class, () -> jqmClient.getJob(sampleId));
    }
}
