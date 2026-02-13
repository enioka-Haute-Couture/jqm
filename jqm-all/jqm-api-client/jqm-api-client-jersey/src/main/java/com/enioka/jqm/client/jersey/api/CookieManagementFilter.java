package com.enioka.jqm.client.jersey.api;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that handles cookies by storing Set-Cookie headers from responses and sending them back in subsequent requests.
 */
class CookieManagementFilter implements ClientRequestFilter, ClientResponseFilter
{
    private static Logger jqmlogger = LoggerFactory.getLogger(CookieManagementFilter.class);

    // Store cookies per domain/path
    private final Map<String, List<HttpCookie>> cookieStore = new ConcurrentHashMap<>();

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException
    {
        URI uri = requestContext.getUri();
        String domain = uri.getHost();

        List<HttpCookie> cookies = cookieStore.get(domain);
        if (cookies != null && !cookies.isEmpty())
        {
            // Filter out expired cookies
            List<HttpCookie> validCookies = cookies.stream().filter(cookie -> !cookie.hasExpired()).collect(Collectors.toList());

            if (!validCookies.isEmpty())
            {
                String cookieHeader = validCookies.stream().map(cookie -> cookie.getName() + "=" + cookie.getValue())
                        .collect(Collectors.joining("; "));

                requestContext.getHeaders().add(HttpHeaders.COOKIE, cookieHeader);
                jqmlogger.debug("Sending cookies: {}", cookieHeader);
            }

            // Update cookie store to remove expired cookies
            if (validCookies.size() != cookies.size())
            {
                cookieStore.put(domain, validCookies);
            }
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException
    {
        List<String> setCookieHeaders = responseContext.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookieHeaders != null && !setCookieHeaders.isEmpty())
        {
            URI uri = requestContext.getUri();
            String domain = uri.getHost();

            List<HttpCookie> cookies = cookieStore.computeIfAbsent(domain, k -> new ArrayList<>());

            for (String setCookieHeader : setCookieHeaders)
            {
                try
                {
                    List<HttpCookie> parsedCookies = HttpCookie.parse(setCookieHeader);
                    for (HttpCookie cookie : parsedCookies)
                    {
                        // Remove existing cookie with same name
                        cookies.removeIf(c -> c.getName().equals(cookie.getName()));

                        // Only store if not being deleted (Max-Age=0 or Expires in past)
                        if (cookie.getMaxAge() != 0)
                        {
                            cookies.add(cookie);
                            jqmlogger.debug("Stored cookie: {}={} (Max-Age: {}, Domain: {})", cookie.getName(), cookie.getValue(),
                                    cookie.getMaxAge(), domain);
                        }
                        else
                        {
                            jqmlogger.debug("Deleted cookie: {} (Domain: {})", cookie.getName(), domain);
                        }
                    }
                }
                catch (Exception e)
                {
                    jqmlogger.warn("Failed to parse Set-Cookie header: {}", setCookieHeader, e);
                }
            }
        }
    }
}
