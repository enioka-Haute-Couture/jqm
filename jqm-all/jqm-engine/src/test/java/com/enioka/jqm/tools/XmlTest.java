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
import java.util.ArrayList;
import java.util.List;

import com.enioka.jqm.jpamodel.JobDefParameter;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class XmlTest extends JqmBaseTest
{
    @Test
    public void testExportQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add("VIPQueue");
        tmp.add("NormalQueue");

        XmlQueueExporter.export(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", em, tmp);

        File t = new File(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");
        Assert.assertEquals(true, t.exists());

        // --> Test Import
        XmlQueueParser.parse(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", em);

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
        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        XmlQueueExporter.export(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", em);

        File t = new File(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");
        Assert.assertEquals(true, t.exists());

        // --> Test Import
        XmlQueueParser.parse(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", em);

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
        // Init the default queue (don't start the engine!)
        Helpers.updateConfiguration(em);

        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltest.xml" });

        List<JobDef> jd = em.createQuery("SELECT j FROM JobDef j", JobDef.class).getResultList();

        Assert.assertEquals(2, jd.size());
        Assert.assertEquals("Fibo", jd.get(0).getApplicationName());
        Assert.assertEquals(true, jd.get(0).isCanBeRestarted());
        Assert.assertEquals("com.enioka.jqm.tests.App", jd.get(0).getJavaClassName());
        Assert.assertEquals(TestHelpers.qVip, jd.get(0).getQueue());
        Assert.assertEquals("ApplicationTest", jd.get(0).getApplication());
        Assert.assertEquals("TestModuleRATONLAVEUR", jd.get(0).getModule());
        Assert.assertEquals(false, jd.get(0).isHighlander());
        Assert.assertEquals("1", jd.get(0).getParameters().get(0).getValue());
        Assert.assertEquals("2", jd.get(0).getParameters().get(1).getValue());
    }

    @Test
    public void testExportJobDef() throws Exception
    {
        List<JobDefParameter> jdp = new ArrayList<JobDefParameter>();
        jdp.add(CreationTools.createJobDefParameter("test-key", "test-value", em));
        CreationTools.createJobDef("My Description", true, "com.enioka.jqm.tests.App", jdp, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", "App", "ModuleMachin", "other1", "other2", null, false, em, "Isolation", true, "HIDDEN");
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime2", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        XmlJobDefExporter.export(TestHelpers.node.getDlRepo() + "xmlexportjobdeftest.xml", em);

        File f = new File(TestHelpers.node.getDlRepo() + "xmlexportjobdeftest.xml");
        Assert.assertEquals(true, f.exists());

        // -> Delete all entries and try to reimport them from the exported file
        em.getTransaction().begin();
        em.createQuery("DELETE JobDefParameter WHERE 1=1)").executeUpdate();
        em.createQuery("DELETE JobDef WHERE 1=1").executeUpdate();
        em.getTransaction().commit();
        XmlJobDefParser.parse(TestHelpers.node.getDlRepo() + "xmlexportjobdeftest.xml", em);

        // test the 4 JobDef were imported
        long count = (Long) em.createQuery("SELECT COUNT(j) FROM JobDef j WHERE j.applicationName IN ('Fibo', 'Geo', 'DateTime', 'DateTime2')").getSingleResult();
        Assert.assertEquals(4, count);
        JobDef fibo = em.createQuery("SELECT j FROM JobDef j WHERE j.applicationName = 'Fibo' ", JobDef.class).getSingleResult();


        Assert.assertEquals("My Description", fibo.getDescription());
        Assert.assertEquals("App", fibo.getApplication());
        Assert.assertEquals("jqm-tests/jqm-test-fibo/target/test.jar", fibo.getJarPath());
        Assert.assertEquals("FS", fibo.getPathType().toString());
        Assert.assertEquals("VIPQueue", fibo.getQueue().getName());
        Assert.assertEquals(true,  fibo.isCanBeRestarted());
        Assert.assertEquals("com.enioka.jqm.tests.App",  fibo.getJavaClassName());
        Assert.assertEquals("ModuleMachin",  fibo.getModule());
        Assert.assertEquals("other1",  fibo.getKeyword1());
        Assert.assertEquals("other2",  fibo.getKeyword2());
        Assert.assertEquals(null,  fibo.getKeyword3());
        Assert.assertEquals(false,  fibo.isHighlander());
        Assert.assertEquals("Isolation",  fibo.getSpecificIsolationContext());
        Assert.assertEquals(true,  fibo.isChildFirstClassLoader());
        Assert.assertEquals("HIDDEN",  fibo.getHiddenJavaClasses());

        f.delete();
    }

    @Test
    public void testImportThenReimportJobDefWithPrms()
    {
        // Init the default queue (don't start the engine!)
        Helpers.updateConfiguration(em);

        // First import
        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltest.xml" });

        List<JobDef> jd = em.createQuery("SELECT j FROM JobDef j", JobDef.class).getResultList();
        Assert.assertEquals(2, jd.size());
        Assert.assertEquals("Fibo", jd.get(0).getApplicationName());
        Assert.assertEquals("1", jd.get(0).getParameters().get(0).getValue());

        // Second import - parameters are different, note 3 instead of 1
        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltest_np.xml" });

        jd = this.getNewEm().createQuery("SELECT j FROM JobDef j", JobDef.class).getResultList();
        Assert.assertEquals(2, jd.size());
        Assert.assertEquals("Fibo", jd.get(0).getApplicationName());
        Assert.assertEquals("3", jd.get(0).getParameters().get(0).getValue());

    }

    @Test
    public void testImportJobdefWithQueue()
    {
        // Init the default queue (don't start the engine!)
        Helpers.updateConfiguration(em);

        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltestnewqueue.xml" });

        List<JobDef> jd = em.createQuery("SELECT j FROM JobDef j", JobDef.class).getResultList();
        Assert.assertEquals(2, jd.size());

        // Was the queue created (and only once)?
        Queue q = em.createQuery("SELECT q from Queue q where q.name = :name", Queue.class).setParameter("name", "NewQueue")
                .getSingleResult();
        Assert.assertEquals("Created from a jobdef import. Description should be set later", q.getDescription());
        em.close();
    }

    @Test
    public void testImportQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        XmlQueueParser.parse("target/payloads/jqm-test-xml/xmlqueuetest.xml", em);

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
