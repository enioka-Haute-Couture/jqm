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
package com.enioka.jqm.pki;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemReader;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.PKI;

/**
 * This class is the link between the X509 methods in CertificateRequest and the database store.
 * 
 */
public class JdbcCa
{
    public static CertificateRequest initCa(DbConn cnx)
    {
        // result field
        CertificateRequest cr = new CertificateRequest();

        // Get the alias of the private key to use
        String caAlias = null;
        caAlias = GlobalParameter.getParameter(cnx, "keyAlias", Constants.CA_DEFAULT_PRETTY_NAME);

        // Create the CA if it does not already exist
        PKI pki = null;
        try
        {
            pki = PKI.select_key(cnx, caAlias);
        }
        catch (NoResultException e)
        {
            // Create the CA certificate and PK
            cr = new CertificateRequest();
            cr.generateCA(caAlias);

            // Store
            PKI.create(cnx, caAlias, cr.writePemPrivateToString(), cr.writePemPublicToString());
            cnx.commit();
            pki = PKI.select_key(cnx, caAlias);
        }

        try
        {
            // Public (X509 certificate)
            String pemCert = pki.getPemCert();
            StringReader sr = new StringReader(pemCert);
            PemReader pr = new PemReader(sr);
            cr.holder = new X509CertificateHolder(pr.readPemObject().getContent());
            pr.close();

            // Private key
            String pemPrivate = pki.getPemPK();
            sr = new StringReader(pemPrivate);
            PEMParser pp = new PEMParser(sr);
            PEMKeyPair caKeyPair = (PEMKeyPair) pp.readObject();
            pp.close();
            byte[] encodedPrivateKey = caKeyPair.getPrivateKeyInfo().getEncoded();
            KeyFactory keyFactory = KeyFactory.getInstance(Constants.KEY_ALGORITHM);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            cr.privateKey = keyFactory.generatePrivate(privateKeySpec);
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }

        // Done
        return cr;
    }

    public static void prepareWebServerStores(DbConn cnx, String subject, String serverPfxPath, String trustPfxPath, String pfxPassword,
            String serverCertPrettyName, String serverCerPath, String caCerPath)
    {
        File pfx = new File(serverPfxPath);

        if (pfx.canRead())
        {
            return;
        }

        CertificateRequest ca = initCa(cnx);
        ca.writePemPublicToFile(caCerPath);

        CertificateRequest srv = new CertificateRequest();
        srv.generateServerCert(serverCertPrettyName, ca.holder, ca.privateKey, subject);
        try
        {
            FileOutputStream pfxStore = new FileOutputStream(serverPfxPath);
            srv.writePfxToFile(pfxStore, pfxPassword);
            pfxStore.close();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
        srv.writePemPublicToFile(serverCerPath);
        srv.writeTrustPfxToFile(trustPfxPath, pfxPassword);
    }

    public static void prepareClientStore(DbConn cnx, String subject, String pfxPath, String pfxPassword, String prettyName, String cerPath)
    {
        File pfx = new File(pfxPath);

        if (pfx.canRead())
        {
            return;
        }

        CertificateRequest ca = initCa(cnx);

        CertificateRequest srv = new CertificateRequest();
        srv.generateClientCert(prettyName, ca.holder, ca.privateKey, subject);
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
        srv.writePemPublicToFile(cerPath);
    }

    public static InputStream getClientData(DbConn cnx, String userName) throws Exception
    {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(sink);

        CertificateRequest ca = JdbcCa.initCa(cnx);
        CertificateRequest cl = new CertificateRequest();
        cl.generateClientCert("JQM authentication certificate", ca.holder, ca.privateKey, "CN=" + userName);

        zos.putNextEntry(new ZipEntry("ca.cer"));
        zos.write(((ByteArrayOutputStream) ca.getPemPublicFile()).toByteArray());
        zos.closeEntry();

        zos.putNextEntry(new ZipEntry("client.cer"));
        zos.write(((ByteArrayOutputStream) cl.getPemPublicFile()).toByteArray());
        zos.closeEntry();

        zos.putNextEntry(new ZipEntry("client.pfx"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        cl.writePfxToFile(os, "SuperPassword");
        zos.write(os.toByteArray());
        zos.closeEntry();

        zos.close();
        return new ByteArrayInputStream(sink.toByteArray());
    }

}
