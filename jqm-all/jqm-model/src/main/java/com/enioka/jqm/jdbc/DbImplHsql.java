package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransactionRollbackException;
import java.util.List;
import java.util.Properties;

import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Queue;
import org.apache.commons.lang.exception.ExceptionUtils;

public class DbImplHsql extends DbAdapter
{
    @Override
    public void prepare(Properties p, Connection cnx)
    {
        super.prepare(p, cnx);

        // We do NOT want to use paginateQuery on each poll query as we want polling to be as painless as possible, so we pre-paginate it.
        queries.put("ji_select_poll", queries.get("ji_select_poll") + " LIMIT ?");
    }

    @Override
    public String adaptSql(String sql)
    {
        return sql.replace("JQM_PK.nextval", "NEXT VALUE FOR JQM_PK").replace("FOR UPDATE LIMIT", "LIMIT").replace("__T__",
                this.tablePrefix);
    }

    @Override
    public boolean compatibleWith(DatabaseMetaData product) throws SQLException
    {
        return product.getDatabaseProductName().toLowerCase().contains("hsql");
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

    @Override
    public boolean testDbUnreachable(Exception e)
    {
        if (ExceptionUtils.indexOfType(e, SQLNonTransientException.class) != -1
                && ExceptionUtils.getMessage(e).contains("connection exception: closed"))
        {
            return true;
        }
        if (ExceptionUtils.indexOfType(e, SQLTransactionRollbackException.class) != -1)
        {
            // Unexpected rollbacks happen on DBA closing the session in HSQLDB.
            return true;
        }
        return super.testDbUnreachable(e);
    }

    @Override
    public void simulateDisconnection(Connection cnx)
    {
        try
        {
            // Get current session ID to avoid it.
            PreparedStatement s1 = cnx.prepareStatement("CALL SESSION_ID()");
            ResultSet rs1 = s1.executeQuery();
            if (!rs1.next())
            {
                throw new NoResultException("Could not fetch current session ID");
            }
            long currentSessionId = rs1.getLong(1);
            rs1.close();

            PreparedStatement s2 = cnx.prepareStatement("SELECT SESSION_ID FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS");

            ResultSet rs2 = s2.executeQuery();
            while (rs2.next())
            {
                if (currentSessionId == rs2.getLong(1))
                {
                    // Do not kill the session killing the others!
                    continue;
                }

                // Note: cannot use parameters with ALTER SESSION in HSQLDB.
                cnx.prepareStatement("ALTER SESSION " + rs2.getLong(1) + " RELEASE").executeUpdate();
                cnx.prepareStatement("ALTER SESSION " + rs2.getLong(1) + " CLOSE").executeUpdate();
            }

            rs2.close();
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

}
