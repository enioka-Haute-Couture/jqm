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

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class FiboTest extends JqmBaseTest
{
    @Before
    public void b()
    {
        TestHelpers.setNodesLogLevel("INFO", em);
    }

    @After
    public void a()
    {
        Logger.getRootLogger().setLevel(Level.toLevel("DEBUG"));
        Logger.getLogger("com.enioka").setLevel(Level.toLevel("DEBUG"));
    }

    @Test
    public void testFibo() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, "jqm-tests/jqm-test-fibo/target/test.jar",
                TestHelpers.qVip, 42, "Fibo", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        JobRequest.create("Fibo", "TestUser").addParameter("p1", "1").addParameter("p2", "2").submit();

        // Start one engine
        addAndStartEngine();

        // 1: (1,2) - 2: (2,3) - 3: (3,5) - 4: (5,8) - 5: (8,13) - 6: (13,21) - 7: (21,34) - 8: (34,55) - 9: (55,89) - 10: (89,144) -
        // 11: (134,233)
        TestHelpers.waitFor(11, 15000, em);

        Assert.assertEquals(11, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testenqueueSynchronously() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, "jqm-tests/jqm-test-fibosync/target/test.jar",
                TestHelpers.qVip, 42, "FiboSync", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("FiboSync", "TestUser").addParameter("p1", "34").addParameter("p2", "55").submit();

        // Start one engine
        addAndStartEngine();

        TestHelpers.waitFor(4, 30000, em);

        Assert.assertEquals(4, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));

        List<History> res = em.createQuery("SELECT j FROM History j ORDER BY j.id", History.class).getResultList();
        History h1, h2 = null;
        for (History h : res)
        {
            h1 = h2;
            h2 = h;
            if (h1 == null)
            {
                continue;
            }
            Assert.assertEquals(h2.getParentJobId(), h1.getId());
            Assert.assertTrue(h2.getEndDate().compareTo(h1.getEndDate()) <= 0);
            Assert.assertTrue(h2.getEndDate().compareTo(h1.getExecutionDate()) > 0);
        }
    }
}
