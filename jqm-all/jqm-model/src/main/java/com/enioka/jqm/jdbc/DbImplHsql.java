package com.enioka.jqm.jdbc;

import java.util.Map;

public class DbImplHsql
{
    public static Map<String, String> getQueries()
    {
        // HSQLDB is very simple
        return DbImplBase.queries;
    }

}
