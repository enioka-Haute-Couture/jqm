package com.enioka.jqm.xml;

public class JqmXmlException extends RuntimeException
{
    private static final long serialVersionUID = 4053707196102340601L;

    public JqmXmlException(String s)
    {
        super(s);
    }

    public JqmXmlException(Exception e)
    {
        super(e);
    }

    public JqmXmlException(String s, Exception e)
    {
        super(s, e);
    }
}
