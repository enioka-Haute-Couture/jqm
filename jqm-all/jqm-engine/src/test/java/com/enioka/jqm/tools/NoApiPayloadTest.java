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
import com.enioka.jqm.jpamodel.JobDef;
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
        JqmSimpleTest.create(em, "pyl.PckRunnable", "jqm-test-pyl-nodep").run(this);
    }

    @Test
    public void testRunnableInject() throws Exception
    {
        int i = JqmSimpleTest.create(em, "pyl.EngineApiInjectThread").expectOk(3).run(this);

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j order by id asc", History.class).getResultList();
        Assert.assertEquals(1, JqmClientFactory.getClient().getJob(i).getMessages().size()); // 1 message per run.
        Assert.assertEquals(100, (int) ji.get(0).getProgress());
    }

    @Test
    public void testMainType() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.PckMain", "jqm-test-pyl-nodep").run(this);
    }

    @Test
    public void testMainTypeInject() throws Exception
    {
        int i = JqmSimpleTest.create(em, "pyl.EngineApiInject").setSessionId("123X").expectOk(3).run(this);

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j order by id asc", History.class).getResultList();

        Assert.assertEquals(1, JqmClientFactory.getClient().getJob(i).getMessages().size()); // 1 message per run created by payload
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

    @Test
    public void testDisabledPayload() throws Exception
    {
        JobDef jd = CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemaven/target/test.jar",
                TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        em.getTransaction().begin();
        jd.setEnabled(false);
        em.getTransaction().commit();
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        History h = em.createQuery("SELECT j FROM History j", History.class).getSingleResult();

        Assert.assertEquals(-1, (int) h.getProgress());
    }

    @Test
    public void testMainTypeInjectWithFullApi() throws Exception
    {
        // Here, engine API + full API mix.
        int i = JqmSimpleTest.create(em, "pyl.EngineApiInject", "jqm-test-pyl-hibapi").setSessionId("123X").expectOk(3).run(this);

        List<History> ji = Helpers.getNewEm().createQuery("SELECT j FROM History j order by id asc", History.class).getResultList();

        Assert.assertEquals(1, JqmClientFactory.getClient().getJob(i).getMessages().size()); // 1 message per run created by payload
        Assert.assertEquals(100, (int) ji.get(0).getProgress());
    }
}
