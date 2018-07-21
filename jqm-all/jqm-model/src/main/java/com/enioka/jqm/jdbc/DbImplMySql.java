package com.enioka.jqm.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DbImplMySql implements DbAdapter
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

        // Full rewrite for the most critical query.
        queries.put("ji_update_poll",
                this.adaptSql("UPDATE __T__JOB_INSTANCE j1 FORCE INDEX (`PRIMARY`) RIGHT JOIN "
                        + "(SELECT j3.ID FROM __T__JOB_INSTANCE j3 FORCE INDEX(`IDX_JOB_INSTANCE_1`) WHERE j3.STATUS = 'SUBMITTED' AND j3.QUEUE = ? AND "
                        + "(j3.HIGHLANDER = FALSE OR (j3.HIGHLANDER = TRUE AND (SELECT COUNT(1) FROM __T__JOB_INSTANCE j4 FORCE INDEX(`IDX_JOB_INSTANCE_2`) "
                        + "WHERE j4.STATUS IN ('ATTRIBUTED' , 'RUNNING') AND j4.JOBDEF = j3.JOBDEF) = 0)) ORDER BY PRIORITY DESC, INTERNAL_POSITION LIMIT ? FOR UPDATE) j2 "
                        + "ON j2.ID = j1.ID SET j1.NODE = ?, j1.STATUS = 'ATTRIBUTED', j1.DATE_ATTRIBUTION = CURRENT_TIMESTAMP"));
    }

    @Override
    public String getSqlText(String key)
    {
        return queries.get(key);
    }

    @Override
    public String adaptSql(String sql)
    {
        if (sql.contains("CREATE SEQUENCE"))
        {
            return "";
        }
        return sql.replace("MEMORY TABLE", "TABLE").replace("JQM_PK.nextval", "?").replace(" DOUBLE", " DOUBLE PRECISION")
                .replace("UNIX_MILLIS()", "ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000)").replace("IN(UNNEST(?))", "IN(?)")
                .replace("CURRENT_TIMESTAMP - 1 MINUTE", "(UNIX_TIMESTAMP() - 60)")
                .replace("CURRENT_TIMESTAMP - ? SECOND", "(NOW() - INTERVAL ? SECOND)").replace("FROM (VALUES(0))", "FROM DUAL")
                .replace("DNS||':'||PORT", "CONCAT(DNS, ':', PORT)").replace(" TIMESTAMP ", " TIMESTAMP(3) ")
                .replace("CURRENT_TIMESTAMP", "FFFFFFFFFFFFFFFFF@@@@").replace("FFFFFFFFFFFFFFFFF@@@@", "CURRENT_TIMESTAMP(3)")
                .replace("TIMESTAMP(3) NOT NULL", "TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)").replace("__T__", this.tablePrefix);
    }

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("mysql");
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
        res.add("/sql/mysql.sql");
        return res;
    }

    @Override
    public void beforeUpdate(Connection cnx, QueryPreparation q)
    {
        List<Object> params = q.parameters;

        // The main query is very different for MySQL, so we must change the parameter order.
        if (q.isKey("ji_update_poll"))
        {
            Object param0 = params.get(0);
            Object param1 = params.get(1);
            Object param2 = params.get(2);
            params.set(0, param1);
            params.set(1, param2);
            params.set(2, param0);
        }

        // There is no way to do parameterized IN(?) queries with MySQL so we must rewrite these queries as IN(?, ?, ?...)
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

        // Manually generate a new ID for INSERT orders. (with one exception - history inserts do not need a generated ID)
        if (!q.sqlText.startsWith("INSERT INTO") || q.queryKey.startsWith("history_insert"))
        {
            return;
        }
        CallableStatement s = null;
        try
        {
            s = cnx.prepareCall("{? = CALL NEXTVAL(?)}");

            s.registerOutParameter(1, Types.INTEGER);
            s.setString(2, "MAIN");
            s.execute();
            int res = s.getInt(1);
            params.add(0, res);
            s.close();
            q.preGeneratedKey = res;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(q.sqlText, e);
        }
        finally
        {
            DbHelper.closeQuietly(s);
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
