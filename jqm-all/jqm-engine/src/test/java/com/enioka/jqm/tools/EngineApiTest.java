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

import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class EngineApiTest extends JqmBaseTest
{
    @Test
    public void testSendMsg() throws Exception
    {
        boolean success = false;
        boolean success2 = false;
        boolean success3 = false;

        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-sendmsg/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        int i = JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        List<History> res = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class).getResultList();
        List<Message> m = em.createQuery("SELECT m FROM Message m WHERE m.ji = :i", Message.class).setParameter("i", i).getResultList();

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
    public void testSendProgress() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-sendprogress/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, em);

        List<History> res = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class).getResultList();
        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
        Assert.assertEquals((Integer) 50, res.get(0).getProgress());
    }

    /**
     * To stop a job, just throw an exception
     */
    @Test
    public void testThrowable() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-throwable/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        History ji1 = em.createQuery("SELECT j FROM History j", History.class).getSingleResult();
        Assert.assertEquals(State.CRASHED, ji1.getState());
    }

    /**
     * A parent job can wait for all its children - then its end date should be after the end date of the children.
     */
    @Test
    public void testWaitChildren() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-waitall/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(6, 10000, em);

        List<History> ji = Helpers.getNewEm()
                .createQuery("SELECT j FROM History j WHERE j.status = 'ENDED' ORDER BY j.id ASC", History.class).getResultList();
        Assert.assertEquals(6, ji.size());

        Calendar parentEnd = ji.get(0).getEndDate();
        for (int i = 1; i < 6; i++)
        {
            Assert.assertTrue(parentEnd.after(ji.get(i).getEndDate()));
        }
    }

    @Test
    public void testGetChildrenStatus() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.EngineApiGetStatus").expectNonOk(1).expectOk(2).run(this);
    }
}
