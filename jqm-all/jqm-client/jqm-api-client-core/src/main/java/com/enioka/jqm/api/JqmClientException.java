package com.enioka.jqm.api;

/**
 * Denotes an internal error that happened inside the JQM API. It is not due to bad user input, but to configuration issues or bugs.
 */
public class JqmClientException extends JqmException
{
    private static final long serialVersionUID = 338795021501465434L;

    public JqmClientException(String message)
    {
        super(message);
    }

    public JqmClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public JqmClientException(Throwable cause)
    {
        super("an internal JQM client exception occured", cause);
    }
}
