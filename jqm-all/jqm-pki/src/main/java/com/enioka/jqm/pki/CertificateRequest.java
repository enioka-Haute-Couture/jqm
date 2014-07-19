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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.BigIntegers;

public class CertificateRequest
{
    // Parameter fields
    private String prettyName;
    private Integer size = 2048;
    private X509CertificateHolder authorityCertificate = null;
    private PrivateKey authorityKey = null;
    private String Subject;
    private KeyPurposeId[] EKU;
    private int keyUsage = 0;
    private int validityYear = 10;

    // Result fields
    OutputStream pemPublicFile;
    OutputStream pemPrivateFile;
    X509CertificateHolder holder;
    PublicKey publicKey;
    PrivateKey privateKey;

    // Public API
    public void generateCA(String prettyName)
    {
        this.prettyName = prettyName;

        Subject = "CN=JQM-CA,OU=ServerProducts,O=Oxymores,C=FR";
        size = 4096;

        EKU = new KeyPurposeId[4];
        EKU[0] = KeyPurposeId.id_kp_codeSigning;
        EKU[1] = KeyPurposeId.id_kp_serverAuth;
        EKU[2] = KeyPurposeId.id_kp_clientAuth;
        EKU[3] = KeyPurposeId.id_kp_emailProtection;

        keyUsage = KeyUsage.cRLSign | KeyUsage.keyCertSign;

        generateAll();
    }

    public void generateClientCert(String prettyName, X509CertificateHolder authority, PrivateKey issuerPrivateKey, String subject)
    {
        this.prettyName = prettyName;

        authorityCertificate = authority;
        authorityKey = issuerPrivateKey;

        this.Subject = subject;

        size = 2048;

        EKU = new KeyPurposeId[1];
        EKU[0] = KeyPurposeId.id_kp_clientAuth;

        keyUsage = KeyUsage.digitalSignature | KeyUsage.keyEncipherment;

        generateAll();
    }

    public void generateServerCert(String prettyName, X509CertificateHolder authority, PrivateKey issuerPrivateKey, String subject)
    {
        this.prettyName = prettyName;

        authorityCertificate = authority;
        authorityKey = issuerPrivateKey;

        this.Subject = subject;

        size = 2048;

        EKU = new KeyPurposeId[1];
        EKU[0] = KeyPurposeId.id_kp_serverAuth;

        keyUsage = KeyUsage.digitalSignature | KeyUsage.keyEncipherment;

        generateAll();
    }

