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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.QueryResult;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for storing the default parameters of a {@link JobDef}, i.e. key/value pairs that should be present for all instances
 * created from a JobDef (and may be overloaded).<br>
 * When a {@link JobDef} is instantiated, {@link RuntimeParameter}s are created from {@link JobDefParameter}s as well as parameters
 * specified inside the execution request and associated to the {@link JobInstance}. Therefore, this table is purely metadata and is never
 * used in TP processing.
 */
public class JobDefParameter implements Serializable
{
    private static final long serialVersionUID = -5308516206913425230L;

    private long id;

    private String key;
    private String value;

    private long jobdef_id;

    /**
     * The name of the parameter.<br>
     * Max length is 50.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * See {@link #getKey()}
     */
    public void setKey(final String key)
    {
        this.key = key;
    }

    /**
     * Value of the parameter.<br>
     * Max length is 1000.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * See {@link #getValue()}
     */
    public void setValue(final String value)
    {
        this.value = value;
    }

    /**
     * A technical ID without special meaning.
     */
    public long getId()
    {
        return id;
    }

    public static List<JobDefParameter> select(DbConn cnx, String query_key, Object... args)
    {
        List<JobDefParameter> res = new ArrayList<>();
        try (ResultSet rs = cnx.runSelect(query_key, args))
        {
            while (rs.next())
            {
                JobDefParameter tmp = new JobDefParameter();

                tmp.id = rs.getLong(1);
                tmp.key = rs.getString(2);
                tmp.value = rs.getString(3);
                tmp.jobdef_id = rs.getLong(4);

                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static Map<Long, List<JobDefParameter>> select_all(DbConn cnx, String query_key, Object... args)
    {
        Map<Long, List<JobDefParameter>> res = new HashMap<>();
        try (ResultSet rs = cnx.runSelect(query_key, args))
        {
            while (rs.next())
            {
                JobDefParameter tmp = new JobDefParameter();

                tmp.id = rs.getLong(1);
                tmp.key = rs.getString(2);
                tmp.value = rs.getString(3);
                tmp.jobdef_id = rs.getLong(4);

                List<JobDefParameter> list = res.get(tmp.jobdef_id);
                if (list == null)
                {
                    list = new ArrayList<>();
                    res.put(tmp.jobdef_id, list);
                }

                list.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static Map<String, String> select_map(DbConn cnx, String query_key, Object... args)
    {
        Map<String, String> res = new HashMap<>();
        try (ResultSet rs = cnx.runSelect(query_key, args))
        {
            while (rs.next())
            {
                res.put(rs.getString(2), rs.getString(3));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static long create(DbConn cnx, String key, String value, long jdId)
    {
        QueryResult qr = cnx.runUpdate("jdprm_insert", key, value, jdId);
        return qr.getGeneratedId();
    }
}
