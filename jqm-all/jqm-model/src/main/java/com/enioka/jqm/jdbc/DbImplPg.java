package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DbImplPg implements DbAdapter
{
    private final static String[] IDS = new String[] { "id" };

    private Map<String, String> queries = new HashMap<String, String>();
    private String tablePrefix = null;

    @Override
    public void prepare(Properties p, Connection cnx)
    {
        this.tablePrefix = p.getProperty("com.enioka.jqm.jdbc.tablePrefix", "");
        queries.putAll(DbImplBase.queries);
        for (Map.Entry<String, String> entry : DbImplBase.queries.entrySet())
        {
            queries.put(entry.getKey(), this.adaptSql(entry.getValue()));
        }
    }

    @Override
    public String getSqlText(String key)
    {
        return queries.get(key);
    }

    @Override
    public String adaptSql(String sql)
    {
        return sql.replace("MEMORY TABLE", "TABLE").replace("JQM_PK.nextval", "nextval('JQM_PK')").replace(" DOUBLE", " DOUBLE PRECISION")
                .replace("UNIX_MILLIS()", "extract('epoch' from current_timestamp)*1000").replace("IN(UNNEST(?))", "=ANY(?)")
                .replace("CURRENT_TIMESTAMP - 1 MINUTE", "NOW() - INTERVAL '1 MINUTES'")
                .replace("CURRENT_TIMESTAMP - ? SECOND", "(NOW() - (? || ' SECONDS')::interval)").replace("FROM (VALUES(0))", "")
                .replace("__T__", this.tablePrefix);
    }

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("postgresql");
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

    @Override
    public void setNullParameter(int position, PreparedStatement s) throws SQLException
    {
        s.setNull(position, s.getParameterMetaData().getParameterType(position));
    }

    @Override
    public String paginateQuery(String sql, int start, int stopBefore, List<Object> prms)
    {
        int pageSize = stopBefore - start;
        sql = String.format("%s LIMIT ? OFFSET ?", sql);
        prms.add(pageSize);
        prms.add(start);
        return sql;
    }
}
