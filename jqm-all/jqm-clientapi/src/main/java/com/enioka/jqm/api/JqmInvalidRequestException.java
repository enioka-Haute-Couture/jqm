package com.enioka.jqm.api;

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
