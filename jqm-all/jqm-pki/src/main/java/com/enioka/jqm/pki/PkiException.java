package com.enioka.jqm.pki;

public class PkiException extends RuntimeException
{
    private static final long serialVersionUID = -838182133891625251L;

    public PkiException()
    {

    }

    public PkiException(String msg)
    {
        super(msg);
    }

    public PkiException(Exception e)
    {
        super(e);
    }

    public PkiException(String msg, Exception e)
    {
        super(msg, e);
    }
}
