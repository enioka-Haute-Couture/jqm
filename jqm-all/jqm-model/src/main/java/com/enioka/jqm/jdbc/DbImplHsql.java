package com.enioka.jqm.jdbc;

public class DbImplHsql implements DbAdapter
{
    private final static String[] IDS = new String[] { "ID" };

    @Override
    public String adaptSql(String sql)
    {
        return sql.replace("JQM_PK.nextval", "NEXT VALUE FOR JQM_PK").replace("FOR UPDATE LIMIT", "LIMIT");
    }

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("hsql");
    }

    @Override
    public String[] keyRetrievalColumn()
    {
        return IDS;
    }
}
