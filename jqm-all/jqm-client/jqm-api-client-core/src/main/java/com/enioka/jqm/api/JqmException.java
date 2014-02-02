package com.enioka.jqm.api;

/**
 * Base class for all JQM API exceptions
 */
public class JqmException extends RuntimeException
{
    private static final long serialVersionUID = -2937310125732117976L;

    JqmException(String message)
    {
        super(message);
    }

    JqmException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
