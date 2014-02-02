package com.enioka.jqm.api;

/**
 * Denotes an input error from the user of the API. The message gives the detail of his error.
 */
public class JqmInvalidRequestException extends JqmException
{
    private static final long serialVersionUID = 2248971878792826983L;

    public JqmInvalidRequestException(String msg, Exception e)
    {
        super(msg, e);
    }

    public JqmInvalidRequestException(String msg)
    {
        super(msg);
    }
}
