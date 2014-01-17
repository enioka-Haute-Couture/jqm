/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.jqm.api.Deliverable;
import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.State;

public class JobBaseTest
{
    public static Logger jqmlogger = Logger.getLogger(JobBaseTest.class);
    public static Server s;

    @BeforeClass
    public static void testInit() throws InterruptedException
    {
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();

        Dispatcher.resetEM();
        Helpers.resetEmf();

        jqmlogger.debug("log init");
    }

    @AfterClass
    public static void stop()
    {
        Dispatcher.resetEM();
        s.shutdown();
        s.stop();
    }

    @Test
    public void testHighlanderMode() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderMode");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        Dispatcher.enQueue(j);
        Dispatcher.enQueue(j);

        // em.getTransaction().begin();
        //
        // JobInstance ji = em.createQuery("SELECT j FROM JobInstance j WHERE j.position = :myId AND j.jd.id = :i", JobInstance.class)
        // .setParameter("myId", 2).setParameter("i", jdDemoMaven.getId()).getSingleResult();
        //
        // em.createQuery("UPDATE JobInstance j SET j.state = 'ATTRIBUTED' WHERE j.id = :idJob").setParameter("idJob", ji.getId())
        // .executeUpdate();
        //
        // em.getTransaction().commit();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        EntityManager emm = Helpers.getNewEm();

