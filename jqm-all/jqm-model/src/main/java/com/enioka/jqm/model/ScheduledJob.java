package com.enioka.jqm.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.QueryResult;

/**
 * A simple scheduling of {@link JobDef}.
 *
 */
public class ScheduledJob
{
    private int id;

    private Integer jobDefinition;

    private String cronExpression;

    private Calendar lastUpdated;

    private Integer priority;

    private Integer queue;

    private Map<String, String> parametersCache = new HashMap<String, String>();

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getJobDefinition()
    {
        return jobDefinition;
    }

    public void setJobDefinition(int jobDefinition)
    {
        this.jobDefinition = jobDefinition;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public Calendar getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(Calendar lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    public Integer getQueue()
    {
        return queue;
    }

    public void setQueue(Integer queue)
    {
        this.queue = queue;
    }

    public Map<String, String> getParameters()
    {
        return parametersCache;
    }

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    public static List<ScheduledJob> select(DbConn cnx, String query_key, Object... args)
    {
        List<ScheduledJob> res = new ArrayList<ScheduledJob>();
        List<Integer> currentIdList = null;
        List<List<Integer>> allIdLists = new ArrayList<List<Integer>>();
        ScheduledJob tmp = null;
        try
        {
            ResultSet rs = cnx.runSelect(query_key, args);
            while (rs.next())
            {
                // ID, CRON_EXPRESSION, JOBDEF, QUEUE, LAST_UPDATED
                tmp = new ScheduledJob();
                tmp.setId(rs.getInt(1));
                tmp.setCronExpression(rs.getString(2));
                tmp.setJobDefinition(rs.getInt(3));
                tmp.setQueue(rs.getInt(4) > 0 ? rs.getInt(4) : null);
                tmp.setPriority(rs.getInt(5) > 0 ? rs.getInt(5) : null);
                tmp.setLastUpdated(cnx.getCal(rs, 6));

                res.add(tmp);

                if (currentIdList == null || currentIdList.size() >= 500)
                {
                    currentIdList = new ArrayList<Integer>();
                    allIdLists.add(currentIdList);
                }
                currentIdList.add(tmp.id);
            }

            // Batch fetch parameters, 500 by 500.
            int currentSJ = 0;
            for (List<Integer> ids : allIdLists)
            {
                rs = cnx.runSelect("sjprm_select_for_sj_list", ids);

                while (rs.next())
                {
                    // ID, KEYNAME, VALUE, JOB_SCHEDULE
                    String key = rs.getString(2);
                    String val = rs.getString(3);
                    int sj = rs.getInt(4);

                    while (res.get(currentSJ).id != sj)
                    {
                        currentSJ++;
                    }
                    res.get(currentSJ).parametersCache.put(key, val);
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static int create(DbConn cnx, String cronExpression, Integer jobDefId, Integer queueId, Integer priority,
            Map<String, String> parameterOverloads)
    {
        QueryResult r = cnx.runUpdate("sj_insert", cronExpression, jobDefId, queueId, priority);

        for (Map.Entry<String, String> e : parameterOverloads.entrySet())
        {
            cnx.runUpdate("sjprm_insert", e.getKey(), e.getValue(), r.generatedKey);
        }

        return r.generatedKey;
    }
}
