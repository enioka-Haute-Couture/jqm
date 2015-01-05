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

import org.junit.Test;

public class ParameterTest extends JqmBaseTest
{
    // Sanity check test
    @Test
    public void testParameterValue() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.JobBaseGetParam").addDefParameter("arg1", "Gaston Lagaffe").addDefParameter("arg2", "Franquin")
                .expectNonOk(1).expectOk(0).run(this);
    }

    @Test
    public void testMixParameters() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.JobBaseGetParam").addDefParameter("arg1", "argument1").addDefParameter("arg2", "Franquin")
                .addRuntimeParameter("arg2", "argument2").run(this);
    }

    @Test
    public void testDefaultParameters() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.JobBaseGetParam").addDefParameter("arg1", "argument1").addDefParameter("arg2", "argument2").run(this);
    }

    @Test
    public void testOverrideAllParmeters() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.JobBaseGetParam").addDefParameter("arg1", "Gaston Lagaffe").addDefParameter("arg2", "Franquin")
                .addRuntimeParameter("arg1", "argument1").addRuntimeParameter("arg2", "argument2").run(this);
    }

}
