package org.jqm.pki;

/**
 * Hello world!
 * 
 */
public class App
{
    public static void main(String[] args)
    {
        System.out.println("Hello World!");

        CertificateRequest re = new CertificateRequest();
        re.generateCA("JQM-CA");

        // Console debug
        System.out.println(re.pemPublicFile);
        System.out.println(re.pemPrivateFile);
        System.out.println(re.pfxFile);

        // File debug
        re.writePemPrivateToFile("C:/temp/ca.key");
        re.writePemPublicToFile("C:/temp/ca.cer");
        re.writePfxToFile("c:/temp/ca.pfx", Constants.PFX_PASSWORD);

        // Client
        CertificateRequest re2 = new CertificateRequest();
        re2.generateClientCert("JQM-CLIENT", re.holder, re.privateKey);
        re2.writePemPublicToFile("C:/temp/client.cer");
        re2.writePfxToFile("c:/temp/client.pfx", Constants.PFX_PASSWORD);

        // Server
        CertificateRequest re3 = new CertificateRequest();
        re3.generateServerCert("JQM-SERVER", re.holder, re.privateKey, "CN=localhost");
        re3.writePfxToFile("c:/temp/server.pfx", Constants.PFX_PASSWORD);
        re3.writePemPublicToFile("c:/temp/server.cer");
    }

}
