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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class GeoTest extends JqmBaseTest
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
    public void testGeo() throws Exception
    {
        em.getTransaction().begin();
        TestHelpers.node.setRootLogLevel("INFO");
        TestHelpers.nodeMix.setRootLogLevel("INFO");
        TestHelpers.nodeMix2.setRootLogLevel("INFO");
        em.getTransaction().commit();

        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-geo/target/test.jar", TestHelpers.qVip, 42, "Geo", null,
                "Franquin", "ModuleMachin", "other1", "other2", false, em);
        JobRequest.create("Geo", "TestUser").addParameter("nbJob", "1").submit();

        addAndStartEngine();
        addAndStartEngine("localhost2");
        addAndStartEngine("localhost3");
        TestHelpers.waitFor(511, 60000, em);

        Assert.assertEquals(511, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }
}
