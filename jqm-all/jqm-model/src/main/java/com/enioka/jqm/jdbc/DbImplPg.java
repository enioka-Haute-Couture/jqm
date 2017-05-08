package com.enioka.jqm.jdbc;

public class DbImplPg implements DbAdapter
{
    private final static String[] IDS = new String[] { "id" };

    @Override
    public String adaptSql(String sql)
    {
        return sql.replace("MEMORY TABLE", "TABLE").replace("JQM_PK.nextval", "nextval('JQM_PK')").replace(" DOUBLE", " DOUBLE PRECISION")
                .replace("UNIX_MILLIS()", "extract('epoch' from current_timestamp)*1000").replace("IN(UNNEST(?))", "=ANY(?)")
                .replace("CURRENT_TIMESTAMP - 1 MINUTE", "NOW() - INTERVAL '1 MINUTES'").replace("FROM (VALUES(0))", "");
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
}
