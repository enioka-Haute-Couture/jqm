package com.enioka.api.admin;

import java.io.Serializable;

public class UsageStatsDto implements Serializable
{
    private Integer activeNodes;
    private Integer queues;
    private Integer pausedJobs;
    private Integer submittedJobs;
    private Integer runningJobs;

    public Integer getActiveNodes()
    {
        return activeNodes;
    }

    public void setActiveNodes(Integer activeNodes)
    {
        this.activeNodes = activeNodes;
    }

    public Integer getQueues()
    {
        return queues;
    }

    public void setQueues(Integer queues)
    {
        this.queues = queues;
    }

    public Integer getPausedJobs()
    {
        return pausedJobs;
    }

    public void setPausedJobs(Integer pausedJobs)
    {
        this.pausedJobs = pausedJobs;
    }

    public Integer getSubmittedJobs()
    {
        return submittedJobs;
    }

    public void setSubmittedJobs(Integer submittedJobs)
    {
        this.submittedJobs = submittedJobs;
    }

    public Integer getRunningJobs()
    {
        return runningJobs;
    }

    public void setRunningJobs(Integer runningJobs)
    {
        this.runningJobs = runningJobs;
    }
}
