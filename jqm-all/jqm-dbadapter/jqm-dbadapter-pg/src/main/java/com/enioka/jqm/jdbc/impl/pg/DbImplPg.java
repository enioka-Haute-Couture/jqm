package com.enioka.jqm.jdbc.impl.pg;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.kohsuke.MetaInfServices;

import com.enioka.jqm.jdbc.DbAdapter;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Queue;

@MetaInfServices(DbAdapter.class)
public class DbImplPg extends DbAdapter
{

    @Override
    public void prepare(Properties p, Connection cnx)
    {
        super.prepare(p, cnx);

        // We do NOT want to use paginateQuery on each poll query as we want polling to be as painless as possible, so we pre-paginate it.
        queries.put("ji_select_poll", queries.get("ji_select_poll") + " LIMIT ?");
    }

    public DbImplPg()
    {
        this.ids[0] = "id";
    }

    @Override
    public String adaptSql(String sql)
    {
        return sql.replace("JQM_PK.nextval", "nextval('JQM_PK')").replace(" DOUBLE", " DOUBLE PRECISION")
                .replace(" REAL", " DOUBLE PRECISION").replace("UNIX_MILLIS()", "extract('epoch' from current_timestamp)*1000")
                .replace("IN(UNNEST(?))", "=ANY(?)").replace("CURRENT_TIMESTAMP - 1 MINUTE", "NOW() - INTERVAL '1 MINUTES'")
                .replace("CURRENT_TIMESTAMP - ? SECOND", "(NOW() - (? || ' SECONDS')::interval)").replace("FROM (VALUES(0))", "")
                .replace("CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP AT TIME ZONE 'UTC'").replace("__T__", this.tablePrefix);
    }

    @Override
    public boolean compatibleWith(DatabaseMetaData product) throws SQLException
    {
        return product.getDatabaseProductName().toLowerCase().contains("postgresql");
    }

    @Override
    public void setNullParameter(int position, PreparedStatement s) throws SQLException
    {
        s.setNull(position, s.getParameterMetaData().getParameterType(position));
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
    public void simulateDisconnection(Connection cnx)
    {

        try (Statement s = cnx.createStatement())
        {
            s.execute("SELECT pg_cancel_backend(pid) FROM pg_stat_activity WHERE pid <> pg_backend_pid()"); // Tue les autres
            s.execute("SELECT pg_terminate_backend(pg_backend_pid())");
        }
        catch (SQLException e)
        {
            // it is expected that this catch an exception.
        }
    }
}
