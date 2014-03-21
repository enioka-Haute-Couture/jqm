package com.enioka.jqm.tools;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class DeliverableTest extends JqmBaseTest
{
    @Test
    public void testGetDeliverables() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetDeliverables");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", TestHelpers.node.getDlRepo(), em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable1.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-deliverable/target/test.jar", TestHelpers.qVip, 42,
                "getDeliverables", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("getDeliverables", "MAG");
        int id = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000, em);

        List<InputStream> tmp = JqmClientFactory.getClient().getJobDeliverablesContent(id);
        engine1.stop();
        Assert.assertEquals(1, tmp.size());

        Assert.assertTrue(tmp.get(0).available() > 0);

        tmp.get(0).close();
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "getDeliverables")));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + id));
    }

    @Test
    public void testGetOneDeliverable() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetOneDeliverable");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", TestHelpers.node.getDlRepo(), em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable2.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-deliverable/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "Franquin");

        int jobId = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000, em);

        File f = new File(TestHelpers.node.getDlRepo() + "jqm-test-deliverable2.txt");
        Assert.assertEquals(false, f.exists()); // file should have been moved

        List<com.enioka.jqm.api.Deliverable> files = JqmClientFactory.getClient().getJobDeliverables(jobId);
        Assert.assertEquals(1, files.size());

        InputStream tmp = JqmClientFactory.getClient().getDeliverableContent(files.get(0));
        engine1.stop();
        Assert.assertTrue(tmp.available() > 0);

        (new File(files.get(0).getFilePath())).delete();
        tmp.close();
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "MarsuApplication")));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + jobId));
    }

    @Test
    public void testGetAllDeliverables() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetAllDeliverables");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);
        EntityManager emm = Helpers.getNewEm();

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", TestHelpers.node.getDlRepo(), em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable3.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-deliverable/target/test.jar",
                TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobRequest j = new JobRequest("MarsuApplication", "Franquin");

        int id = JqmClientFactory.getClient().enqueue(j);

        JobInstance ji = emm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
                .setParameter("myId", jdDemoMaven.getId()).getSingleResult();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000, em);

        List<com.enioka.jqm.api.Deliverable> tmp = JqmClientFactory.getClient().getJobDeliverables(ji.getId());
        engine1.stop();

        Assert.assertEquals(1, tmp.size());
        (new File(tmp.get(0).getFilePath())).delete();
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "MarsuApplication")));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + id));
    }

}
