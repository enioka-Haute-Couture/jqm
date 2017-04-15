package com.enioka.jqm.jdbc;

import java.util.Map;

public class DbImplHsql
{
    public static Map<String, String> getQueries()
    {
        // HSQLDB is very simple
        for (String key : DbImplBase.queries.keySet())
        {
            DbImplBase.queries.put(key, adaptSql(DbImplBase.queries.get(key)));
        }

        return DbImplBase.queries;
    }

    public static String adaptSql(String sql)
    {
        return sql.replace("JQM_PK.nextval", "NEXT VALUE FOR JQM_PK");
    }
}
