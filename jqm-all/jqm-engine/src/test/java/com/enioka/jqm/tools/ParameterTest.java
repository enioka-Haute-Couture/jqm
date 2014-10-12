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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class ParameterTest extends JqmBaseTest
{
    @Test
    public void testMixParameters() throws Exception
    {
        List<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg1", "argument1", em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("arg2", "Franquin", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);
        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-checkargs/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("MarsuApplication", "TestUser").addParameter("arg2", "argument2").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testDefaultParameters() throws Exception
    {
        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg1", "argument1", em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("arg2", "argument2", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);
        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-checkargs/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testOverrideAllParmeters() throws Exception
    {
        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg1", "Gaston Lagaffe", em);
        JobDefParameter jdp2 = CreationTools.createJobDefParameter("arg2", "Franquin", em);
        jdargs.add(jdp);
        jdargs.add(jdp2);

        CreationTools.createJobDef(null, true, "App", jdargs, "jqm-tests/jqm-test-checkargs/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("MarsuApplication", "TestUser").addParameter("arg1", "argument1").addParameter("arg2", "argument2").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }
}
