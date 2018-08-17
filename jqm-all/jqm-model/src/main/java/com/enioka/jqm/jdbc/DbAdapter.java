package com.enioka.jqm.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;

/**
 * The interface to implement to create a new database adapter. Adapters contain all the database-specific stuff for running JQM on a
 * specific database brand.<br>
 * <br>
 * All implementations must have a no-argument constructor.
 *
 */
public abstract class DbAdapter
{
    private static Logger jqmlogger = LoggerFactory.getLogger(DbAdapter.class);

    protected String[] IDS = new String[] { "ID" };

    /**
     * Query cache.
     */
    protected Map<String, String> queries = new HashMap<String, String>();

    /**
     * Prefix to use for all tables and views.
     */
    protected String tablePrefix = null;

    /**
     * Tests if the adapter is compatible with a given database. When in doubt, answer no.
     * 
     * @param product
     *            the product code (from Connection.getMetaData().getDatabaseProductName()) in lower case.
     * @return true if compatible, false otherwise.
     */
    public abstract boolean compatibleWith(String product);

    /**
     * Adapt the given query template to enable it to run on the target database. This is only called on startup, and the results are
     * cached.
     * 
     * @param sql
     *            the SQL query (full text, no terminator).
     * @return a ready to use query.
     */
    public abstract String adaptSql(String sql);

    /**
     * The name of the columns to retrieve for getGeneratedKeys calls. (some dbs want uppercase, other lowercase).
     * 
     * @return the list of id columns.
     */
    public String[] keyRetrievalColumn()
    {
        return IDS;
    }

    /**
     * A list of files to run (from the classpath) before running schema upgrades. Default is empty.
     */
    public List<String> preSchemaCreationScripts()
    {
        return new ArrayList<String>();
    }

    /**
     * Hook run before creating a new update Statement.
     * 
     * @param cnx
     *            an open and ready to use connection to the database. Please return it without any open statement/result set.
     * @param q
     *            the query which will be run. Can be modified by this method.
     * @return a generated ID or null;
     */
    public void beforeUpdate(Connection cnx, QueryPreparation q)
    {}

    /**
     * Called after creating the first connection. The adapter should create its caches and do all initialization it requires. Most
     * importantly, the SQL query cache should be created.
     * 
     * @param cnx
     *            an open ready to use connection to the database.
     */
    public void prepare(Properties p, Connection cnx)
    {
        this.tablePrefix = p.getProperty("com.enioka.jqm.jdbc.tablePrefix", "");
        queries.putAll(DbImplBase.queries);
        for (Map.Entry<String, String> entry : DbImplBase.queries.entrySet())
        {
            queries.put(entry.getKey(), this.adaptSql(entry.getValue()));
        }
    }

    /**
     * Returns a ready to run SQL text for the given key. (adaptSql does not need to be run on the returned string)
     * 
     * @param key
     *            key of the SQL order
     * @return full text to run on the database. Null if key not found.
     */
    public String getSqlText(String key)
    {
        return queries.get(key);
    }

    /**
     * Databases all have different issues when settings null parameters.
     * 
     * @param position
     *            in the statement (sql parameters).
     * @param s
     *            statement being built.
     */
    public abstract void setNullParameter(int position, PreparedStatement s) throws SQLException;

    /**
     * Adds pagination elements to a query.
     * 
     * @param sql
     *            the full SQL text
     * @param start
     *            the first included row (start of page). First row is zero, not one.
     * @param stopBefore
     *            the first excluded row (end of page)
     * @param prms
     *            the bind variables values. Can be modified.
     * @return the ready to use SQL query for this database.
     */
    public abstract String paginateQuery(String sql, int start, int stopBefore, List<Object> prms);

    /**
     * The polling method. Creating "queues" on the different databases can be extremely different in the different supported RDBMS, so this
     * method allows to fully change the polling method. <br>
     * <br>
     * Default implementation uses the ji_select_to_run SQL template query and should work for most databases. It has the advantage of
     * polling with a single UPDATE, solving a lot of of concurrency issues. But it is not possible in all RDBMS to UPDATE ORDER BY LIMIT.
     * 
     * @param cnx
     *            a session without active TX.
     * @param nodeId
     *            the node being polled
     * @param queueId
     *            the queue being polled
     * @param nbSlots
     *            how many slots are available - i.e. max JI which can be taken from the queue.
     * @param level
     *            - recursion helper. Always use 0.
     * @return a list of JI, or null if none.
     */
    public List<JobInstance> poll(DbConn cnx, Node node, Queue queue, int nbSlots, int level)
    {
        if (nbSlots <= 0)
        {
            return null;
        }

        QueryResult qr = cnx.runUpdate("ji_update_poll", node.getId(), queue.getId(), nbSlots);
        if (qr.nbUpdated > 0)
        {
            jqmlogger.debug("Poller has found {} JI to run", qr.nbUpdated);
            List<JobInstance> res = JobInstance.select(cnx, "ji_select_to_run", node.getId(), queue.getId());
            if (res.size() == qr.nbUpdated)
            {
                cnx.commit();
                return res;
            }
            else if (level <= 3)
            {
                // Try again. This means the jobs marked for exec on previous loop have not already started.
                // So they were still in the SELECT WHERE STATE='ATTRIBUTED'.
                // Happens when loop interval is too low (ms and not s), so only happens during tests.
                jqmlogger.trace("Polling interval seems too low");
                cnx.rollback();
                Thread.yield();
                return poll(cnx, node, queue, nbSlots, level + 1);
            }
            else
            {
                // Simply give up. The engine is under such a heavy load that we should first deal with the backlog.
                return null;
            }
        }
        else
        {
            return null;
        }
    }
}
