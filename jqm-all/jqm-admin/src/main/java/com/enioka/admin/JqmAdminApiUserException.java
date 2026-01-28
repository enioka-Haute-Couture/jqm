package com.enioka.admin;

/**
 * Exception thrown when a user error occurs while using the JQM Admin API.
 */
public class JqmAdminApiUserException extends JqmAdminApiException
{
    private static final long serialVersionUID = 8470196707989067977L;

    /**
     * Create a new exception with a specific message.
     * @param e the error message
     */
    public JqmAdminApiUserException(String e)
    {
        super(e);
    }

    /**
     * Create a new exception wrapping an existing one.
     * @param e the root cause
     */
    public JqmAdminApiUserException(Exception e)
    {
        super(e);
    }

    /**
     * Create a new exception with a custom message and a root cause.
     * @param m the error message
     * @param e the root cause
     */
    public JqmAdminApiUserException(String m, Exception e)
    {
        super(m, e);
    }
}
