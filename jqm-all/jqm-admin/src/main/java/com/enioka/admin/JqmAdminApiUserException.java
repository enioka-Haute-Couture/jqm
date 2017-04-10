package com.enioka.admin;

public class JqmAdminApiUserException extends JqmAdminApiException
{
    private static final long serialVersionUID = 8470196707989067977L;

    public JqmAdminApiUserException(String e)
    {
        super(e);
    }

    public JqmAdminApiUserException(Exception e)
    {
        super(e);
    }

    public JqmAdminApiUserException(String m, Exception e)
    {
        super(m, e);
    }
}
