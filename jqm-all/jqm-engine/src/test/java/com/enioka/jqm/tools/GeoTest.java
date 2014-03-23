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

public class GeoTest extends JqmBaseTest
{
    @Test
    public void testGeo() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testGeo");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createLocalNode(em);

        em.getTransaction().begin();
        TestHelpers.node.setRootLogLevel("INFO");
        TestHelpers.nodeMix.setRootLogLevel("INFO");
        TestHelpers.nodeMix2.setRootLogLevel("INFO");
        em.getTransaction().commit();

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jd = CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42,
                "Geo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);

        JobRequest form = new JobRequest("Geo", "MAG");
        form.addParameter("nbJob", "1");
        JqmClientFactory.getClient().enqueue(form);

        // Start the engine
        JqmEngine engine1 = new JqmEngine();
        JqmEngine engine2 = new JqmEngine();
        JqmEngine engine3 = new JqmEngine();
        engine1.start("localhost");
        engine2.start("localhost4");
        engine3.start("localhost5");

        TestHelpers.waitFor(511, 60000, em);
        engine1.stop();
        engine2.stop();
        engine3.stop();

        long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
        Assert.assertTrue(i > 3);
        TypedQuery<History> query = em.createQuery("SELECT j FROM History j ORDER BY j.enqueueDate ASC", History.class);
        ArrayList<History> res = (ArrayList<History>) query.getResultList();

        for (History history : res)
        {
            if (history.getState().equals(State.CRASHED))
            {
                Assert.fail("No job should be crashed");
            }
        }

        Assert.assertEquals(511, res.size());
    }
}
