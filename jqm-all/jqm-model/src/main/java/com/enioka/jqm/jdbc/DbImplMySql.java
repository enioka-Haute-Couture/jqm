package com.enioka.jqm.jdbc;

import java.io.Console;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Queue;

public class DbImplMySql extends DbAdapter
{
    private String sequenceSql, sequenceSqlRetrieval;

    @Override
    public void prepare(Properties p, Connection cnx)
    {
        super.prepare(p, cnx);

        // We do NOT want to use paginateQuery on each poll query as we want polling to be as painless as possible, so we pre-paginate it.
        queries.put("ji_select_poll", queries.get("ji_select_poll") + " LIMIT ?");

        sequenceSqlRetrieval = adaptSql("SELECT next FROM __T__JQM_SEQUENCE WHERE name = ?");
        sequenceSql = adaptSql("UPDATE __T__JQM_SEQUENCE SET next = next + 1 WHERE name = ?");
    }

    @Override
    public String adaptSql(String sql)
    {
        if (sql.contains("CREATE SEQUENCE"))
        {
            return "";
        }
        return sql.replace("MEMORY TABLE", "TABLE").replace("JQM_PK.nextval", "?").replace(" DOUBLE", " DOUBLE PRECISION")
                .replace("UNIX_MILLIS()", "ROUND(UNIX_TIMESTAMP(NOW(4)) * 1000)").replace("IN(UNNEST(?))", "IN(?)")
                .replace("CURRENT_TIMESTAMP - 1 MINUTE", "(UNIX_TIMESTAMP() - 60)")
                .replace("CURRENT_TIMESTAMP - ? SECOND", "(UTC_TIMESTAMP() - INTERVAL ? SECOND)").replace("FROM (VALUES(0))", "FROM DUAL")
                .replace("DNS||':'||PORT", "CONCAT(DNS, ':', PORT)").replace(" TIMESTAMP ", " DATETIME(3) ")
                .replace("CURRENT_TIMESTAMP", "FFFFFFFFFFFFFFFFF@@@@").replace("FFFFFFFFFFFFFFFFF@@@@", "UTC_TIMESTAMP(3)")
                .replace("DATETIME(3) NOT NULL", "DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)").replace("__T__", this.tablePrefix);
    }

    @Override
    public boolean compatibleWith(DatabaseMetaData product) throws SQLException
    {
        return (product.getDatabaseProductName().contains("MySQL")
                && ((product.getDatabaseMajorVersion() == 5 && product.getDatabaseMinorVersion() >= 6)
                        || product.getDatabaseMajorVersion() > 5))
                || (product.getDatabaseProductName().contains("MariaDB") && product.getDatabaseMajorVersion() >= 10);
    }

    @Override
    public List<String> preSchemaCreationScripts(DbConn cnx)
    {
        // Sequence management
        List<String> res = new ArrayList<String>();
        res.add("/sql/mysql.sql");

        // Deal with a change in version 2.3.0 where we changed timestamps into datetimes for better TZ support.
        // This cannot be handled in normal migration scripts as the meaning of scripts version 1 & 2 has changed with the changes in
        // adaptSql().
        ResultSet rs = cnx.runRawSelect("select UPPER(DATA_TYPE) FROM __T__INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME=? AND COLUMN_NAME=?",
                "VERSION", "INSTALL_DATE");
        try
        {
            if (rs.next() && rs.getString(1).equals("TIMESTAMP"))
            {
                // Should be DATETIME not TIMESTAMP
                res.add("/sql/mysql_00002_00003_raw.sql");
            }
            // If no result we do not care as it means DB schema is empty, hence the rs.next() condition.
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Could not update MySQL existing schema", e);
        }
        finally
        {
            DbHelper.closeQuietly(rs);
        }

        return res;
    }

    @Override
    public void beforeUpdate(Connection cnx, QueryPreparation q)
    {
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
        PreparedStatement s = null;
        PreparedStatement s2 = null;
        try
        {
            s = cnx.prepareStatement(sequenceSql);
            s.setString(1, "MAIN");
            s.executeUpdate();

            s2 = cnx.prepareStatement(sequenceSqlRetrieval);
            s2.setString(1, "MAIN");
            ResultSet rs = s2.executeQuery();
            if (!rs.next())
            {
                throw new NoResultException("The query returned zero rows when one was expected.");
            }
            q.preGeneratedKey = rs.getInt(1);
            q.parameters.add(0, q.preGeneratedKey);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(q.sqlText + " - " + sequenceSql + " - while fetching new ID from table sequence", e);
        }
        finally
        {
            DbHelper.closeQuietly(s);
            DbHelper.closeQuietly(s2);
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

    @Override
    public List<JobInstance> poll(DbConn cnx, Queue queue, int headSize)
    {
        return JobInstance.select(cnx, "ji_select_poll", queue.getId(), headSize);
    }
}
