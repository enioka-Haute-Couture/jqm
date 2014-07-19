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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.util.WebUtils;

public class BasicHttpAuthenticationFilter extends org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
{

    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response)
    {
        final X509Certificate[] clientCertificateChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if (clientCertificateChain != null && clientCertificateChain.length > 0)
        {
            return true;
        }
        else
        {
            return super.isLoginAttempt(request, response);
        }
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response)
    {
        final X509Certificate[] clientCertificateChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if (clientCertificateChain != null && clientCertificateChain.length > 0)
        {
            return new CertificateToken(clientCertificateChain[0]);
        }
        else
        {
            return super.createToken(request, response);
        }
    }

    @Override
    protected boolean sendChallenge(ServletRequest request, ServletResponse response)
    {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String appHeader = httpRequest.getHeader("X-Requested-With");

        if (!"XMLHttpRequest".equals(appHeader))
        {
            // If not from an interactive application, return the classic 401/Basic authentication challenge
            return super.sendChallenge(request, response);
        }
        else
        {
            // If not, return a stupid challenge to avoid basic auth prompt from the browser
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String authcHeader = "BASIC2 realm=\"" + getApplicationName() + "\"";
            httpResponse.setHeader(AUTHENTICATE_HEADER, authcHeader);
            return false;
        }
    }
}
