package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.State;

public class DbImplOracle extends DbAdapter
{

    @Override
    public boolean compatibleWith(String product)
    {
        return product.contains("oracle");
    }

    @Override
    public void prepare(Properties p, Connection cnx)
    {
        super.prepare(p, cnx);

        // In some driver versions, trace is enabled by default!
        System.setProperty("oracle.jdbc.Trace", "false");

        // See poll method for everything which is wrong with Oracle and queues.
        queries.put("ji_poll", adaptSql("SELECT /*+ FIRST_ROWS */ ID from (" + "SELECT j2.ID" + "    FROM __T__JOB_INSTANCE j2"
                + "    WHERE j2.STATUS='SUBMITTED' AND j2.QUEUE=? AND (j2.HIGHLANDER=0 OR (j2.HIGHLANDER=1 AND (SELECT COUNT(1)"
                + "        FROM __T__JOB_INSTANCE j3" + "        WHERE j3.STATUS IN('ATTRIBUTED', 'RUNNING') AND j3.JOBDEF=j2.JOBDEF)=0))"
                + "    ORDER BY INTERNAL_POSITION) a " + "WHERE ROWNUM <= ?"));
        queries.put("ji_poll_lock", adaptSql(DbImplBase.queries.get("ji_select_all") + " WHERE ji.ID IN(?) "
                + "AND STATUS='SUBMITTED' AND (ji.HIGHLANDER=0 OR (ji.HIGHLANDER=1 AND (SELECT COUNT(1) FROM __T__JOB_INSTANCE j2 WHERE j2.STATUS IN('ATTRIBUTED', 'RUNNING') AND ji.JOBDEF=j2.JOBDEF)=0)) "
                + "FOR UPDATE OF STATUS, NODE, DATE_ATTRIBUTION"));
        queries.put("ji_poll_attribute", adaptSql(
                "UPDATE __T__JOB_INSTANCE j1 SET j1.NODE=?, j1.STATUS='ATTRIBUTED', j1.DATE_ATTRIBUTION=CURRENT_TIMESTAMP WHERE j1.ID IN(?) "));
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
    public void beforeUpdate(Connection cnx, QueryPreparation q)
    {
        // There is no way to do parameterized IN(?) queries with Oracle so we must rewrite these queries as IN(?, ?, ?...)
        // This cannot be done at startup, as the ? count may be different for each call.
        // We could also use Oracle-specific arrays... but we prefer to avoid importing the driver here. Also it's not very user friendly,
        // so keep it simple.
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
        sql = String.format("SELECT * FROM ( SELECT /*+ FIRST_ROWS */ a.*, ROWNUM rnum FROM (%s) a WHERE ROWNUM < ?) WHERE RNUM >= ?", sql);
        prms.add(stopBefore);
        prms.add(start + 1);
        return sql;
    }

    @Override
    public List<JobInstance> poll(DbConn cnx, Node node, Queue queue, int nbSlots, int level)
    {
        // Oracle is very problematic in regards to creating queues with tables, probably since it has AQ (Advanced Queuing, a
        // fully-fledged message broker) to create actual queues.
        // We may want to evaluate that framework one day. For now, we actually cannot use a message broker since we use many filters like
        // the Highlander filter. So it will have to wait for a new H handling mode.
        // There is no - to our knowledge - way of polling a table in a single UPDATE on this db.

        // We do it very stupidly: first a non-locking query, then a locking query checking status again. After that update.
        // Cannot do both SELECT at the same time, as Oracle does not really support FOR UPDATE in sub queries with ORDER BY.

        // This implementation will often return less results than allowed by nbSlots.

        // Sorted list.
        List<Integer> ids = cnx.runSelectColumn("ji_poll", Integer.class, queue.getId(), nbSlots);
        if (ids.isEmpty())
        {
            return null;
        }

        // Now lock (this repeats the WHERE clause, just not the ORDER BY/LIMIT).
        List<JobInstance> res = JobInstance.select(cnx, "ji_poll_lock", ids);
        if (res.isEmpty())
        {
            cnx.rollback();
            return null;
        }

        // Now update and actually attribute the JI to the local node
        ids.clear();
        for (JobInstance jobInstance : res)
        {
            ids.add(jobInstance.getId());

            // We have taken the JI from the DB before the update, so update the object in memory.
            jobInstance.setNode(node);
            jobInstance.setState(State.ATTRIBUTED);
        }
        cnx.runUpdate("ji_poll_attribute", node.getId(), ids);
        cnx.commit();

        return res;
    }
}
