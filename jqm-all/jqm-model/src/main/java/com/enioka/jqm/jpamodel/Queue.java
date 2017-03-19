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

package com.enioka.jqm.jpamodel;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.QueryResult;

public class Queue implements Serializable
{
    private static final long serialVersionUID = 4677042929807285233L;

    private Integer id = null;

    private String name;
    private String description;

    private Integer timeToLive = 0;

    private boolean defaultQueue;

    /**
     * Functional key. Queues are specified by name inside all APIs. Must be unique.<br>
     * Max length is 50.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Mandatory description.<br>
     * Max length is 1000.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * See {@link #getName()}
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * See {@link #getDescription()}
     */
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public int getId()
    {
        return id;
    }

    /**
     * See {@link #getId()}
     */
    void setId(final int id)
    {
        this.id = id;
    }

    /**
     * There is only one (and always one) queue which is the "default queue", which is used for operations requiring a queue when no queue
     * is specified.
     */
    public boolean isDefaultQueue()
    {
        return defaultQueue;
    }

    /**
     * See {@link #isDefaultQueue()}
     */
    public void setDefaultQueue(final boolean defaultQueue)
    {
        this.defaultQueue = defaultQueue;
    }

    /**
     * Not used for now. Reserved. Should be the max time to wait inside the queue.
     */
    public Integer getTimeToLive()
    {
        return timeToLive;
    }

    /**
     * See {@link #getTimeToLive()}
     */
    public void setTimeToLive(Integer timeToLive)
    {
        this.timeToLive = timeToLive;
    }

    /**
     * Create a new entry in the database. No commit performed.
     */
    public static Integer create(DbConn cnx, String name, String description, boolean defaultQ)
    {
        QueryResult r = cnx.runUpdate("q_insert", defaultQ, name, description);
        Queue res = new Queue();
        res.id = r.getGeneratedId();
        res.name = name;
        res.description = description;
        res.defaultQueue = defaultQ;
        return res.id;
    }

    static Queue map(ResultSet rs, int colShift)
    {
        try
        {
            Queue tmp = new Queue();
            tmp.id = rs.getInt(1 + colShift);
            tmp.defaultQueue = rs.getBoolean(2 + colShift);
            tmp.description = rs.getString(3 + colShift);
            tmp.name = rs.getString(4 + colShift);
            return tmp;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

    public static List<Queue> select(DbConn cnx, String query_key, Object... args)
    {
        List<Queue> res = new ArrayList<Queue>();
        try
        {
            ResultSet rs = cnx.runSelect(query_key, args);
            while (rs.next())
            {
                Queue tmp = map(rs, 0);
                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static Queue select_key(DbConn cnx, String name)
    {
        List<Queue> res = select(cnx, "q_select_by_key", name);
        if (res.isEmpty())
        {
            throw new DatabaseException("no result for query by key for key " + name);
        }
        if (res.size() > 1)
        {
            throw new DatabaseException("Inconsistent database! Multiple results for query by key for key " + name);
        }
        return res.get(0);
    }

    public void update(DbConn cnx)
    {
        if (this.id == null)
        {
            create(cnx, name, description, defaultQueue);
        }
        else
        {
            cnx.runUpdate("q_update_all_fields_by_id", defaultQueue, description, name, id);
        }

    }
}
