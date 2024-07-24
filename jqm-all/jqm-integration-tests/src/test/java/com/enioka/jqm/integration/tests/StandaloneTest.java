package com.enioka.jqm.integration.tests;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Properties;

import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Option;

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
        // ///// Disabled for non-HSQLDB runners

        AssumeHsqldb();

        // ///// Standalone setup

        localIp = Inet4Address.getLocalHost().getHostAddress();
        GlobalParameter.setParameter(cnx, "wsStandaloneMode", "true");
        cnx.commit();
    }

    @Test
    public void testStandaloneJobIdMatchesIp() {
        addAndStartEngine();
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
            "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        long generatedId = jqmClient.newJobRequest("MarsuApplication", "TestUser").enqueue();

        Assert.assertEquals("Generated ID was " + generatedId, localIp, ipFromId(generatedId));
    }
}
