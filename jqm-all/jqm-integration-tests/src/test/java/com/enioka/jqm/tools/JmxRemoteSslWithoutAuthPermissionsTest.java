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

import org.junit.Assert;
import org.junit.Test;

public class JmxRemoteSslWithoutAuthPermissionsTest extends JqmBaseTest
{

    /**
     * Test registration of a remote JMX using SSL without clients authentication
     * for connections and test connection to this remote JMX with a client having
     * valid stuff to connect (the client trusts the server) and roles giving all
     * the JMX permissions required to execute the
     * {@link JmxTest#jmxRemoteSslTest(JqmBaseTest, boolean, boolean, boolean, boolean, Runnable, boolean, boolean, boolean, String...)}
     * test successfully.
     */
    @Test
    public void jmxRemoteSslWithoutAuthPermissionsTest() throws Exception
    {
        String[] role1Perms = new String[] { "jmx:javax.management.MBeanPermission \"com.enioka.jqm.*#*[com.enioka.jqm:*]\", \"getDomains\"",
                "jmx:java.net.SocketPermission \"localhost:1111\", \"resolve\"" };
        String[] role2Perms = new String[] { "jmx:javax.management.MBeanPermission \"com.enioka.jqm.*#*[com.enioka.jqm:*]\", \"queryMBeans\"",
                "jmx:java.net.SocketPermission \"localhost:2222\", \"listen\"", "jmx:java.lang.reflect.ReflectPermission \"suppressAccessChecks\"" };
        String[] role3Perms = new String[] { "jmx:java.net.SocketPermission \"localhost:3333\", \"listen\"" };
        Helpers.createRoleIfMissing(cnx, "role1", "Some JMX permissions", role1Perms);
        Helpers.createRoleIfMissing(cnx, "role2", "Some JMX permissions", role2Perms);
        Helpers.createRoleIfMissing(cnx, "role3", "Some JMX permissions", role3Perms);

        JmxTest.jmxRemoteSslTest(this, true, false, true, false, true, true, true, true, "role1", "role2", "role3");
        Assert.assertTrue(JmxTest.checkRolePolicyPermissions("role1", role1Perms));
        Assert.assertTrue(JmxTest.checkRolePolicyPermissions("role2", role2Perms));

        // Role 3 is not defined in jmxremote.policy test file, it is completely created
        // from database after updating the policy file:
        Assert.assertTrue(JmxTest.checkRolePolicyPermissions("role3", role3Perms));
    }

}
