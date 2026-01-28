package com.enioka.admin;

/**
 * Exception thrown when an internal error occurs while using the JQM Admin API.
 */
public class JqmAdminApiInternalException extends JqmAdminApiException
{
    private static final long serialVersionUID = 419250515324483751L;

    /**
     * Create a new exception with a specific message.
     * @param e the error message
     */
    public JqmAdminApiInternalException(String e)
    {
        super(e);
    }

    /**
     * Create a new exception wrapping an existing one.
     * @param e the root cause
     */
    public JqmAdminApiInternalException(Exception e)
    {
        super(e);
    }

    /**
     * Create a new exception with a custom message and a root cause.
     * @param m the error message
     * @param e the root cause
     */
    public JqmAdminApiInternalException(String m, Exception e)
    {
        super(m, e);
    }
}
