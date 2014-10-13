package com.enioka.jqm.tools;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Queue;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.pki.JpaCa;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class SslTest
{
    public static Logger jqmlogger = Logger.getLogger(SslTest.class);

    JqmEngine engine1;
    EntityManager em;
    public static org.hsqldb.Server s;
    private Properties p;

    @BeforeClass
    public static void testInit() throws Exception
    {
        JndiContext.createJndiContext();
        s = new org.hsqldb.Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();
    }

    @AfterClass
    public static void stop() throws NamingException
    {
        JqmClientFactory.resetClient(null);
        Helpers.resetEmf();
        ((JndiContext) NamingManager.getInitialContext(null)).resetSingletons();
        s.shutdown();
        s.stop();
    }

    @Test
    public void testSslAuth() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testSslAuth");

        // Test envt
        em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createTestData(em);

        // Start in SSL mode with web services
        File jar = FileUtils.listFiles(new File("../../jqm-ws/target/"), new String[] { "war" }, false).iterator().next();
        FileUtils.copyFile(jar, new File("./webapp/jqm-ws.war"));
        Helpers.setSingleParam("disableWsApi", "false", em);
        Helpers.setSingleParam("enableWsApiAuth", "true", em);
        Helpers.setSingleParam("enableWsApiSsl", "true", em);

        em.getTransaction().begin();
        TestHelpers.node.setLoadApiAdmin(true);
        TestHelpers.node.setLoadApiClient(true);
        TestHelpers.node.setLoapApiSimple(true);
        em.getTransaction().commit();

        engine1 = new JqmEngine();
        engine1.start("localhost");

        // Test user
        RRole r = em.createQuery("SELECT rr from RRole rr WHERE rr.name = :r", RRole.class).setParameter("r", "administrator")
                .getSingleResult();
        CreationTools.createUser(em, "test", "test", r);

        // Create auth data
        Node n = em.find(Node.class, TestHelpers.node.getId());
        em.refresh(n);
        JpaCa.prepareClientStore(em, "CN=test", "./conf/client.pfx", "SuperPassword", "client-cert", "./conf/client.cer");

        // Configure client
        p = new Properties();
        p.put("com.enioka.jqm.ws.url", "https://" + n.getDns() + ":" + n.getPort() + "/ws/client");
        p.put("com.enioka.jqm.ws.login", "testdfg"); // Wrong on purpose...
        p.put("com.enioka.jqm.ws.password", "testcdfg"); // Same as above
        p.put("com.enioka.jqm.ws.keystoreFile", "./conf/client.pfx");
        p.put("com.enioka.jqm.ws.keystorePass", "SuperPassword");
        p.put("com.enioka.jqm.ws.keystoreType", "PKCS12");
        p.put("com.enioka.jqm.ws.truststoreFile", "./conf/trusted.jks");
        p.put("com.enioka.jqm.ws.truststorePass", "SuperPassword");
        JqmClientFactory.setProperties(p);

        // Try a query
        List<Queue> qs = JqmClientFactory.getClient().getQueues();
        Assert.assertEquals(9, qs.size());

        // Done
        engine1.stop();
    }
}
