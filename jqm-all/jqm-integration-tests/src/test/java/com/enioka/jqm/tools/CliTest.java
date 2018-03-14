package com.enioka.jqm.tools;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.admin.MetaService;
import com.enioka.api.admin.NodeDto;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.model.RUser;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class CliTest extends JqmBaseTest
{
    @Test
    public void testCliChangeUser()
    {
        Helpers.updateConfiguration(cnx);
        Main.main(new String[] { "-U", "myuser", "mypassword", "administrator", "client" });

        RUser u = RUser.selectlogin(cnx, "myuser");

        Assert.assertEquals(2, u.getRoles(cnx).size());
        boolean admin = false, client = false;
        for (RRole r : u.getRoles(cnx))
        {
            if (r.getName().equals("administrator"))
            {
                admin = true;
            }
            if (r.getName().equals("client"))
            {
                client = true;
            }
        }
        Assert.assertTrue(client && admin);

        Main.main(new String[] { "-U", "myuser", "mypassword", "administrator" });
        Assert.assertEquals(1, u.getRoles(cnx).size());

        Main.main(new String[] { "-U", "myuser,mypassword,administrator,config admin" });
        Assert.assertEquals(2, u.getRoles(cnx).size());
    }

    @Test
    public void testSingleLauncher() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        cnx.runUpdate("ji_update_poll", TestHelpers.node.getId(), TestHelpers.qVip, 10);
        cnx.runUpdate("debug_jj_update_node_by_id", TestHelpers.node.getId(), i);
        cnx.commit();

        Main.main(new String[] { "-s", String.valueOf(i) });

        // This is not really a one shot JVM, so let's reset log4j
        LogManager.resetConfiguration();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testTemplate() throws Exception
    {
        NodeDto template = MetaService.getNode(cnx, TestHelpers.nodeMix.getId());
        template.setPort(123);
        MetaService.upsertNode(cnx, template);
        cnx.commit();

        NodeDto target = MetaService.getNode(cnx, TestHelpers.node.getId());
        Assert.assertEquals(3, MetaService.getNodeQueueMappings(cnx, target.getId()).size());

        Main.main(new String[] { "-t", TestHelpers.nodeMix.getName() + "," + TestHelpers.node.getName() });

        target = MetaService.getNode(cnx, TestHelpers.node.getId());

        Assert.assertEquals(template.getPort(), target.getPort());
        Assert.assertEquals(1, MetaService.getNodeQueueMappings(cnx, target.getId()).size());
    }
}
