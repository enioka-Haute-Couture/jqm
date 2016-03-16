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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class ClientApiTest extends JqmBaseTest
{
    @Test
    public void testRestartJob() throws Exception
    {
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar",
                TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        int i = JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();

        TestHelpers.waitFor(1, 10000, em);
        JqmClientFactory.getClient().enqueueFromHistory(i);
        TestHelpers.waitFor(2, 10000, em);

        Assert.assertEquals(2, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(2, res.size());
        Assert.assertEquals(jdDemoMaven.getId(), res.get(0).getJd().getId());
        Assert.assertEquals(jdDemoMaven.getId(), res.get(1).getJd().getId());
    }

    @Test
    public void testHistoryFields() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other2", true, em);

        int i = JobRequest.create("MarsuApplication", "TestUser").setSessionID("session42").setKeyword1("k1").setKeyword2("k2").submit();

        addAndStartEngine();

        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        History h = em.createQuery("SELECT h FROM History h WHERE h.id = :i", History.class).setParameter("i", i).getSingleResult();

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        jqmlogger.debug("enqueueDate: " + df.format(h.getEnqueueDate().getTime()));
        jqmlogger.debug("ExecutionDate: " + df.format(h.getExecutionDate().getTime()));
        jqmlogger.debug("EndDate: " + df.format(h.getEndDate().getTime()));

        Assert.assertTrue(h.getEnqueueDate() != null);
        Assert.assertTrue(h.getUserName() != null);
        Assert.assertTrue(h.getEndDate() != null);
        Assert.assertTrue(h.getExecutionDate() != null);
        Assert.assertTrue(h.getSessionId() != null);
        Assert.assertEquals("session42", h.getSessionId());

        com.enioka.jqm.api.JobInstance ji = Query.create().setApplicationName("MarsuApplication").run().get(0);
        Assert.assertEquals("ModuleMachin", ji.getDefinitionKeyword1());
        Assert.assertEquals("other", ji.getDefinitionKeyword2());
        Assert.assertEquals("other2", ji.getDefinitionKeyword3());
        Assert.assertEquals("k1", ji.getKeyword1());
        Assert.assertEquals("k2", ji.getKeyword2());
        Assert.assertEquals(null, ji.getKeyword3());
    }

    @Test
    public void testKillJob() throws Exception
    {
        int i = JqmSimpleTest.create(em, "pyl.KillMe").expectOk(0).addWaitTime(3000).run(this);

        JqmClientFactory.getClient().killJob(i);
        TestHelpers.waitFor(1, 3000, em);

        List<History> res = em.createQuery("SELECT j FROM History j", History.class).getResultList();
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.KILLED, res.get(0).getState());
    }

    @Test
    public void testGetMsg() throws Exception
    {
        boolean success = false;
        boolean success2 = false;
        boolean success3 = false;

        int i = JqmSimpleTest.create(em, "pyl.EngineApiSend3Msg").run(this);

        List<String> ress = JqmClientFactory.getClient().getJobMessages(i);
        List<Message> m = em.createQuery("SELECT m FROM Message m WHERE m.ji = :i", Message.class).setParameter("i", i).getResultList();

        Assert.assertEquals(3, ress.size());
        Assert.assertEquals(3, m.size());

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
        int i = JqmSimpleTest.create(em, "pyl.EngineApiProgress").addWaitMargin(10000).run(this);
        Integer k = JqmClientFactory.getClient().getJobProgress(i);
        Assert.assertEquals((Integer) 50, k);
    }

    @Test
    public void testPause() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        JqmClientFactory.getClient().pauseQueuedJob(i);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 5000, em);

        List<History> res1 = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class).getResultList();
        List<JobInstance> res2 = em.createQuery("SELECT j FROM JobInstance j", JobInstance.class).getResultList();

        Assert.assertEquals(1, res1.size());
        Assert.assertEquals(1, res2.size());
        Assert.assertEquals(State.HOLDED, res2.get(0).getState());
        Assert.assertEquals(State.ENDED, res1.get(0).getState());
    }

    @Test
    public void testCancelJob() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        JqmClientFactory.getClient().cancelJob(i);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(2, 5000, em);

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.CANCELLED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());
    }

    @Test
    public void testChangeQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        JqmClientFactory.getClient().setJobQueue(i, TestHelpers.qSlow.getId());

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        List<History> res = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class).getResultList();
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals(TestHelpers.qSlow.getName(), res.get(0).getQueue().getName());
    }

    @Test
    public void testDelJobInQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);

        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        JqmClientFactory.getClient().deleteJob(i);

        addAndStartEngine();
        Thread.sleep(1000);

        Assert.assertEquals(0, TestHelpers.getHistoryAllCount(em));
        Assert.assertEquals(0, TestHelpers.getQueueAllCount(em));
    }

    @Test
    public void testResume() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();

        JqmClientFactory.getClient().pauseQueuedJob(i);
        addAndStartEngine();
        Thread.sleep(3000);
        Assert.assertEquals(0, TestHelpers.getOkCount(em));
        JqmClientFactory.getClient().resumeJob(i);

        TestHelpers.waitFor(1, 5000, em);
        Assert.assertEquals(1, TestHelpers.getOkCount(em));
    }

    @Test
    public void testEnqueueWithQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        int i = JobRequest.create("MarsuApplication", "TestUser").setQueueName(TestHelpers.qNormal.getName()).submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        com.enioka.jqm.api.JobInstance ji = JqmClientFactory.getClient().getJob(i);
        Assert.assertEquals(TestHelpers.qNormal.getName(), ji.getQueue().getName());
    }

    /**
     * Temp dir should be removed after run
     */
    @Test
    public void testTempDir() throws Exception
    {
        int i = JqmSimpleTest.create(em, "pyl.EngineApiTmpDir").run(this);

        File tmpDir = new File(FilenameUtils.concat(TestHelpers.node.getTmpDirectory(), "" + i));
        Assert.assertFalse(tmpDir.isDirectory());
    }

    @Test
    public void testTags() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiTags", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "keyword1", null, "keyword3", false, em);
        JobRequest.create("MarsuApplication", "TestUser").setKeyword1("Houba").setKeyword3("Meuh").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));

        History h = Helpers.getNewEm().createQuery("SELECT j FROM History j", History.class).getSingleResult();

        Assert.assertEquals("Houba", h.getInstanceKeyword1());
        Assert.assertEquals(null, h.getInstanceKeyword2());
        Assert.assertEquals("Meuh", h.getInstanceKeyword3());

        Assert.assertEquals("keyword1", h.getKeyword1());
        Assert.assertEquals(null, h.getKeyword2());
        Assert.assertEquals("keyword3", h.getKeyword3());
    }
}
