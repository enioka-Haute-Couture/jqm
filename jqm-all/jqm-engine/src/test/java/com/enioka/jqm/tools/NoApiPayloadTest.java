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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class NoApiPayloadTest extends JqmBaseTest
{
    @Test
    public void testClassicPayload() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-datetimemaven", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("jqm-test-datetimemaven", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testRunnable() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-runnable/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-runnable", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("jqm-test-runnable", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testRunnableInject() throws Exception
    {
        CreationTools.createJobDef("super app", true, "App", null, "jqm-tests/jqm-test-runnable-inject/target/test.jar", TestHelpers.qVip,
                42, "jqm-test-runnable-inject", "testapp", "Franquin", "ModuleMachin", "other", "other", false, em);
        int i = JobRequest.create("jqm-test-runnable-inject", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(3, 10000, em);

        Assert.assertEquals(3, TestHelpers.getOkCount(em));
        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j order by id asc", History.class).getResultList();
        Assert.assertEquals(1, JqmClientFactory.getClient().getJob(i).getMessages().size()); // 3 auto messages + 1 message per run.
        Assert.assertEquals(100, (int) ji.get(0).getProgress());
    }

    @Test
    public void testMainType() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-main/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-main", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("jqm-test-main", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testMainTypeInject() throws Exception
    {
        CreationTools.createJobDef("super app", true, "App", null, "jqm-tests/jqm-test-main-inject/target/test.jar", TestHelpers.qVip, 42,
                "jqm-test-main-inject", "testapp", "Franquin", "ModuleMachin", "other", "other", false, em);
        int i = JobRequest.create("jqm-test-main-inject", "TestUser").setSessionID("123X").submit();

        addAndStartEngine();
        TestHelpers.waitFor(3, 10000, em);

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j order by id asc", History.class).getResultList();

        Assert.assertEquals(3, ji.size());
        Assert.assertEquals(State.ENDED, ji.get(0).getState());
        Assert.assertEquals(State.ENDED, ji.get(1).getState());

        Assert.assertEquals(1, JqmClientFactory.getClient().getJob(i).getMessages().size()); // 3 auto messages + 1 message per run.
        Assert.assertEquals(100, (int) ji.get(0).getProgress());
    }

    @Test
    public void testProvidedApi() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-providedapi/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);

        // Create an empty lib directory just to be sure no dependencies will be resolved.
        FileUtils.forceMkdir(new File("../jqm-tests/jqm-test-providedapi/target/lib"));

        JobRequest j = new JobRequest("MarsuApplication", "TestUser");
        JqmClientFactory.getClient().enqueue(j);

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        TypedQuery<History> query = em.createQuery("SELECT j FROM History j", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        Assert.assertEquals(1, res.size());
        Assert.assertEquals(State.ENDED, res.get(0).getState());
    }
}
