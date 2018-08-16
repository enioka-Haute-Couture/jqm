package com.enioka.jqm.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DbImplHsql extends DbAdapter
{
    @Override
    public String adaptSql(String sql)
    {
        return sql.replace("JQM_PK.nextval", "NEXT VALUE FOR JQM_PK").replace("FOR UPDATE LIMIT", "LIMIT").replace("__T__",
                this.tablePrefix);
    }

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("hsql");
    }

    @Override
    public void setNullParameter(int position, PreparedStatement s) throws SQLException
    {
        // Absolutely stupid: set to null regardless of type.
        s.setObject(position, null);
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
