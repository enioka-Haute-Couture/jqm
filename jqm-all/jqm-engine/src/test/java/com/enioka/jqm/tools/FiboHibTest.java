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

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class FiboHibTest extends JqmBaseTest
{
    @Test
    public void testFiboHib() throws Exception
    {
        CreationTools.createJobDef(null, true, "com.enioka.jqm.tests.App", null, "jqm-tests/jqm-test-fibohib/target/test.jar",
                TestHelpers.qVip, 42, "FiboHib", null, "Franquin", "ModuleMachin", "other1", "other2", false, em);
        JobRequest.create("FiboHib", "TestUser").addParameter("p1", "1").addParameter("p2", "2").submit();

        addAndStartEngine();
        TestHelpers.waitFor(11, 30000, em);

        Assert.assertEquals(11, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }
}
