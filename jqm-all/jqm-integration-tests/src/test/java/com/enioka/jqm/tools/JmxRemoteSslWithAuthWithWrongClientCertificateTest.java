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

import java.io.File;

import org.junit.Test;

import com.enioka.jqm.pki.JdbcCa;

public class JmxRemoteSslWithAuthWithWrongClientCertificateTest extends JqmBaseTest
{

    /**
     * Test registration of a remote JMX using SSL and authentication of clients for
     * connections and test connection to this remote JMX with a client not having
     * valid stuff to connect (the client trusts the server and the server trusts
     * him but the client certificate used has a Common Name different from the
     * username provided in credentials).
     */
    @Test(expected = SecurityException.class)
    public void jmxRemoteSslWithAuthWithWrongClientCertificateTest() throws Exception
    {
        new File("./conf").mkdir();
        JdbcCa.prepareClientStore(cnx, "CN=wrongUsername", "./conf/client.pfx", "SuperPassword", "client-cert", "./conf/client.cer");
        JmxTest.jmxRemoteSslTest(this, true, true, true, true, false, true, true, true);
    }

}
