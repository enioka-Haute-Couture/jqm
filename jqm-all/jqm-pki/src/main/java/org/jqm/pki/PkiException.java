package org.jqm.pki;

public class PkiException extends RuntimeException
{
    private static final long serialVersionUID = -838182133891625251L;

    public PkiException()
    {

    }

    public PkiException(Exception e)
    {
        super(e);
    }
}
