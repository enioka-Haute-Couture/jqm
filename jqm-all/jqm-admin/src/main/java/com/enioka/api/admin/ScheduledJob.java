package com.enioka.api.admin;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * A simple (cron-like) scheduling of a {@link JobDefDto}.
 *
 */
@XmlRootElement
public class ScheduledJob implements Serializable
{
    private static final long serialVersionUID = 7928212054684657247L;

    private Long id = null;

    private String cronExpression;

    private Calendar lastUpdated;

    private Long queue;

    private Integer priority;

    private Map<String, String> parameters = new HashMap<>();

    /**
     * Fluent API builder.
     *
     * @param cronExpression
     * @return
     */
    public static ScheduledJob create(String cronExpression)
    {
        ScheduledJob res = new ScheduledJob();
        res.cronExpression = cronExpression;
        return res;
    }

    /**
     * Unique key of the schedule job. If null, this is considered a new scheduled job.<br>
     * Note that the ScheduledJob is a part of the JobDefDto and yet it has a unique key. This is because we want to track updates to the
     * schedules individually, and not only the updates to the job as a whole.
     *
     * @return the ID.
     */
    public Long getId()
    {
        return this.id;
    }

    /**
     * See {@link #getId()}
     *
     * @param id
     */
    public ScheduledJob setId(Long id)
    {
        this.id = id;
        return this;
    }

    /**
     * A valid cron expression.
     *
     * @return
     */
    public String getCronExpression()
    {
        return cronExpression;
    }

    /**
     * See {@link #getCronExpression()}.
     *
     * @param cronExpression
     */
    public ScheduledJob setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
        return this;
    }

    /**
     * This date is updated whenever the object (or one of its parameters) gets updated. This allows easier parameter sync.
     *
     * @return
     */
    public Calendar getLastUpdated()
    {
        return lastUpdated;
    }

    /**
     * See {@link #getLastUpdated()}
     *
     * @param lastUpdated
     */
    public void setLastUpdated(Calendar lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    /**
     * A scheduled job can override the default queue of its {@link JobDefDto}. If null, the {@link JobDefDto} default queue is used (or the
     * global default queue if it has none).
     *
     * @return
     */
    public Long getQueue()
    {
        return queue;
    }

    /**
     * See {@link #getQueue()}
     *
     * @param queue
     */
    public ScheduledJob setQueue(Long queue)
    {
        this.queue = queue;
        return this;
    }

    /**
     * A scheduled job can override the default parameters (and add new ones, but not remove any) of its {@link JobDefDto}. Can be empty,
     * but not null.
     *
     * @return a copy of the parameter map.
     */
    public Map<String, String> getParameters()
    {
        return new HashMap<>(parameters);
    }

    /**
     * See {@link #getParameters()}.
     *
     * @param parameters
     */
    public void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Add a parameter to the override parameters. See {@link #getParameters()}.
     *
     * @param key
     * @param value
     * @return
     */
    public ScheduledJob addParameter(String key, String value)
    {
        this.parameters.put(key, value);
        return this;
    }

    public ScheduledJob removeParameter(String key)
    {
        this.parameters.remove(key);
        return this;
    }

    /**
     * The default priority for job instances created from this scheduled job. Null if the default.
     *
     */
    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }
}
