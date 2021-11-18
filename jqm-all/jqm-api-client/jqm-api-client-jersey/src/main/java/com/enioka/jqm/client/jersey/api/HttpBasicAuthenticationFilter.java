package com.enioka.jqm.client.jersey.api;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

/**
 * Basic preemptive HTTP authentication filter for JAX-RS. For use with {@link HttpBasicAuthenticationFeature} only. Most JAX-RS clients
 * actually have a similar feature, but we want to stay purely standard-compliant.
 */
class HttpBasicAuthenticationFilter implements ClientRequestFilter
{
    private String username;
    private byte[] password;

    HttpBasicAuthenticationFilter(String username, String password)
    {
        this.username = username;
        if (username == null)
        {
            username = "";
        }

        this.password = password == null ? new byte[0] : password.getBytes();
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException
    {
        final byte[] userNameColumn = (username + ":").getBytes(Charset.forName("iso-8859-1"));
        final byte[] usernameColumnPassword = new byte[userNameColumn.length + password.length];

        System.arraycopy(userNameColumn, 0, usernameColumnPassword, 0, userNameColumn.length);
        System.arraycopy(password, 0, usernameColumnPassword, userNameColumn.length, password.length);

        String header = "Basic " + Base64.getEncoder().encodeToString(usernameColumnPassword);

        requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, header);
    }
}
