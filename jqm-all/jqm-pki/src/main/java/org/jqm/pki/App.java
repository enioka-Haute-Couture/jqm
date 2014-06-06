package org.jqm.pki;


/**
 * Hello world!
 * 
 */
public class App
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Hello World!");

        CertificateRequest re = new CertificateRequest();
        re.generateCA("houbahop", "JQM-CA2");

        // Console debug
        System.out.println(re.pemPublicFile);
        System.out.println(re.pemPrivateFile);
        System.out.println(re.pfxFile);

        // File debug
        re.writePemPrivateToFile("C:/temp/cert.key");
        re.writePemPublicToFile("C:/temp/cert.cer");
        re.writePfxToFile("c:/temp/cert.pfx");

        // Client
        CertificateRequest re2 = new CertificateRequest();
        System.out.println(re.privateKey);
        re2.generateClientCert("houbahop", "JQM-CA2", re.holder, re.privateKey);
        re2.writePemPublicToFile("C:/temp/client.cer");
    }

}
