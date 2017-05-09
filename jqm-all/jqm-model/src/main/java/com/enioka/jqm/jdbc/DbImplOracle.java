package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbImplOracle implements DbAdapter
{
    private final static String[] IDS = new String[] { "ID" };

    private Map<String, String> queries = new HashMap<String, String>();

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("oracle");
    }

    @Override
    public void prepare(Connection cnx)
    {
        queries.putAll(DbImplBase.queries);
        for (Map.Entry<String, String> entry : DbImplBase.queries.entrySet())
        {
            queries.put(entry.getKey(), this.adaptSql(entry.getValue()));
        }
    }

    @Override
    public String adaptSql(String sql)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSqlText(String key)
    {
        return queries.get(key);
    }

    @Override
    public String[] keyRetrievalColumn()
    {
        return IDS;
    }

    @Override
    public List<String> preSchemaCreationScripts()
    {
        return new ArrayList<String>();
    }

    @Override
    public void beforeUpdate(Connection cnx, QueryPreparation q)
    {
        return;
    }
}
