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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.jdbc.NoResultException;
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
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add("VIPQueue");
        tmp.add("NormalQueue");

        XmlQueueExporter.export(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", cnx, tmp);

        File t = new File(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");
        Assert.assertEquals(true, t.exists());

        // --> Test Import
        XmlQueueParser.parse(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", cnx);

        try
        {
            Queue.select(cnx, "q_select_by_key", "VIPQueue");
            Queue.select(cnx, "q_select_by_key", "NormalQueue");

            JobDef jd1 = JobDef.select_key(cnx, "Fibo");
            JobDef jd2 = JobDef.select_key(cnx, "Geo");
            JobDef jd3 = JobDef.select_key(cnx, "DateTime");

            Assert.assertEquals("VIPQueue", jd1.getQueue(cnx).getName());
            Assert.assertEquals("VIPQueue", jd2.getQueue(cnx).getName());
            Assert.assertEquals("NormalQueue", jd3.getQueue(cnx).getName());
        }
        catch (NoResultException e)
        {
            Assert.fail("missing configuration element");
        }

        t.delete();
    }

    @Test
    public void testExportQueueAll() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        XmlQueueExporter.export(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", cnx);

        File t = new File(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml");
        Assert.assertEquals(true, t.exists());

        // --> Test Import
        XmlQueueParser.parse(TestHelpers.node.getDlRepo() + "xmlexportqueuetest.xml", cnx);

        try
        {
            Queue.select(cnx, "q_select_by_key", "VIPQueue");
            Queue.select(cnx, "q_select_by_key", "NormalQueue");

            JobDef jd1 = JobDef.select_key(cnx, "Fibo");
            JobDef jd2 = JobDef.select_key(cnx, "Geo");
            JobDef jd3 = JobDef.select_key(cnx, "DateTime");

            Assert.assertEquals("VIPQueue", jd1.getQueue(cnx).getName());
            Assert.assertEquals("VIPQueue", jd2.getQueue(cnx).getName());
            Assert.assertEquals("NormalQueue", jd3.getQueue(cnx).getName());
        }
        catch (NoResultException e)
        {
            Assert.fail("missing configuration element");
        }

        t.delete();
    }

    @Test
    public void testXmlParser()
    {
        // Init the default queue (don't start the engine!)
        Helpers.updateConfiguration(cnx);

        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltest.xml" });

        List<JobDef> jd = JobDef.select(cnx, "jd_select_all");

        Assert.assertEquals(2, jd.size());
        Assert.assertEquals("Fibo", jd.get(0).getApplicationName());
        Assert.assertEquals(true, jd.get(0).isCanBeRestarted());
        Assert.assertEquals("com.enioka.jqm.tests.App", jd.get(0).getJavaClassName());
        Assert.assertEquals(TestHelpers.qVip, jd.get(0).getQueue());
        Assert.assertEquals("ApplicationTest", jd.get(0).getApplication());
        Assert.assertEquals("TestModuleRATONLAVEUR", jd.get(0).getModule());
        Assert.assertEquals(false, jd.get(0).isHighlander());
        Assert.assertEquals("1", jd.get(0).getParameters(cnx).get(0).getValue());
        Assert.assertEquals("2", jd.get(0).getParameters(cnx).get(1).getValue());
    }

    @Test
    public void testExportJobDef() throws Exception
    {
        Map<String, String> jdp = new HashMap<String, String>();
        jdp.put("test-key", "test-value");
        CreationTools.createJobDef("My Description", true, "com.enioka.jqm.tests.App", jdp, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", "App", "ModuleMachin", "other1", "other2", null, false, cnx, "Isolation", true, "HIDDEN");
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime2", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        XmlJobDefExporter.export(TestHelpers.node.getDlRepo() + "xmlexportjobdeftest.xml", cnx);

        File f = new File(TestHelpers.node.getDlRepo() + "xmlexportjobdeftest.xml");
        Assert.assertEquals(true, f.exists());

        // -> Delete all entries and try to reimport them from the exported file
        TestHelpers.cleanup(cnx);

        XmlJobDefParser.parse(TestHelpers.node.getDlRepo() + "xmlexportjobdeftest.xml", cnx);

        // test the 4 JobDef were imported
        JobDef fibo = null;
        try
        {
            fibo = JobDef.select_key(cnx, "Fibo");
            JobDef.select_key(cnx, "Geo");
            JobDef.select_key(cnx, "DateTime");
            JobDef.select_key(cnx, "DateTime2");
        }
        catch (NoResultException e)
        {
            Assert.fail("missing configuration element");
        }

        Assert.assertEquals("My Description", fibo.getDescription());
        Assert.assertEquals("App", fibo.getApplication());
        Assert.assertEquals("jqm-tests/jqm-test-fibo/target/test.jar", fibo.getJarPath());
        Assert.assertEquals("FS", fibo.getPathType().toString());
        Assert.assertEquals("VIPQueue", fibo.getQueue(cnx).getName());
        Assert.assertEquals(true, fibo.isCanBeRestarted());
        Assert.assertEquals("com.enioka.jqm.tests.App", fibo.getJavaClassName());
        Assert.assertEquals("ModuleMachin", fibo.getModule());
        Assert.assertEquals("other1", fibo.getKeyword1());
        Assert.assertEquals("other2", fibo.getKeyword2());
        Assert.assertEquals(null, fibo.getKeyword3());
        Assert.assertEquals(false, fibo.isHighlander());
        Assert.assertEquals("Isolation", fibo.getSpecificIsolationContext());
        Assert.assertEquals(true, fibo.isChildFirstClassLoader());
        Assert.assertEquals("HIDDEN", fibo.getHiddenJavaClasses());

        f.delete();
    }

    @Test
    public void testImportThenReimportJobDefWithPrms()
    {
        // Init the default queue (don't start the engine!)
        Helpers.updateConfiguration(cnx);

        // First import
        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltest.xml" });

        List<JobDef> jd = JobDef.select(cnx, "jd_select_all");
        Assert.assertEquals(2, jd.size());
        Assert.assertEquals("Fibo", jd.get(0).getApplicationName());
        Assert.assertEquals("1", jd.get(0).getParameters(cnx).get(0).getValue());

        // Second import - parameters are different, note 3 instead of 1
        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltest_np.xml" });

        jd = JobDef.select(cnx, "jd_select_all");
        Assert.assertEquals(2, jd.size());
        Assert.assertEquals("Fibo", jd.get(0).getApplicationName());
        Assert.assertEquals("3", jd.get(0).getParameters(cnx).get(0).getValue());
    }

    @Test
    public void testImportJobdefWithQueue()
    {
        // Init the default queue (don't start the engine!)
        Helpers.updateConfiguration(cnx);

        Main.main(new String[] { "-importjobdef", "target/payloads/jqm-test-xml/xmltestnewqueue.xml" });

        List<JobDef> jd = JobDef.select(cnx, "jd_select_all");
        Assert.assertEquals(2, jd.size());

        // Was the queue created (and only once)?
        Queue q = Queue.select_key(cnx, "NewQueue");
        Assert.assertEquals("Created from a jobdef import. Description should be set later", q.getDescription());
        cnx.close();
    }

    @Test
    public void testImportQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, cnx);
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qNormal, 42,
                "DateTime", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        XmlQueueParser.parse("target/payloads/jqm-test-xml/xmlqueuetest.xml", cnx);

        try
        {
            Queue.select(cnx, "q_select_by_key", "XmlQueue");
            Queue.select(cnx, "q_select_by_key", "XmlQueue2");

            JobDef jd1 = JobDef.select_key(cnx, "Fibo");
            JobDef jd2 = JobDef.select_key(cnx, "Geo");
            JobDef jd3 = JobDef.select_key(cnx, "DateTime");

            Assert.assertEquals("XmlQueue", jd1.getQueue(cnx).getName());
            Assert.assertEquals("XmlQueue", jd2.getQueue(cnx).getName());
            Assert.assertEquals("XmlQueue2", jd3.getQueue(cnx).getName());
        }
        catch (NoResultException e)
        {
            Assert.fail("missing configuration element");
        }
    }
}
