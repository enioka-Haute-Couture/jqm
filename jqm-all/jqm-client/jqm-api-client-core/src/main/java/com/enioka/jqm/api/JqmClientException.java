package com.enioka.jqm.api;

public class JqmClientException extends RuntimeException
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
}
