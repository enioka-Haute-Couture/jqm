package com.enioka.jqm.jdbc;

public class QueryResult
{
    public int nbUpdated = 0;
    public Long generatedKey = null;

    public Long getGeneratedId()
    {
        return generatedKey;
    }

}
