package com.enioka.jqm.old.jdbc;

public class QueryResult
{
    public int nbUpdated = 0;
    public Integer generatedKey = null;

    public Integer getGeneratedId()
    {
        return generatedKey;
    }

}
