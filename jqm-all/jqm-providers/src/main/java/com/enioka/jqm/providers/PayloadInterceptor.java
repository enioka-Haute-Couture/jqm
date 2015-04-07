package com.enioka.jqm.providers;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.PooledConnection;

/**
 * Intercepter for Tomcat JDBC pool connections used by JQM payloads.<br>
 * It tracks all connections opened by a specific payload Thread so as to be able to forcefully close them at the end of its run.<br>
 * <br>
 * Limitation: if the payload has created sub-threads, they will not be cleaned up.
 */
public class PayloadInterceptor extends JdbcInterceptor
{
    private class ConnPair
    {
        public Thread thread;
        public PooledConnection conn;
    }

    private static Map<ConnectionPool, Set<ConnPair>> conns = new ConcurrentHashMap<ConnectionPool, Set<ConnPair>>();

    private PooledConnection trackedPooledCon;
    private ConnectionPool trackedConnPool;

    @Override
    public void reset(ConnectionPool parent, PooledConnection con)
    {
        trackedConnPool = parent;
        trackedPooledCon = con;

        if (parent != null && con != null)
        {
            ConnPair cp = new ConnPair();
            cp.conn = con;
            cp.thread = Thread.currentThread();
            Set<ConnPair> pairs = conns.get(parent);
            if (pairs != null)
            {
                conns.get(parent).add(cp);
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (this.trackedConnPool != null && this.trackedPooledCon != null && CLOSE_VAL.equals(method.getName()))
        {
            for (ConnPair cp : conns.get(trackedConnPool))
            {
                if (cp.conn.equals(trackedPooledCon))
                {
                    conns.get(trackedConnPool).remove(cp);
                }
            }
        }
        return super.invoke(proxy, method, args);
    }

    @Override
    public void disconnected(ConnectionPool parent, PooledConnection con, boolean finalizing)
    {
        for (ConnPair cp : conns.get(parent))
        {
            if (cp.conn.equals(con))
            {
                conns.get(parent).remove(cp);
            }
        }
        super.disconnected(parent, con, finalizing);
    }

    @Override
    public void poolStarted(ConnectionPool pool)
    {
        conns.put(pool, Collections.newSetFromMap(new ConcurrentHashMap<ConnPair, Boolean>()));
        super.poolStarted(pool);
    }

    @Override
    public void poolClosed(ConnectionPool pool)
    {
        conns.remove(pool);
        super.poolClosed(pool);
    }

    /**
     * Called by the engine to trigger the cleanup at the end of a payload thread.
     */
    public static int forceCleanup(Thread t)
    {
        int i = 0;
        for (Map.Entry<ConnectionPool, Set<ConnPair>> e : conns.entrySet())
        {
            for (ConnPair c : e.getValue())
            {
                if (c.thread.equals(t))
                {
                    try
                    {
                        // This will in turn remove it from the static Map.
                        c.conn.getHandler().invoke(c.conn, Connection.class.getMethod("close"), null);
                    }
                    catch (Throwable e1)
                    {
                        e1.printStackTrace();
                    }
                    i++;
                }
            }
        }
        return i;
    }
}
