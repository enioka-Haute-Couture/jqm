package com.enioka.jqm.webui.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.util.WebUtils;

public class BasicHttpAuthenticationFilter extends org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
{
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
