package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DbImplDb2 implements DbAdapter
{
    private final static String[] IDS = new String[] { "ID" };

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

        // Simpler polling query, as DB2 has a very weird locking model.
        queries.put("ji_update_poll", this.adaptSql(
                "UPDATE __T__JOB_INSTANCE j1 SET NODE=?, STATUS='ATTRIBUTED', DATE_ATTRIBUTION=CURRENT_TIMESTAMP WHERE j1.STATUS='SUBMITTED' AND j1.ID IN "
                        + "(SELECT j2.ID FROM __T__JOB_INSTANCE j2 WHERE j2.STATUS='SUBMITTED' AND j2.QUEUE=? "
                        + "AND (j2.HIGHLANDER=false OR (j2.HIGHLANDER=true AND (SELECT COUNT(1) FROM __T__JOB_INSTANCE j3 WHERE j3.STATUS IN('ATTRIBUTED', 'RUNNING') AND j3.JOBDEF=j2.JOBDEF)=0 )) ORDER BY PRIORITY DESC, INTERNAL_POSITION LIMIT ?)"));
    }

    @Override
    public String getSqlText(String key)
    {
        return queries.get(key);
    }

    @Override
    public String adaptSql(String sql)
    {
        return sql.replace("MEMORY TABLE", "TABLE").replace("UNIX_MILLIS()", "JQM_PK.nextval").replace("IN(UNNEST(?))", "IN(?)")
                .replace("FROM (VALUES(0))", "FROM SYSIBM.SYSDUMMY1").replace("BOOLEAN", "SMALLINT").replace("__T__", this.tablePrefix);
    }

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("db2");
    }

    @Override
    public String[] keyRetrievalColumn()
    {
        return IDS;
    }

    @Override
    public List<String> preSchemaCreationScripts()
    {
        List<String> res = new ArrayList<String>();
        return res;
    }

    @Override
    public void beforeUpdate(Connection cnx, QueryPreparation q)
    {
        // There is no (clean) way to do parameterized IN(?) queries with DB2 so we must rewrite these queries as IN(?, ?, ?...)
        // This cannot be done at startup, as the ? count may be different for each call.
        if (q.sqlText.contains("IN(?)"))
        {
            int index = q.sqlText.indexOf("IN(?)");
            int nbIn = 0;
            while (index >= 0)
            {
                index = q.sqlText.indexOf("IN(?)", index + 1);
                nbIn++;
            }

            int nbList = 0;
            ArrayList<Object> newParams = new ArrayList<Object>(q.parameters.size() + 10);
            for (Object o : q.parameters)
            {
                if (o instanceof List<?>)
                {
                    nbList++;
                    List<?> vv = (List<?>) o;
                    if (vv.size() == 0)
                    {
                        throw new DatabaseException("Cannot do a query whith an empty list parameter");
                    }

                    newParams.addAll(vv);

                    String newPrm[] = new String[vv.size()];
                    for (int j = 0; j < vv.size(); j++)
                    {
                        newPrm[j] = "?";
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < vv.size(); j++)
                    {
                        sb.append("?,");
                    }
                    q.sqlText = q.sqlText.replaceFirst("IN\\(\\?\\)", "IN(" + sb.substring(0, sb.length() - 1) + ")");
                }
                else
                {
                    newParams.add(o);
                }
            }
            q.parameters = newParams;

            if (nbList != nbIn)
            {
                throw new DatabaseException("Mismatch: count of list parameters and of IN clauses is different.");
            }
        }
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
