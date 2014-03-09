package com.enioka.jqm.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class XmlTest extends JqmBaseTest
{
    @Test
    public void testExportQueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testExportQueue");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo",
                null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimemaven/target/test.jar",
                TestHelpers.qNormal, 42, "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add("VIPQueue");
        tmp.add("NormalQueue");

        QueueXmlExporter qxe = new QueueXmlExporter();
        qxe.exportSeveral(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", tmp);

        File t = new File(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");
        Assert.assertEquals(true, t.exists());

        // --> Test Import

        QueueXmlParser qxp = new QueueXmlParser();
        qxp.parse(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");

        long ii = (Long) em.createQuery("SELECT COUNT(q) FROM Queue q WHERE q.name = 'VIPQueue'").getSingleResult();
        long iii = (Long) em.createQuery("SELECT COUNT(q) FROM Queue q WHERE q.name = 'NormalQueue'").getSingleResult();
        Assert.assertEquals(2, ii + iii);
        Assert.assertEquals("VIPQueue", em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'Fibo' ", String.class)
                .getSingleResult());
        Assert.assertEquals("VIPQueue", em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'Geo' ", String.class)
                .getSingleResult());
        Assert.assertEquals("NormalQueue",
                em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'DateTime' ", String.class).getSingleResult());
        t.delete();
    }

    @Test
    public void testExportQueueAll() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testExportQueueAll");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo",
                null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimemaven/target/test.jar",
                TestHelpers.qNormal, 42, "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        QueueXmlExporter qxe = new QueueXmlExporter();
        qxe.exportAll(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");

        File t = new File(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");
        Assert.assertEquals(true, t.exists());

        // --> Test Import

        QueueXmlParser qxp = new QueueXmlParser();
        qxp.parse(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");

        long ii = (Long) em.createQuery("SELECT COUNT(q) FROM Queue q WHERE q.name = 'VIPQueue'").getSingleResult();
        long iii = (Long) em.createQuery("SELECT COUNT(q) FROM Queue q WHERE q.name = 'NormalQueue'").getSingleResult();
        Assert.assertEquals(2, ii + iii);
        Assert.assertEquals("VIPQueue", em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'Fibo' ", String.class)
                .getSingleResult());
        Assert.assertEquals("VIPQueue", em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'Geo' ", String.class)
                .getSingleResult());
        Assert.assertEquals("NormalQueue",
                em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'DateTime' ", String.class).getSingleResult());
        t.delete();
    }

    @Test
    public void testXmlParser()
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testXmlParser");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        // Init the default queue (don't start the engine!)
        Helpers.checkAndUpdateNodeConfiguration("marsu", em);

        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltest.xml" });

        List<JobDef> jd = em.createQuery("SELECT j FROM JobDef j", JobDef.class).getResultList();

        Assert.assertEquals(2, jd.size());
        Assert.assertEquals("Fibo", jd.get(0).getApplicationName());
        Assert.assertEquals(true, jd.get(0).isCanBeRestarted());
        Assert.assertEquals("com.enioka.jqm.tests.App", jd.get(0).getJavaClassName());
        Assert.assertEquals("jqm-test-fibo/", jd.get(0).getFilePath());
        Assert.assertEquals(TestHelpers.qVip, jd.get(0).getQueue());
        Assert.assertEquals((Integer) 42, jd.get(0).getMaxTimeRunning());
        Assert.assertEquals("ApplicationTest", jd.get(0).getApplication());
        Assert.assertEquals("TestModuleRATONLAVEUR", jd.get(0).getModule());
        Assert.assertEquals(false, jd.get(0).isHighlander());
        Assert.assertEquals("1", jd.get(0).getParameters().get(0).getValue());
        Assert.assertEquals("2", jd.get(0).getParameters().get(1).getValue());
    }

    @Test
    public void testImportQueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testImportQueue");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo",
                null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, null, "jqm-tests/jqm-test-datetimemaven/target/test.jar",
                TestHelpers.qNormal, 42, "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        QueueXmlParser qxp = new QueueXmlParser();
        qxp.parse("target/payloads/jqm-test-xml/xmlqueuetest.xml");

        long ii = (Long) em.createQuery("SELECT COUNT(q) FROM Queue q WHERE q.name = 'XmlQueue'").getSingleResult();
        long iii = (Long) em.createQuery("SELECT COUNT(q) FROM Queue q WHERE q.name = 'XmlQueue2'").getSingleResult();
        Assert.assertEquals(2, ii + iii);
        Assert.assertEquals("XmlQueue", em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'Fibo' ", String.class)
                .getSingleResult());
        Assert.assertEquals("XmlQueue", em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'Geo' ", String.class)
                .getSingleResult());
        Assert.assertEquals("XmlQueue2",
                em.createQuery("SELECT j.queue.name FROM JobDef j WHERE j.applicationName = 'DateTime' ", String.class).getSingleResult());
    }

}
