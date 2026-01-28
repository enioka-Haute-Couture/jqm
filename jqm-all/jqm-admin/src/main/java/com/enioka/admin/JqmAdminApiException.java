package com.enioka.admin;

/**
 * Exception thrown when an error occurs while using the JQM Admin API.
 */
public class JqmAdminApiException extends RuntimeException
{
    private static final long serialVersionUID = 65922602936206453L;

    /**
     * Create a new exception with a specific message.
     * @param e the error message
     */
    public JqmAdminApiException(String e)
    {
        super(e);
    }

    /**
     * Create a new exception wrapping an existing one.
     * @param e the root cause
     */
    public JqmAdminApiException(Exception e)
    {
        super(e);
    }

    /**
     * Create a new exception with a custom message and a root cause.
     * @param m the error message
     * @param e the root cause
     */
    public JqmAdminApiException(String m, Exception e)
    {
        super(m, e);
    }
}
