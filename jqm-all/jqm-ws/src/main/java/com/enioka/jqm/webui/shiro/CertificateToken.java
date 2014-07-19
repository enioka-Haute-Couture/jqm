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
