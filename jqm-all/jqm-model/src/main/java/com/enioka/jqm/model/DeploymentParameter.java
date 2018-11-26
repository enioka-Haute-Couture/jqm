/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.QueryResult;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for associating {@link Node} with {@link Queue}, specifying the max number of concurrent instances and polling
 * interval.
 */
public class DeploymentParameter
{
    private Integer id;
    private Integer classId;
    private int node;
    private int nbThread;
    private int pollingInterval;
    private int queue;
    private boolean enabled = true;
    private Calendar lastModified;

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * Should never be called. See {@link #getId()}
     */
    void setId(final Integer id)
    {
        this.id = id;
    }

    /**
     * @deprecated was never used
     */
    public Integer getClassId()
    {
        return classId;
    }

    /**
     * @deprecated was never used
     */
    public void setClassId(final Integer classId)
    {
        this.classId = classId;
    }

    /**
     * The maximum of concurrent {@link JobInstance} executions for the {@link Queue} designated by {@link #getQueue()} on the {@link Node}
     * designated by {@link #getNode()}. The queue is considered "full" once there as many active executions as this number and the engine
     * will ignore new execution requests until a running {@link JobInstance} ends.
     */
    public Integer getNbThread()
    {
        return nbThread;
    }

    /**
     * See {@link #getNbThread()}
     */
    public void setNbThread(final Integer nbThread)
    {
        this.nbThread = nbThread;
    }

    /**
     * The {@link Node} that will have to poll the {@link Queue} designated by {@link #getQueue()} for new {@link JobInstance}s to run.
     */
    public int getNode()
    {
        return node;
    }

    /**
     * See {@link #setNode(Node)}
     */
    public void setNode(final int node)
    {
        this.node = node;
    }

    /**
     * The period in milliseconds between two peeks on the {@link Queue} designated by {@link #getQueue()} (looking for {@link JobInstance}s
     * to run). Reasonable minimum is 1000 (1s).
     */
    public Integer getPollingInterval()
    {
        return pollingInterval;
    }

    /**
     * See {@link #getPollingInterval()}
     */
    public void setPollingInterval(final Integer pollingInterval)
    {
        this.pollingInterval = pollingInterval;
    }

    /**
     * The {@link Queue} that will have to be polled by the {@link Node} designated by {@link #getNode()} for new {@link JobInstance}s to
     * run.
     */
    public int getQueue()
    {
        return queue;
    }

    /**
     * See {@link #getQueue()}
     */
    public void setQueue(final int queue)
    {
        this.queue = queue;
    }

    /**
     * When the object was last modified. Read only.
     */
    public Calendar getLastModified()
    {
        return lastModified;
    }

    /**
     * See {@link #getLastModified()}
     */
    protected void setLastModified(Calendar lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * Disabled means the binding still exists but no job instances are polled (poller is paused, with already running job instances going
     * on normally).
     */
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * See {@link #getEnabled()}
     */
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public static DeploymentParameter create(DbConn cnx, Node node, Integer nbThread, Integer pollingInterval, Integer queueId)
    {
        return create(cnx, node.getId(), nbThread, pollingInterval, queueId);
    }

    /**
     * Create a new entry in the database. No commit performed.
     */
    public static DeploymentParameter create(DbConn cnx, Boolean enabled, Integer nodeId, Integer nbThread, Integer pollingInterval,
            Integer qId)
    {
        QueryResult r = cnx.runUpdate("dp_insert", enabled, nbThread, pollingInterval, nodeId, qId);
        DeploymentParameter res = new DeploymentParameter();
        res.id = r.getGeneratedId();
        res.node = nodeId;
        res.nbThread = nbThread;
        res.pollingInterval = pollingInterval;
        res.queue = qId;

        return res;
    }

    public static DeploymentParameter create(DbConn cnx, Integer nodeId, Integer nbThread, Integer pollingInterval, Integer qId)
    {
        return create(cnx, true, nodeId, nbThread, pollingInterval, qId);
    }

    public static List<DeploymentParameter> select(DbConn cnx, String query_key, Object... args)
    {
        List<DeploymentParameter> res = new ArrayList<>();
        try
        {
            ResultSet rs = cnx.runSelect(query_key, args);
            while (rs.next())
            {
                DeploymentParameter tmp = new DeploymentParameter();

                tmp.id = rs.getInt(1);
                tmp.enabled = rs.getBoolean(2);

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(rs.getTimestamp(3).getTime());
                tmp.lastModified = c;

                tmp.nbThread = rs.getInt(4);
                tmp.pollingInterval = rs.getInt(5);
                tmp.node = rs.getInt(6);
                tmp.queue = rs.getInt(7);

                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }
}
