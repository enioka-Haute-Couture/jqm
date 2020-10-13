package com.enioka.jqm.jdbc;

public class NoResultException extends DatabaseException
{
    private static final long serialVersionUID = -6912578872501954384L;

    public NoResultException(String e)
    {
        super(e);
    }

}
