package com.enioka.jqm.tools;

class JqmRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 8187758834667680389L;

    JqmRuntimeException(String msg)
    {
        super(msg);
    }

    JqmRuntimeException(String msg, Exception e)
    {
        super(msg, e);
    }
}