        ArrayList<History> res = (ArrayList<History>) emm.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class)
                .getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testHighlanderMode2() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderMode2");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);
        List<JobParameter> jps = new ArrayList<JobParameter>();

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        em.getTransaction().begin();

        JobInstance j = CreationTools.createJobInstance(jdDemoMaven, jps, "MAG", null, State.SUBMITTED, 2, TestHelpers.qVip, null, em);
        JobInstance jj = CreationTools.createJobInstance(jdDemoMaven, jps, "MAG", null, State.RUNNING, 1, TestHelpers.qVip, null, em);

        @SuppressWarnings("unused")
        History h = CreationTools.createhistory(null, null, jdDemoMaven, null, TestHelpers.qVip, null, null, j, null, null, null, null,
                TestHelpers.node, null, em);
        @SuppressWarnings("unused")
        History hh = CreationTools.createhistory(null, null, jdDemoMaven, null, TestHelpers.qVip, null, null, jj, null, null, null, null,
                TestHelpers.node, null, em);

        em.getTransaction().commit();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(2, 10000);
        engine1.stop();

        EntityManager emm = Helpers.getNewEm();

        ArrayList<JobInstance> res = (ArrayList<JobInstance>) emm.createQuery(
                "SELECT j FROM JobInstance j ORDER BY j.internalPosition ASC", JobInstance.class).getResultList();

        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.RUNNING, res.get(0).getState());
        Assert.assertEquals(State.SUBMITTED, res.get(1).getState());
    }

    @Test
    public void testHighlanderModeMultiQueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHighlanderModeMultiQueue");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        Dispatcher.enQueue(j);
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        EntityManager emm = Helpers.getNewEm();

        ArrayList<History> res = (ArrayList<History>) emm
                .createQuery("SELECT j FROM History j WHERE j.jd.id = :j ORDER BY j.enqueueDate ASC", History.class)
                .setParameter("j", jdDemoMaven.getId()).getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testGetDeliverables() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetDeliverables");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "jqm-test-deliverable.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/", "jqm-test-deliverable/jqm-test-deliverable.jar",
                TestHelpers.qVip, 42, "getDeliverables", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("getDeliverables", "MAG");
        int id = Dispatcher.enQueue(j);

        TestHelpers.printJobInstanceTable();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);

        TestHelpers.printJobInstanceTable();

        Dispatcher.resetEM();
        List<InputStream> tmp = Dispatcher.getDeliverables(id);
        engine1.stop();
        Assert.assertEquals(1, tmp.size());

        Assert.assertTrue(tmp.get(0).available() > 0);

        tmp.get(0).close();
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "" + id)));
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "getDeliverables")));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + id));
    }

    @Test
    public void testGetOneDeliverable() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetOneDeliverable");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable42.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/", "jqm-test-deliverable/jqm-test-deliverable.jar",
                TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "Franquin");

        int jobId = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);

        File f = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable42.txt");
        Assert.assertEquals(false, f.exists()); // file should have been moved

        Dispatcher.resetEM();
        List<com.enioka.jqm.api.Deliverable> files = Dispatcher.getAllDeliverables(jobId);
        Assert.assertEquals(1, files.size());

        InputStream tmp = Dispatcher.getOneDeliverable(files.get(0));
        engine1.stop();
        Assert.assertTrue(tmp.available() > 0);

        (new File(files.get(0).getFilePath())).delete();
        tmp.close();
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "" + jobId)));
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "MarsuApplication")));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + jobId));
    }

    @Test
    public void testGetUserDeliverables() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetUserDeliverables");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
        JobDefParameter jdpp = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdpp);

        ArrayList<JobDefParameter> jdargs2 = new ArrayList<JobDefParameter>();
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
        JobDefParameter jdpp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable2.txt", em);
        jdargs2.add(jdp2);
        jdargs2.add(jdpp2);

        ArrayList<JobDefParameter> jdargs3 = new ArrayList<JobDefParameter>();
        JobDefParameter jdp3 = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
        JobDefParameter jdpp3 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable3.txt", em);
        jdargs3.add(jdp3);
        jdargs3.add(jdpp3);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/",
                "jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication1", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven2 = CreationTools.createJobDef(null, true, "App", jdargs2, "jqm-test-deliverable/",
                "jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication2", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven3 = CreationTools.createJobDef(null, true, "App", jdargs3, "jqm-test-deliverable/",
                "jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication3", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        JobDefinition j1 = new JobDefinition("MarsuApplication1", "Franquin");
        JobDefinition j2 = new JobDefinition("MarsuApplication2", "Franquin");
        JobDefinition j3 = new JobDefinition("MarsuApplication3", "Franquin");

        int id1 = Dispatcher.enQueue(j1);
        int id2 = Dispatcher.enQueue(j2);
        int id3 = Dispatcher.enQueue(j3);

        TestHelpers.printJobInstanceTable();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(3, 10000);
        engine1.stop();

        TestHelpers.printJobInstanceTable();

        File f1 = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable.txt");
        File f2 = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable2.txt");
        File f3 = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable3.txt");

        // Files moved?
        Assert.assertEquals(false, f1.exists());
        Assert.assertEquals(false, f2.exists());
        Assert.assertEquals(false, f3.exists());

        TestHelpers.printJobInstanceTable();

        List<com.enioka.jqm.api.Deliverable> tmp = Dispatcher.getUserDeliverables("Franquin");

        Assert.assertEquals(3, tmp.size());
        for (Deliverable d : tmp)
        {
            FileUtils.deleteQuietly(new File(d.getFilePath()));
        }

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(3, res.size());
        for (History jobInstance : res)
        {
            Assert.assertEquals(State.ENDED, jobInstance.getState());
        }

        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "" + id1)));
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "" + id2)));
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "" + id3)));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + id1));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + id2));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + id3));
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "MarsuApplication1")));
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "MarsuApplication2")));
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "MarsuApplication3")));
    }

    @Test
    public void testGetQueues() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetQueues");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<Queue> qs = (ArrayList<Queue>) em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList();

        Assert.assertEquals(9, qs.size());
    }

    // This test is weird - it tests nothing really important?
    @Test
    public void testGoodOrder() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGoodOrder");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qNormal, 42, "MarsuApplication2", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        @SuppressWarnings("unused")
        JobDef jd2 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qSlow, 42, "MarsuApplication3", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
        JobDefinition jj = new JobDefinition("MarsuApplication2", "Franquin");
        JobDefinition jjj = new JobDefinition("MarsuApplication3", "Franquin");

        Dispatcher.enQueue(j);
        Dispatcher.enQueue(j);
        Dispatcher.enQueue(j);
        Dispatcher.enQueue(jj);
        Dispatcher.enQueue(jj);
        Dispatcher.enQueue(jjj);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        Thread.sleep(10);

        Dispatcher.enQueue(jjj);
        Dispatcher.enQueue(j);
        Dispatcher.enQueue(jj);
        Dispatcher.enQueue(j);

        TestHelpers.waitFor(10, 10000);
        engine1.stop();

        TypedQuery<History> query = em
                .createQuery("SELECT j FROM History j WHERE j.queue = :q ORDER BY j.executionDate ASC", History.class);
        query.setParameter("q", TestHelpers.qVip);
        ArrayList<History> resVIP = (ArrayList<History>) query.getResultList();

        TypedQuery<History> query2 = em.createQuery("SELECT j FROM History j WHERE j.queue = :q ORDER BY j.executionDate ASC",
                History.class);
        query2.setParameter("q", TestHelpers.qNormal);
        ArrayList<History> resNormal = (ArrayList<History>) query.getResultList();

        TypedQuery<History> query3 = em.createQuery("SELECT j FROM History j WHERE j.queue = :q ORDER BY j.executionDate ASC",
                History.class);
        query3.setParameter("q", TestHelpers.qSlow);
        ArrayList<History> resSlow = (ArrayList<History>) query.getResultList();

        for (int i = 0; i < resVIP.size() - 1; i++)
        {
            Assert.assertNotEquals(resVIP.get(i).getExecutionDate(), resVIP.get(i + 1).getExecutionDate());
        }

        for (int i = 0; i < resNormal.size() - 1; i++)
        {
            Assert.assertNotEquals(resNormal.get(i).getExecutionDate(), resNormal.get(i + 1).getExecutionDate());
        }

        for (int i = 0; i < resSlow.size() - 1; i++)
        {
            Assert.assertNotEquals(resSlow.get(i).getExecutionDate(), resSlow.get(i + 1).getExecutionDate());
        }
    }

    @Test
    public void testPomError() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testPomError");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/pom_error.xml",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDef jdDemoMaven2 = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication2", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
        JobDefinition jj = new JobDefinition("MarsuApplication2", "MAG");

        Dispatcher.enQueue(j);
        Dispatcher.enQueue(jj);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(2, 10000);
        engine1.stop();

        History ji1 = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId", History.class)
                .setParameter("myId", jdDemoMaven.getId()).getSingleResult();

        History ji2 = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId", History.class)
                .setParameter("myId", jdDemoMaven2.getId()).getSingleResult();

        Assert.assertEquals(State.CRASHED, ji1.getState());
        Assert.assertEquals(State.ENDED, ji2.getState());
    }

    @Test
    public void testRestartJob() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testRestartJob");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);
        Dispatcher.restartJob(i);
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(2, res.size());
        Assert.assertEquals(jdDemoMaven.getId(), res.get(0).getJd().getId());
        Assert.assertEquals(jdDemoMaven.getId(), res.get(1).getJd().getId());

    }

    @Test
    public void testPomOnlyInJar() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testPomOnlyInJar");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemavennopom/",
                "jqm-test-datetimemavennopom/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testNoPomLibDir() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testNoPomLibDir");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemavennopomlib/",
                "jqm-test-datetimemavennopomlib/jqm-test-datetimemavennopomlib.jar", TestHelpers.qVip, 42, "MarsuApplication", null,
                "Franquin", "ModuleMachin", "other", "other", false, em);
        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testEmail() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testSendEmail");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG", "jqm.noreply@gmail.com");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 20000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testRestartCrashedJob() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testRestartCrashedJob");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);

        em.getTransaction().begin();
        JobInstance ji = em.find(JobInstance.class, i);
        ji.setState(State.CRASHED);
        em.getTransaction().commit();

        TestHelpers.printJobInstanceTable();

        History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstanceId = :j", History.class).setParameter("j", ji.getId())
                .getSingleResult();
        em.getTransaction().begin();
        Message m = new Message();
        m.setHistory(h);
        m.setTextMessage("Status updated: CRASHED");
        em.persist(m);
        em.getTransaction().commit();
        TestHelpers.printJobInstanceTable();

        @SuppressWarnings("unused")
        Message mm = em.createQuery("SELECT m FROM Message m WHERE m.history = :h", Message.class).setParameter("h", h).getSingleResult();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        Thread.sleep(4000);

        Dispatcher.restartCrashedJob(i);
        TestHelpers.printJobInstanceTable();

        Thread.sleep(4000);
        engine1.stop();

        em.refresh(h);
        Assert.assertEquals(State.ENDED, h.getState());
        Assert.assertEquals(jdDemoMaven.getId(), h.getJd().getId());
    }

    @Test
    public void testGetAllDeliverables() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetAllDeliverables");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);
        EntityManager emm = Helpers.getNewEm();

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("filepath", "./testprojects/jqm-test-deliverable/", em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("fileName", "JobGenADeliverable42.txt", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-deliverable/",
                "jqm-test-deliverable/jqm-test-deliverable.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "Franquin");

        int id = Dispatcher.enQueue(j);

        JobInstance ji = emm.createQuery("SELECT j FROM JobInstance j WHERE j.jd.id = :myId", JobInstance.class)
                .setParameter("myId", jdDemoMaven.getId()).getSingleResult();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);

        File f = new File("./testprojects/jqm-test-deliverable/JobGenADeliverable42.txt");
        Assert.assertEquals(false, f.exists());

        Dispatcher.resetEM();
        List<com.enioka.jqm.api.Deliverable> tmp = Dispatcher.getAllDeliverables(ji.getId());
        engine1.stop();

        Assert.assertEquals(1, tmp.size());
        (new File(tmp.get(0).getFilePath())).delete();
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "" + id)));
        FileUtils.deleteDirectory(new File(FilenameUtils.concat(TestHelpers.node.getDlRepo(), "MarsuApplication")));
        FileUtils.deleteDirectory(new File(System.getProperty("java.io.tmpdir") + "/" + id));
    }

    @Test
    public void testSendMsg() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testSendMsg");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);
        boolean success = false;
        boolean success2 = false;
        boolean success3 = false;

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendmsg/",
                "jqm-test-sendmsg/jqm-test-sendmsg.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin",
                "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);

        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        ArrayList<Message> m = (ArrayList<Message>) em
                .createQuery("SELECT m FROM Message m WHERE m.history.jobInstanceId = :i", Message.class).setParameter("i", i)
                .getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());

        for (Message msg : m)
        {
            if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!"))
            {
                success = true;
            }
            if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!2"))
            {
                success2 = true;
            }
            if (msg.getTextMessage().equals("Les marsus sont nos amis, il faut les aimer aussi!3"))
            {
                success3 = true;
            }
        }

        Assert.assertEquals(true, success);
        Assert.assertEquals(true, success2);
        Assert.assertEquals(true, success3);
    }

    @Test
    public void testHistoryFields() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHistoryFields");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
        j.setSessionID("session42");

        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstanceId = :i", History.class).setParameter("i", i)
                .getSingleResult();

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        jqmlogger.debug("EnqueueDate: " + df.format(h.getEnqueueDate().getTime()));
        jqmlogger.debug("ReturnedValue: " + h.getReturnedValue());
        jqmlogger.debug("ExecutionDate: " + df.format(h.getExecutionDate().getTime()));
        jqmlogger.debug("EndDate: " + df.format(h.getEndDate().getTime()));

        Assert.assertTrue(h.getEnqueueDate() != null);
        Assert.assertTrue(h.getReturnedValue() != null);
        Assert.assertTrue(h.getUserName() != null);
        Assert.assertTrue(h.getEndDate() != null);
        Assert.assertTrue(h.getExecutionDate() != null);
        Assert.assertTrue(h.getSessionId() != null);
    }

    @Test
    public void testSendProgress() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testSendProgress");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendprogress/",
                "jqm-test-sendprogress/jqm-test-sendprogress.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 20000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals((Integer) 50, res.get(0).getProgress());
    }

    @Test
    public void testKillJob() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testKillJob");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-kill/", "jqm-test-kill/jqm-test-kill.jar",
                TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);
        // Dispatcher.enQueue(j);

        TestHelpers.printJobInstanceTable();

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        Thread.sleep(3000);

        Dispatcher.killJob(i);
        TestHelpers.printJobInstanceTable();

        Thread.sleep(3000);

        engine1.stop();

        TestHelpers.printJobInstanceTable();

        TypedQuery<History> query = Helpers.getNewEm().createQuery("SELECT j FROM History j", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.KILLED, res.get(0).getState());
    }

    @Test
    public void testLibInJar() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testLibInJar");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemavenlib/",
                "jqm-test-datetimemavenlib/jqm-test-datetime.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testPomOnlyInJar2() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testPomOnlyInJar2");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemavennopom/",
                "jqm-test-datetimemavennopom/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testNothing() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testNothing");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemavennopom/",
                "jqm-test-datetimemavennopom/nothing.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin",
                "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.CRASHED, res.get(0).getState());
    }

    @Test
    public void testParent() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testParent");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemavenparent/",
                "jqm-test-datetimemavenparent/jqm-test-datetimemavenparent.jar", TestHelpers.qVip, 42, "MarsuApplication", null,
                "Franquin", "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testGetMsg() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetMsg");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);
        boolean success = false;
        boolean success2 = false;
        boolean success3 = false;

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendmsg/",
                "jqm-test-sendmsg/jqm-test-sendmsg.jar", TestHelpers.qVip, 42, "Marsu-Application", null, "Franquin", "ModuleMachin",
                "other", "other", true, em);

        JobDefinition j = new JobDefinition("Marsu-Application", "MAG");

        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        ArrayList<String> ress = (ArrayList<String>) Dispatcher.getMsg(i);

        @SuppressWarnings("unused")
        ArrayList<Message> m = (ArrayList<Message>) em
                .createQuery("SELECT m FROM Message m WHERE m.history.jobInstanceId = :i", Message.class).setParameter("i", i)
                .getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());

        for (int k = 0; k < ress.size(); k++)
        {
            if (ress.get(k).equals("Les marsus sont nos amis, il faut les aimer aussi!"))
            {
                success = true;
            }
            if (ress.get(k).equals("Les marsus sont nos amis, il faut les aimer aussi!2"))
            {
                success2 = true;
            }
            if (ress.get(k).equals("Les marsus sont nos amis, il faut les aimer aussi!3"))
            {
                success3 = true;
            }
        }

        Assert.assertEquals(true, success);
        Assert.assertEquals(true, success2);
        Assert.assertEquals(true, success3);
    }

    @Test
    public void testGetProgress() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGetProgress");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendprogress/",
                "jqm-test-sendprogress/jqm-test-sendprogress.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Integer k = Dispatcher.getProgress(i);

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals((Integer) 50, k);
    }

    // @Test
    public void testCrashPurge() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testCrashPurge");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);

        em.getTransaction().begin();
        JobInstance t = em.find(JobInstance.class, i);
        t.setState(State.CRASHED);

        History h = em.createQuery("SELECT j FROM History j WHERE j.id = 1", History.class).getSingleResult();

        Calendar tmp = GregorianCalendar.getInstance(Locale.getDefault());
        h.setEndDate(DateUtils.toCalendar(DateUtils.addDays(tmp.getTime(), -11)));
        em.getTransaction().commit();
        em.refresh(em.merge(h));
        em.clear();

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        jqmlogger.debug("EndDate: " + df.format(h.getEndDate().getTime()));

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        Thread.sleep(3000);

        engine1.stop();

        ArrayList<JobInstance> r = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j", JobInstance.class)
                .getResultList();

        Assert.assertEquals(0, r.size());
    }

    @Test
    public void testPause() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testPause");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);
        @SuppressWarnings("unused")
        int ii = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        Dispatcher.pauseJob(i);
        TestHelpers.printJobInstanceTable();
        engine1.start("localhost");
        Thread.sleep(5000);

        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.HOLDED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());
    }

    @Test
    public void testLongName() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testLongName");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42,
                "Marsu-Application-nnnnnnnn-nnnnnn-nnnnnnnnnn-nnNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNnn-nnnnnnnnnnnnnn", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobDefinition j = new JobDefinition(
                "Marsu-Application-nnnnnnnn-nnnnnn-nnnnnnnnnn-nnNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNnn-nnnnnnnnnnnnnn", "MAG");

        @SuppressWarnings("unused")
        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }

    @Test
    public void testCancelJob() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testCancelJob");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);
        @SuppressWarnings("unused")
        int ii = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        Dispatcher.cancelJobInQueue(i);
        TestHelpers.printJobInstanceTable();
        engine1.start("localhost");
        Thread.sleep(5000);

        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.CANCELLED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());
    }

    @Test
    public void testChangeQueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testChanegQueue");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        Dispatcher.changeQueue(i, TestHelpers.qSlow);
        TestHelpers.printJobInstanceTable();

        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals(TestHelpers.qSlow.getName(), res.get(0).getQueue().getName());
    }

    @Test
    public void testDelJobInQueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testDelJobInQueue");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        Dispatcher.delJobInQueue(i);
        TestHelpers.printJobInstanceTable();
        engine1.start("localhost");
        Thread.sleep(1000);

        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        ArrayList<JobInstance> js = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j", JobInstance.class)
                .getResultList();

        Assert.assertEquals(0, res.size());
        Assert.assertEquals(0, js.size());
    }

    @Test
    public void testHttpEnqueue() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHttpEnqueue");

        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendmsg/",
                "jqm-test-sendmsg/jqm-test-sendmsg.jar", TestHelpers.qVip, 42, "Marsu-Application", null, "Franquin", "ModuleMachin",
                "other", "other", true, em);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        em.refresh(TestHelpers.node);
        HttpPost post = new HttpPost("http://localhost:" + TestHelpers.node.getPort() + "/enqueue");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("applicationname", "Marsu-Application"));
        nvps.add(new BasicNameValuePair("user", "testuser"));
        nvps.add(new BasicNameValuePair("module", "testuser"));
        nvps.add(new BasicNameValuePair("param_1", "arg"));
        nvps.add(new BasicNameValuePair("paramvalue_1", "newvalue"));
        post.setEntity(new UrlEncodedFormEntity(nvps));

        HttpClient client = HttpClients.createDefault();
        HttpResponse res = client.execute(post);

        Assert.assertEquals(200, res.getStatusLine().getStatusCode());

        HttpEntity entity = res.getEntity();
        InputStream in = entity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer);
        String result = writer.toString();
        IOUtils.closeQuietly(in);

        Integer jid = 0;
        try
        {
            jid = Integer.parseInt(result);
        }
        catch (Exception e)
        {
            Assert.fail("result was not an integer " + e.getMessage());
        }

        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        // Check run is OK
        History h = em.createQuery("SELECT j FROM History j", History.class).getSingleResult();
        Assert.assertEquals(State.ENDED, h.getStatus());
        Assert.assertEquals(jid, h.getJobInstanceId());
    }

    @Test
    public void testHttpStatus() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testHttpEnqueue");

        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-sendmsg/",
                "jqm-test-sendmsg/jqm-test-sendmsg.jar", TestHelpers.qVip, 42, "Marsu-Application", null, "Franquin", "ModuleMachin",
                "other", "other", true, em);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        em.refresh(TestHelpers.node);
        HttpPost post = new HttpPost("http://localhost:" + TestHelpers.node.getPort() + "/enqueue");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("applicationname", "Marsu-Application"));
        nvps.add(new BasicNameValuePair("user", "testuser"));
        nvps.add(new BasicNameValuePair("module", "testuser"));
        nvps.add(new BasicNameValuePair("param_1", "arg"));
        nvps.add(new BasicNameValuePair("paramvalue_1", "newvalue"));
        post.setEntity(new UrlEncodedFormEntity(nvps));

        HttpClient client = HttpClients.createDefault();
        HttpResponse res = client.execute(post);

        Assert.assertEquals(200, res.getStatusLine().getStatusCode());

        HttpEntity entity = res.getEntity();
        InputStream in = entity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(in, writer);
        String result = writer.toString();
        IOUtils.closeQuietly(in);

        Integer jid = 0;
        try
        {
            jid = Integer.parseInt(result);
        }
        catch (Exception e)
        {
            Assert.fail("result was not an integer " + e.getMessage());
        }

        TestHelpers.waitFor(1, 10000);

        HttpGet rq = new HttpGet("http://localhost:" + TestHelpers.node.getPort() + "/status?id=" + jid);
        res = client.execute(rq);
        Assert.assertEquals(200, res.getStatusLine().getStatusCode());

        entity = res.getEntity();
        in = entity.getContent();
        writer = new StringWriter();
        IOUtils.copy(in, writer);
        State currentState = State.valueOf(writer.toString());
        IOUtils.closeQuietly(in);
        engine1.stop();

        Assert.assertEquals(State.ENDED, currentState);
    }

    @Test
    public void testResume() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testResume");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");

        int i = Dispatcher.enQueue(j);
        @SuppressWarnings("unused")
        int ii = Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        Dispatcher.pauseJob(i);
        TestHelpers.printJobInstanceTable();
        engine1.start("localhost");
        Thread.sleep(5000);
        Dispatcher.resumeJob(i);
        Thread.sleep(5000);

        engine1.stop();

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());
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
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, "jqm-test-fibo/", "jqm-test-fibo/jqm-test-fibo.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-geo/", "jqm-test-geo/jqm-test-geo.jar", TestHelpers.qVip, 42,
                "Geo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-datetimemaven/",
                "jqm-test-datetimemaven/jqm-test-datetimemaven.jar", TestHelpers.qVip, 42, "DateTime", null, "Franquin", "ModuleMachin",
                "other", "other", false, em);

        QueueXmlParser qxp = new QueueXmlParser();
        qxp.parse("testprojects/jqm-test-xml/xmlqueuetest.xml");

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

    @Test
    public void testThrowable() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testThrowable");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-throwable",
                "jqm-test-throwable/jqm-test-throwable.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin",
                "other", "other", false, em);

        JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        History ji1 = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId", History.class)
                .setParameter("myId", jd.getId()).getSingleResult();

        Assert.assertEquals(State.CRASHED, ji1.getState());
    }

    @Test
    public void testJobWithPersistenceUnit() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testJobWithPersistenceUnit");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        CreationTools.createDatabaseProp("jdbc/test", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbmarsu", "SA", "", em);
        CreationTools.createDatabaseProp("jdbc/jqm2", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:testdbengine", "SA", "", em);
        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-em", "jqm-test-em/jqm-test-em.jar", TestHelpers.qVip,
                42, "jqm-test-em", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("jqm-test-em", "MAG");
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(2, 10000);
        engine1.stop();

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId order by id asc", History.class)
                .setParameter("myId", jd.getId()).getResultList();

        Assert.assertEquals(2, ji.size());
        Assert.assertEquals(State.ENDED, ji.get(0).getState());
        Assert.assertEquals(State.ENDED, ji.get(1).getState());
    }

    @Test
    public void testJobWithSystemExit() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testJobWithSystemExit");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-test-exit", "jqm-test-exit/jqm-test-exit.jar",
                TestHelpers.qVip, 42, "jqm-test-exit", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        JobDefinition j = new JobDefinition("jqm-test-exit", "MAG");
        Dispatcher.enQueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000);
        engine1.stop();

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j WHERE j.jd.id = :myId order by id asc", History.class)
                .setParameter("myId", jd.getId()).getResultList();

        Assert.assertEquals(1, ji.size());
        Assert.assertEquals(State.CRASHED, ji.get(0).getState());
    }
}
