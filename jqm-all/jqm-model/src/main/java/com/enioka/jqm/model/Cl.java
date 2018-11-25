package com.enioka.jqm.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.QueryResult;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for storing the definition of the class loaders used by the {@link JobDef}.
 */
public class Cl implements Serializable
{
    private static final long serialVersionUID = -965329631668297388L;

    private Integer id;

    private String name;

    private boolean childFirst = false;

    private String hiddenClasses;

    private boolean tracingEnabled = false;

    private boolean persistent = false;

    private String allowedRunners;

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * See {@link #setId(Integer)}
     */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
     * The key used to identify the class loader in the deployment descriptor. Unique.
     */
    public String getName()
    {
        return name;
    }

    /**
     * See {@link #getName()}
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Offer option to have child first class loading. Default should stay parent first, as it is the norm in JSE.
     */
    public boolean isChildFirst()
    {
        return childFirst;
    }

    /**
     * See {@link #isChildFirst()}
     */
    public void setChildFirst(boolean childFirst)
    {
        this.childFirst = childFirst;
    }

    /**
     * Offer possibility to hide Java classes from jobs. One or more regex defining classes never to load from the parent class loader.
     */
    public String getHiddenClasses()
    {
        return hiddenClasses;
    }

    /**
     * See {@link #getHiddenClasses()}
     */
    public void setHiddenClasses(String hiddenClasses)
    {
        this.hiddenClasses = hiddenClasses;
    }

    /**
     * Activate listing all class loaded inside the job log
     */
    public boolean isTracingEnabled()
    {
        return tracingEnabled;
    }

    /**
     * See {@link #isTrace()}
     * 
     * @param trace
     */
    public void setTracingEnabled(boolean trace)
    {
        this.tracingEnabled = trace;
    }

    /**
     * The different event handlers associated to this class loader. Ordered list.
     */
    public List<ClHandler> getHandlers()
    {
        return handlerCache;
    }

    private List<ClHandler> getHandlers(DbConn cnx)
    {
        return ClHandler.select(cnx, "cleh_select_all_for_cl", this.id);
    }

    /**
     * Default is false. When false, the class loader is transient: it is created to run a new job instance and is thrown out when the job
     * instance ends. When true, it is not thrown out at the end and will be reused by all job instances created from the different
     * {@link JobDef}s using this {@link Cl} (therefore, multiple {@link JobDef}s can share the same static context).
     */
    public boolean isPersistent()
    {
        return persistent;
    }

    /**
     * See {@link #isPersistent()}
     */
    public void setPersistent(boolean persistent)
    {
        this.persistent = persistent;
    }

    /**
     * The different runners that are active in this context. If null, the global parameter job_runners is used instead.
     */
    public String getAllowedRunners()
    {
        return allowedRunners;
    }

    /**
     * See {@link #getAllowedRunners()}
     */
    public void setAllowedRunners(String allowedRunners)
    {
        this.allowedRunners = allowedRunners;
    }

    /**
     * ResultSet is not modified (no rs.next called).
     * 
     * @param rs
     * @return
     */
    static Cl map(ResultSet rs, int colShift)
    {
        Cl tmp = new Cl();

        try
        {
            tmp.id = rs.getInt(1 + colShift);
            tmp.name = rs.getString(2 + colShift);
            tmp.childFirst = rs.getBoolean(3 + colShift);
            tmp.hiddenClasses = rs.getString(4 + colShift);
            tmp.tracingEnabled = rs.getBoolean(5 + colShift);
            tmp.persistent = rs.getBoolean(6 + colShift);
            tmp.allowedRunners = rs.getString(7 + colShift);
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return tmp;
    }

    private List<ClHandler> handlerCache = new ArrayList<ClHandler>();

    public static List<Cl> select(DbConn cnx, String query_key, Object... args)
    {
        List<Cl> res = new ArrayList<Cl>();
        try
        {
            ResultSet rs = cnx.runSelect(query_key, args);
            while (rs.next())
            {
                Cl tmp = map(rs, 0);
                res.add(tmp);

                tmp.handlerCache = tmp.getHandlers(cnx);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static Cl select_key(DbConn cnx, String name)
    {
        List<Cl> res = select(cnx, "cl_select_by_key", name);
        if (res.isEmpty())
        {
            throw new NoResultException("no result for query by key for key " + name);
        }
        if (res.size() > 1)
        {
            throw new DatabaseException("Inconsistent database! Multiple results for query by key for key " + name);
        }
        return res.get(0);
    }

    public static int create(DbConn cnx, String name, boolean childFirst, String hiddenClasses, boolean tracing, boolean persistent,
            String allowedRunners)
    {
        QueryResult r = cnx.runUpdate("cl_insert", name, childFirst, hiddenClasses, tracing, persistent, allowedRunners);
        int newId = r.getGeneratedId();

        return newId;
    }

    public void update(DbConn cnx)
    {
        if (id == null)
        {
            this.id = Cl.create(cnx, name, childFirst, hiddenClasses, tracingEnabled, persistent, allowedRunners);
        }
        else
        {
            cnx.runUpdate("cl_update_all_fields_by_id", name, childFirst, hiddenClasses, tracingEnabled, persistent, allowedRunners, id);
        }
    }
}
