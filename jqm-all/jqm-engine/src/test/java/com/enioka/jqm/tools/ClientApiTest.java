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
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.api.Query.Sort;
import com.enioka.jqm.api.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class ClientApiTest extends JqmBaseTest
{
    @Test
    public void testRestartJob() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, cnx);

        int i = JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();

        TestHelpers.waitFor(1, 10000, cnx);
        JqmClientFactory.getClient().enqueueFromHistory(i);
        TestHelpers.waitFor(2, 10000, cnx);

        Assert.assertEquals(2, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));

        List<JobInstance> res = Query.create().addSortAsc(Sort.DATEENQUEUE).run();

        Assert.assertEquals(2, res.size());
        Assert.assertEquals("MarsuApplication", res.get(0).getApplicationName());
        Assert.assertEquals("MarsuApplication", res.get(1).getApplicationName());
    }

    @Test
    public void testHistoryFields() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other2", true, cnx);

        int i = JobRequest.create("MarsuApplication", "TestUser").setSessionID("session42").setKeyword1("k1").setKeyword2("k2").submit();

        addAndStartEngine();

        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        JobInstance h = JqmClientFactory.getClient().getJob(i);

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        jqmlogger.debug("enqueueDate: " + df.format(h.getEnqueueDate().getTime()));
        jqmlogger.debug("ExecutionDate: " + df.format(h.getBeganRunningDate().getTime()));
        jqmlogger.debug("EndDate: " + df.format(h.getEndDate().getTime()));

        Assert.assertTrue(h.getEnqueueDate() != null);
        Assert.assertTrue(h.getUser() != null);
        Assert.assertTrue(h.getEndDate() != null);
        Assert.assertTrue(h.getBeganRunningDate() != null);
        Assert.assertTrue(h.getSessionID() != null);
        Assert.assertEquals("session42", h.getSessionID());

        JobInstance ji = Query.create().setApplicationName("MarsuApplication").run().get(0);
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
        int i = JqmSimpleTest.create(cnx, "pyl.KillMe").expectOk(0).addWaitTime(3000).run(this);

        JqmClientFactory.getClient().killJob(i);
        TestHelpers.waitFor(1, 3000, cnx);

        List<JobInstance> res = Query.create().run();
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.KILLED, res.get(0).getState());
    }

    @Test
    public void testGetMsg() throws Exception
    {
        boolean success = false;
        boolean success2 = false;
        boolean success3 = false;

        int i = JqmSimpleTest.create(cnx, "pyl.EngineApiSend3Msg").run(this);

        List<String> ress = JqmClientFactory.getClient().getJobMessages(i);

        Assert.assertEquals(3, ress.size());

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
        int i = JqmSimpleTest.create(cnx, "pyl.EngineApiProgress").addWaitMargin(10000).run(this);
        Integer k = JqmClientFactory.getClient().getJobProgress(i);
        Assert.assertEquals((Integer) 50, k);
    }

    @Test
    public void testPause() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        JqmClientFactory.getClient().pauseQueuedJob(i);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 5000, cnx);

        List<JobInstance> res1 = Query.create().addSortAsc(Sort.DATEENQUEUE).run();
        List<JobInstance> res2 = Query.create().addSortAsc(Sort.DATEENQUEUE).setQueryHistoryInstances(false).setQueryLiveInstances(true)
                .run();

        Assert.assertEquals(1, res1.size());
        Assert.assertEquals(1, res2.size());
        Assert.assertEquals(State.HOLDED, res2.get(0).getState());
        Assert.assertEquals(State.ENDED, res1.get(0).getState());
    }

    @Test
    public void testCancelJob() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        JqmClientFactory.getClient().cancelJob(i);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(2, 5000, cnx);

        List<JobInstance> res = Query.create().addSortAsc(Sort.DATEENQUEUE).run();
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(State.CANCELLED, res.get(0).getState());
        Assert.assertEquals(State.ENDED, res.get(1).getState());
    }

    @Test
    public void testChangeQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        JqmClientFactory.getClient().setJobQueue(i, TestHelpers.qSlow);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, cnx);

        List<JobInstance> res = Query.create().addSortAsc(Sort.DATEENQUEUE).run();
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals("SlowQueue", res.get(0).getQueue().getName());
    }

    @Test
    public void testDelJobInQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);

        int i = JobRequest.create("MarsuApplication", "TestUser").submit();
        JqmClientFactory.getClient().deleteJob(i);

        addAndStartEngine();
        Thread.sleep(1000);

        Assert.assertEquals(0, TestHelpers.getHistoryAllCount(cnx));
        Assert.assertEquals(0, TestHelpers.getQueueAllCount(cnx));
    }

    @Test
    public void testResume() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();

        JqmClientFactory.getClient().pauseQueuedJob(i);
        addAndStartEngine();
        Thread.sleep(3000);
        Assert.assertEquals(0, TestHelpers.getOkCount(cnx));
        JqmClientFactory.getClient().resumeJob(i);

        TestHelpers.waitFor(1, 5000, cnx);
        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
    }

    @Test
    public void testEnqueueWithQueue() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        int i = JobRequest.create("MarsuApplication", "TestUser").setQueueName("NormalQueue").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, cnx);

        JobInstance ji = JqmClientFactory.getClient().getJob(i);
        Assert.assertEquals("NormalQueue", ji.getQueue().getName());
    }

    /**
     * Temp dir should be removed after run
     */
    @Test
    public void testTempDir() throws Exception
    {
        int i = JqmSimpleTest.create(cnx, "pyl.EngineApiTmpDir").run(this);

        File tmpDir = new File(FilenameUtils.concat(TestHelpers.node.getTmpDirectory(), "" + i));
        Assert.assertFalse(tmpDir.isDirectory());
    }

    @Test
    public void testTags() throws Exception
    {
        CreationTools.createJobDef(null, true, "pyl.EngineApiTags", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "keyword1", null, "keyword3", false, cnx);
        JobRequest.create("MarsuApplication", "TestUser").setKeyword1("Houba").setKeyword3("Meuh").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));

        List<JobInstance> res = Query.create().run();

        Assert.assertEquals(1, res.size());
        JobInstance h = res.get(0);

        Assert.assertEquals("Houba", h.getKeyword1());
        Assert.assertEquals(null, h.getKeyword2());
        Assert.assertEquals("Meuh", h.getKeyword3());

        Assert.assertEquals("keyword1", h.getDefinitionKeyword1());
        Assert.assertEquals(null, h.getDefinitionKeyword2());
        Assert.assertEquals("keyword3", h.getDefinitionKeyword3());
    }
}
