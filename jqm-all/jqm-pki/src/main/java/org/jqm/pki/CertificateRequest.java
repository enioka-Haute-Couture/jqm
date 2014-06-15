package org.jqm.pki;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.PKCS12PfxPduBuilder;
import org.bouncycastle.pkcs.PKCS12SafeBagBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
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
    byte[] pfxFile;
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

    public void generateClientCert(String prettyName, X509CertificateHolder authority, PrivateKey issuerPrivateKey)
    {
        this.prettyName = prettyName;

        authorityCertificate = authority;
        authorityKey = issuerPrivateKey;

        Subject = "CN=JQMUser,OU=ServerProducts,O=Oxymores,C=FR";

        size = 2048;

        EKU = new KeyPurposeId[1];
        EKU[0] = KeyPurposeId.id_kp_clientAuth;

        keyUsage = KeyUsage.dataEncipherment;

        generateAll();
    }

    public void generateServerCert(String prettyName, X509CertificateHolder authority, PrivateKey authorityPrivateKey, String subject)
    {
        this.prettyName = prettyName;

        authorityCertificate = authority;
        authorityKey = authorityPrivateKey;

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

    public void writePfxToFile(String path, String password)
    {
        try
        {
            generatedPfx(password);
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(pfxFile);
            fos.close();
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

    private void generatePem() throws IOException
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

    private void generatedPfx(String pfxPassword) throws Exception
    {
        PKCS12PfxPduBuilder b = new PKCS12PfxPduBuilder();

        PKCS12SafeBagBuilder sbb_public = new PKCS12SafeBagBuilder(holder);
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        // CA
        PKCS12SafeBagBuilder ca_builder = null;
        if (authorityCertificate != null)
        {
            X509Certificate ca = new JcaX509CertificateConverter().setProvider(Constants.JCA_PROVIDER).getCertificate(authorityCertificate);
            ca_builder = new JcaPKCS12SafeBagBuilder(ca);
            ca_builder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("CA"));
        }

        // Public key has a bag by itself
        sbb_public.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(prettyName));
        sbb_public.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(publicKey));

        // Private key
        PKCS12SafeBagBuilder sbb_private = new JcaPKCS12SafeBagBuilder(privateKey, new BcPKCS12PBEOutputEncryptorBuilder(
                PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, new CBCBlockCipher(new DESedeEngine())).build(pfxPassword
                .toCharArray()));
        sbb_private.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(prettyName));
        sbb_private.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(publicKey));

        // Add all bags to PFX
        b.addData(sbb_public.build());
        b.addData(sbb_private.build());
        if (ca_builder != null)
        {
            b.addData(ca_builder.build());
        }

        PKCS12PfxPdu pfx = b.build(new BcPKCS12MacCalculatorBuilder(), pfxPassword.toCharArray());
        pfxFile = pfx.getEncoded();
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

        // ExtensionsGenerator eg = new ExtensionsGenerator();
        gen.addExtension(Extension.subjectKeyIdentifier, false, publicKeyInfo);

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
        return pemPublicFile;
    }

    public OutputStream getPemPrivateFile()
    {
        return pemPrivateFile;
    }

    public byte[] getPfxFile()
    {
        return pfxFile;
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
