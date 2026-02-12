package com.enioka.jqm.client.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The definition of a recurrence associated with a job definition. This is a read only object designed to help with calling some
 * {@link JqmClient} verbs.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Schedule implements Serializable
{
    /** Default Constructor. **/
    public Schedule()
    {}

    private static final long serialVersionUID = -8520223502712009225L;

    private long id;
    private String cronExpression;
    private Integer priority;
    private Queue queue;
    private Map<String, String> parameters = new HashMap<>();

    /**
     * The schedule ID. This is the same ID returned by {@link JobRequest#enqueue()}.
     *
     * @return the schedule ID
     */
    public long getId()
    {
        return id;
    }

    /**
     * Sets the schedule ID.
     *
     * @see #getId()
     * @param id
     *            the schedule ID
     */
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * The recurrence expression, in cron syntax.
     *
     * @return the cron expression
     */
    public String getCronExpression()
    {
        return cronExpression;
    }

    /**
     * Sets the recurrence expression.
     *
     * @see #getCronExpression()
     * @param cronExpression
     *            the cron expression
     */
    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    /**
     * Optional. This is an overload of the default priority of the job definition (itself optional).
     *
     * @return the priority
     */
    public Integer getPriority()
    {
        return priority;
    }

    /**
     * Sets the priority.
     *
     * @see #getPriority()
     * @param priority
     *            the priority
     */
    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }

    /**
     * Optional. This is an overload of the default queue of the job definition.
     *
     * @return the queue
     */
    public Queue getQueue()
    {
        return queue;
    }

    /**
     * Sets the queue.
     *
     * @see #getQueue()
     * @param queue
     *            the queue
     */
    public void setQueue(Queue queue)
    {
        this.queue = queue;
    }

    /**
     * A set of parameters (key/value pairs) which are available to job instances at runtime. Parameters can be defined at multiple levels:
     * here, inside the job definition, and inside the {@link JobRequest}. In case a parameter key exists at multiple levels, the parameter
     * defined here has the least priority.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * Sets the parameters.
     *
     * @see #getParameters()
     * @param parameters
     *            the parameters
     */
    public void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

}
