package com.enioka.jqm.client.jersey.api;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

/**
 * Basic preemptive HTTP authentication feature for JAX-RS. Most JAX-RS client actually have a similar feature, but we want to stay purely
 * standard-compliant.
 */
class HttpBasicAuthenticationFeature implements Feature
{
    private String username;
    private String password;

    HttpBasicAuthenticationFeature(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean configure(FeatureContext context)
    {
        context.register(new HttpBasicAuthenticationFilter(username, password));
        return true;
    }
}
