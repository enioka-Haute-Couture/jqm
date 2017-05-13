package com.enioka.admin;

public class JqmAdminApiException extends RuntimeException
{
    private static final long serialVersionUID = 65922602936206453L;

    public JqmAdminApiException(String e)
    {
        super(e);
    }

    public JqmAdminApiException(Exception e)
    {
        super(e);
    }

    public JqmAdminApiException(String m, Exception e)
    {
        super(m, e);
    }
}
