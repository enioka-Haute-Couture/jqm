package com.enioka.jqm.jdbc;

public class DbImplOracle implements DbAdapter
{
    private final static String[] IDS = new String[] { "ID" };

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("oracle");
    }

    @Override
    public String adaptSql(String sql)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] keyRetrievalColumn()
    {
        return IDS;
    }
}
