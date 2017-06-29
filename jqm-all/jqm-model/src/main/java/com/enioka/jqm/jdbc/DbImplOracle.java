package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DbImplOracle implements DbAdapter
{
    private final static String[] IDS = new String[] { "ID" };

    private Map<String, String> queries = new HashMap<String, String>();
    private String tablePrefix = null;

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("oracle");
    }

    @Override
    public void prepare(Properties p, Connection cnx)
    {
        this.tablePrefix = p.getProperty("com.enioka.jqm.jdbc.tablePrefix", "");
        queries.putAll(DbImplBase.queries);
        for (Map.Entry<String, String> entry : DbImplBase.queries.entrySet())
        {
            queries.put(entry.getKey(), this.adaptSql(entry.getValue()));
        }

        queries.put("ji_update_poll",
                "UPDATE tmpjqm.JOB_INSTANCE j1 SET NODE=?, STATUS='ATTRIBUTED', DATE_ATTRIBUTION=CURRENT_TIMESTAMP WHERE rowid IN (SELECT rid FROM (SELECT rowid as rid FROM tmpjqm.JOB_INSTANCE j2 WHERE j2.STATUS='SUBMITTED' AND j2.QUEUE=? AND (j2.HIGHLANDER=0 OR (j2.HIGHLANDER=1 AND (SELECT COUNT(1) FROM tmpjqm.JOB_INSTANCE j3 WHERE j3.STATUS IN('ATTRIBUTED', 'RUNNING') AND j3.JOBDEF=j2.JOBDEF)=0)) ORDER BY INTERNAL_POSITION) WHERE rownum < ?)");
    }

    @Override
    public String adaptSql(String sql)
    {
        return sql.replace("MEMORY TABLE", "TABLE").replace(" INTEGER", " NUMBER(10, 0)").replace(" DOUBLE", " DOUBLE PRECISION")
                .replace("UNIX_MILLIS()", "JQM_PK.currval").replace("IN(UNNEST(?))", "IN(?)")
                .replace("CURRENT_TIMESTAMP - 1 MINUTE", "(SYSDATE - 1/1440)")
                .replace("CURRENT_TIMESTAMP - ? SECOND", "(SYSDATE - ?/86400)").replace("FROM (VALUES(0))", "FROM DUAL")
                .replace("BOOLEAN", "NUMBER(1)").replace("true", "1").replace("false", "0").replace("__T__", this.tablePrefix);
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
        sql = String.format("SELECT * FROM ( SELECT /*+ FIRST_ROWS(?) */ a.*, ROWNUM rnum FROM (%s) a WHERE ROWNUM < ?) WHERE RNUM >= ?",
                sql);
        prms.add(0, pageSize);
        prms.add(stopBefore);
        prms.add(start + 1);
        return sql;
    }
}
