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
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.test.helpers.TestHelpers;

public class GeoTest extends JqmBaseTest
{
    @Before
    public void b()
    {
        TestHelpers.setNodesLogLevel("INFO", em);
        em.getTransaction().begin();
        TestHelpers.node.setRootLogLevel("INFO");
        TestHelpers.nodeMix.setRootLogLevel("INFO");
        TestHelpers.nodeMix2.setRootLogLevel("INFO");
        em.getTransaction().commit();
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
        JqmSimpleTest.create(em, "pyl.StressGeo").addEngine("localhost2").addEngine("localhost3").addRuntimeParameter("nbJob", "1")
                .addWaitMargin(60000).expectOk(511).run(this);
    }
}
