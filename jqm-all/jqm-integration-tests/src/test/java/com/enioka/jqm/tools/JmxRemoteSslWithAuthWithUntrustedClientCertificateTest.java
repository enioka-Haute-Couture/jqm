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
import java.io.FileOutputStream;
import java.io.StringReader;
import java.rmi.ConnectIOException;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.Test;

import com.enioka.jqm.pki.CertificateRequest;
import com.enioka.jqm.pki.JdbcCa;
import com.enioka.jqm.pki.PkiException;

public class JmxRemoteSslWithAuthWithUntrustedClientCertificateTest extends JqmBaseTest
{

    /**
     * Test registration of a remote JMX using SSL with authentication of users for
     * connections and test connection to this remote JMX with a client not having
     * valid stuff to connect (the client uses an untrusted certificate to
     * authenticate).
     * 
     * @throws ConnectIOException
     */
    @Test(expected = ConnectIOException.class)
    public void jmxRemoteSslWithAuthWithUntrustedClientCertificateTest() throws Exception
    {
        // System.setProperty("javax.net.debug", "all");
        JmxTest.jmxRemoteSslTest(this, true, true, true, true, new Runnable()
        {

            @Override
            public void run()
            {
                prepareClientStoreWithUntrustedCA("CN=testuser", "./conf/client.pfx", "SuperPassword", "client-cert");
            }

        });
    }

    /**
     * Code taken from {@link JdbcCa} class of JQM internal PKI.
     */
    static CertificateRequest generateUntrustedCA()
    {
        String caAlias = "JQM-UNTRUSTED-CA";

        CertificateRequest cr = new CertificateRequest();

        // Create the CA certificate and PK
        cr.generateCA(caAlias);

        try
        {
            // Public (X509 certificate)
            String pemCert = cr.writePemPublicToString();
            StringReader sr = new StringReader(pemCert);
            PemReader pr = new PemReader(sr);
            cr.setHolder(new X509CertificateHolder(pr.readPemObject().getContent()));
            pr.close();

            // Private key
            String pemPrivate = cr.writePemPrivateToString();
            sr = new StringReader(pemPrivate);
            PEMParser pp = new PEMParser(sr);
            PEMKeyPair caKeyPair = (PEMKeyPair) pp.readObject();
            pp.close();
            byte[] encodedPrivateKey = caKeyPair.getPrivateKeyInfo().getEncoded();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            cr.setPrivateKey(keyFactory.generatePrivate(privateKeySpec));
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }

        return cr;
    }

    /**
     * Code taken from {@link JdbcCa} class of JQM internal PKI.
     */
    static void prepareClientStoreWithUntrustedCA(String subject, String pfxPath, String pfxPassword, String prettyName)
    {
        File pfx = new File(pfxPath);

        if (pfx.canRead())
        {
            return;
        }

        CertificateRequest ca = generateUntrustedCA();

        CertificateRequest srv = new CertificateRequest();
        srv.generateClientCert(prettyName, ca.getHolder(), ca.getPrivateKey(), subject);
        try
        {
            FileOutputStream pfxStore = new FileOutputStream(pfxPath);
            srv.writePfxToFile(pfxStore, pfxPassword);
            pfxStore.close();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }

        // srv.writePemPublicToFile(cerPath); Not useful for this test.
    }

}
