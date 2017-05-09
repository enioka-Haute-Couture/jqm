package com.enioka.jqm.jdbc;

public class QueryResult
{
    public int nbUpdated = 0;
    public Integer generatedKey = null;

    public Integer getGeneratedId()
    {
        return generatedKey;
    }

}
