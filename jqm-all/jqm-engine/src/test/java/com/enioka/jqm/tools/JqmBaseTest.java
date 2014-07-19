/**
 * Copyright © 2013 enioka. All rights reserved
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

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.enioka.jqm.api.JqmClientFactory;

public class JqmBaseTest
{
    public static Logger jqmlogger = Logger.getLogger(JqmBaseTest.class);
    public static Server s;

    @BeforeClass
    public static void testInit() throws Exception
    {
        JndiContext.createJndiContext();
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();
    }

    @AfterClass
    public static void stop() throws NamingException
    {
        JqmClientFactory.resetClient(null);
        Helpers.resetEmf();
        ((JndiContext) NamingManager.getInitialContext(null)).resetSingletons();
        s.shutdown();
        s.stop();
    }
}
