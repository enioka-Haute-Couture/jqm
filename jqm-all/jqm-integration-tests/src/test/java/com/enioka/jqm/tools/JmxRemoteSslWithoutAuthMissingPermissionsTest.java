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

import java.security.AccessControlException;

import org.junit.Test;

public class JmxRemoteSslWithoutAuthMissingPermissionsTest extends JqmBaseTest
{
    /**
     * Test registration of a remote JMX using SSL without clients authentication
     * for connections and test connection to this remote JMX with a client having
     * valid stuff to connect (the client trusts the server) and having one role not
     * giving all the JMX permissions required to execute the
     * {@link JmxTest#jmxRemoteSslTest(JqmBaseTest, boolean, boolean, boolean, boolean, Runnable, boolean, boolean, boolean, String...)}
     * test successfully.
     * 
     * @throws AccessControlException
     */
    @Test(expected = AccessControlException.class)
    public void jmxRemoteSslWithoutAuthMissingPermissionsTest() throws Exception
    {
        Helpers.createRoleIfMissing(cnx, "role1", "Some JMX permissions");
        JmxTest.jmxRemoteSslTest(this, true, false, true, false, true, true, true, true, "role1");
    }

}
