package com.enioka.jqm.webui.shiro;

import java.security.cert.X509Certificate;

import org.apache.shiro.authc.AuthenticationToken;

public class CertificateToken implements AuthenticationToken
{
    private static final long serialVersionUID = -3513329844423322673L;

    protected X509Certificate clientCert;

    public CertificateToken(X509Certificate c)
    {
        this.clientCert = c;
        System.out.println(getUserName());
    }

    @Override
    public Object getPrincipal()
    {
        return clientCert.getSubjectDN().getName();
    }

    public String getUserName()
    {
        return clientCert.getSubjectDN().getName().replaceFirst("CN=", "");
    }

    @Override
    public Object getCredentials()
    {
        // No need for credentials - the very existence of a validated certificate is enough
        return null;
    }

    public X509Certificate getClientCert()
    {
        return clientCert;
    }
}
