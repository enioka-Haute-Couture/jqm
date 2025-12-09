package com.enioka.jqm.integration.tests;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class MultiRepoTest extends JqmBaseTest{


    @Before
    public void b()
    {
        TestHelpers.setNodesLogLevel("INFO", cnx);
    }

    @Test
    public void testCreateNodeWithMultipleRepo()
    {
        long qId = Queue.create(cnx, "testqueue", "super test queue", false);

        CreationTools.createJobDef(null, true, "pyl.EngineApiSendMsg", null, "test.jar", qId, 42, "appliname",
            null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        Node n0 = Node.create(cnx, "n0", 0, "./target/outputfiles/", "./../jqm-tests/jqm-test-pyl/src" + File.pathSeparator +"./../jqm-tests/jqm-test-pyl/target", "./target/tmp", "localhost", "INFO");

        DeploymentParameter.create(cnx, n0.getId(), 5, 1, qId);

        TestHelpers.setNodesLogLevel("DEBUG", cnx);

        cnx.commit();

        jqmClient.newJobRequest("appliname", "user").enqueue();

        this.addAndStartEngine("n0");

        TestHelpers.waitFor(1, 120000, cnx);

        long msgs = cnx.runSelectSingle("message_select_count_all", Long.class);
        Assert.assertEquals(1, msgs);

    }
}
