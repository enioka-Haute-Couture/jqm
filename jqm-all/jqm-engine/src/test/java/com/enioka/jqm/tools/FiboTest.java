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

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.State;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class FiboTest extends JqmBaseTest
{
    @Test
    public void testFibo() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testFibo");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, null,
                "jqm-tests/jqm-test-fibo/target/test.jar", TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1",
                "other2", false, em);

        JobRequest form = new JobRequest("Fibo", "MAG");
        form.addParameter("p1", "1");
        form.addParameter("p2", "2");
        JqmClientFactory.getClient().enqueue(form);

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");

        TestHelpers.waitFor(11, 15000, em);
        engine1.stop();

        long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
        Assert.assertEquals(11, i);
    }

    @Test
    public void testenqueueSynchronously() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testenqueueSynchronously");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", jdargs, null,
                "jqm-tests/jqm-test-fibosync/target/test.jar", TestHelpers.qVip, 42, "FiboSync", null, "Franquin", "ModuleMachin", "other",
                "other", false, em);

        JobRequest j = new JobRequest("FiboSync", "MAG");
        j.addParameter("p1", "1");
        j.addParameter("p2", "2");
        int i = JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(11, 30000, em);

        engine1.stop();
        long ii = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
        Assert.assertEquals(11, ii);
        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.endDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();
        for (History history : res)
        {
            Assert.assertEquals(State.ENDED, history.getState());
        }
        TestHelpers.printJobInstanceTable(em);
        Assert.assertEquals(i, (int) res.get(res.size() - 1).getId());
    }
}
