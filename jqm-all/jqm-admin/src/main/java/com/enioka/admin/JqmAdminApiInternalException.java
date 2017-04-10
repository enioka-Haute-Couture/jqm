package com.enioka.admin;

public class JqmAdminApiInternalException extends JqmAdminApiException
{
    private static final long serialVersionUID = 419250515324483751L;

    public JqmAdminApiInternalException(String e)
    {
        super(e);
    }

    public JqmAdminApiInternalException(Exception e)
    {
        super(e);
    }

    public JqmAdminApiInternalException(String m, Exception e)
    {
        super(m, e);
    }
}
