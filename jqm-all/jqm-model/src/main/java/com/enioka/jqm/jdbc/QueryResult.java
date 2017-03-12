package com.enioka.jqm.jdbc;

import java.sql.ResultSet;

public class QueryResult
{
    public int nbUpdated = 0;
    public ResultSet generatedKeys = null;

    public int getGeneratedId()
    {
        try
        {
            this.generatedKeys.next();
            return this.generatedKeys.getInt(1);
        }
        catch (Exception e)
        {
            throw new DatabaseException(e);
        }
    }

}
