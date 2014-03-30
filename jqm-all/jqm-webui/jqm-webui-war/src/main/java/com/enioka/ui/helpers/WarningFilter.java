package com.enioka.ui.helpers;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

/**
 * Filter to get rid of annoying "PWC4011: Unable to set request character encoding to UTF-8 from context" warnings.
 */
@WebFilter("/faces/*")
public class WarningFilter implements Filter
{
    public WarningFilter()
    {}

    public void destroy()
    {}

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        request.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    public void init(FilterConfig fConfig) throws ServletException
    {}
}
