package com.enioka.jqm.jdbc;

public class DatabaseException extends RuntimeException
{
    public DatabaseException(Exception e)
    {
        super(e);
    }

    public DatabaseException(String e)
    {
        super(e);
    }

    public DatabaseException(String e, Exception ex)
    {
        super(e, ex);
    }
}