    public void writePemPublicToFile(String path)
    {
        try
        {
            File f = new File(path);
            if (!f.getParentFile().isDirectory() && !f.getParentFile().mkdir())
            {
                throw new PkiException("couldn't create directory " + f.getParentFile().getAbsolutePath() + " for storing the SSL keystore");
            }
            FileWriter fw = new FileWriter(path);
            PEMWriter wr = new PEMWriter(fw);
            wr.writeObject(holder);
            wr.close();
            fw.close();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
    }

    public String writePemPublicToString()
    {
        try
        {
            StringWriter sw = new StringWriter();
            PEMWriter wr = new PEMWriter(sw);
            wr.writeObject(holder);
            wr.close();
            sw.close();
            return sw.toString();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
    }

    public void writePemPrivateToFile(String path)
    {
        try
        {
            FileWriter fw = new FileWriter(path);
            PEMWriter wr = new PEMWriter(fw);
            wr.writeObject(privateKey);
            wr.close();
            fw.close();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
    }

    public String writePemPrivateToString()
    {
        try
        {
            StringWriter sw = new StringWriter();
            PEMWriter wr = new PEMWriter(sw);
            wr.writeObject(privateKey);
            wr.close();
            sw.close();
            return sw.toString();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
    }

    // Internal methods
    private void generateAll()
    {
        try
        {
            generateKeyPair();
            generateX509();
            generatePem();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
    }

    private void generatePem()
    {
        try
        {
            // PEM public key
            pemPublicFile = new ByteArrayOutputStream();
            Writer osw = new OutputStreamWriter(pemPublicFile);
            PEMWriter wr = new PEMWriter(osw);
            wr.writeObject(holder);
            wr.close();

            // PEM private key
            pemPrivateFile = new ByteArrayOutputStream();
            osw = new OutputStreamWriter(pemPrivateFile);
            wr = new PEMWriter(osw);
            wr.writeObject(privateKey);
            wr.close();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
    }

    void writePfxToFile(OutputStream out, String password)
    {
        try
        {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);

            Certificate[] chain = null;
            if (authorityCertificate != null)
            {
                chain = new Certificate[2];
                chain[0] = new JcaX509CertificateConverter().getCertificate(this.holder);
                chain[1] = new JcaX509CertificateConverter().getCertificate(this.authorityCertificate);
            }
            else
            {
                chain = new Certificate[1];
                chain[0] = new JcaX509CertificateConverter().setProvider("BC").getCertificate(this.holder);
            }

            ks.setKeyEntry("private key for " + this.prettyName, privateKey, password.toCharArray(), chain);

            ks.store(out, password.toCharArray());
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
    }

    public void writeTrustPfxToFile(String path, String password)
    {
        try
        {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(null, null);

            Certificate ca = new JcaX509CertificateConverter().getCertificate(this.authorityCertificate);

            ks.setCertificateEntry("JQM-CA", ca);

            FileOutputStream fos = new FileOutputStream(path);
            ks.store(fos, password.toCharArray());
            fos.close();
        }
        catch (Exception e)
        {
            throw new PkiException(e);
        }
    }

    private void generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException
    {
        Security.addProvider(new BouncyCastleProvider());

        SecureRandom random = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(Constants.KEY_ALGORITHM, Constants.JCA_PROVIDER);
        keyPairGenerator.initialize(size, random);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    private void generateX509() throws Exception
    {
        SecureRandom random = new SecureRandom();
        X500Name dnName = new X500Name(Subject);
        Calendar endValidity = Calendar.getInstance();
        endValidity.add(Calendar.YEAR, validityYear);

        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());

        X509v3CertificateBuilder gen = new X509v3CertificateBuilder(authorityCertificate == null ? dnName
                : authorityCertificate.getSubject(), BigIntegers.createRandomInRange(BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE),
                random), new Date(), endValidity.getTime(), dnName, publicKeyInfo);

        // Public key ID
        DigestCalculator digCalc = new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
        X509ExtensionUtils x509ExtensionUtils = new X509ExtensionUtils(digCalc);
        gen.addExtension(Extension.subjectKeyIdentifier, false, x509ExtensionUtils.createSubjectKeyIdentifier(publicKeyInfo));

        // EKU
        gen.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(EKU));

        // Basic constraints (is CA?)
        if (authorityCertificate == null)
        {
            gen.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        }

        // Key usage
        gen.addExtension(Extension.keyUsage, true, new KeyUsage(keyUsage));

        // Subject Alt names ?

        // Authority
        if (authorityCertificate != null)
        {
            gen.addExtension(Extension.authorityKeyIdentifier, false,
                    new AuthorityKeyIdentifier(authorityCertificate.getSubjectPublicKeyInfo()));
        }

        // Signer
        ContentSigner signer = new JcaContentSignerBuilder("SHA512WithRSAEncryption").setProvider(Constants.JCA_PROVIDER).build(
                authorityKey == null ? privateKey : authorityKey);

        // Go
        holder = gen.build(signer);
    }

    // Data that can be accessed
    public OutputStream getPemPublicFile()
    {
        if (pemPublicFile == null)
        {
            generatePem();
        }
        return pemPublicFile;
    }

    public OutputStream getPemPrivateFile()
    {
        if (pemPrivateFile == null)
        {
            generatePem();
        }
        return pemPrivateFile;
    }

    public X509CertificateHolder getHolder()
    {
        return holder;
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }
}
