package com.enioka.jqm.webserver.api;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 *
 * The API implemented by the web server. This is the entry point for the JQM service to start the web server.<br>
 * Implementations must be able to load a full JAX-RS application and provide JAX-RS implementation, including serializers.
 */
public interface WebServer
{
    void start(WebServerConfiguration configuration);

    void stop();

    int getActualPort();
}
