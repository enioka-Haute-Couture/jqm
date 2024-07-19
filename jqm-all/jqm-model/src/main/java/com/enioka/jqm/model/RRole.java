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
import java.util.List;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.QueryResult;

public class RRole implements Serializable
{
    private static final long serialVersionUID = 1234354709423603792L;

    private long id;
    private String name;
    private String description;

    public long getId()
    {
        return id;
    }

    void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<RUser> getUsers(DbConn cnx)
    {
        return RUser.select(cnx, "user_select_all_in_role", this.id);
    }

    public List<RPermission> getPermissions(DbConn cnx)
    {
        return RPermission.select(cnx, "perm_select_all_in_role", this.id);
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public static List<RRole> select(DbConn cnx, String query_key, Object... args)
    {
        List<RRole> res = new ArrayList<>();
        try (ResultSet rs = cnx.runSelect(query_key, args))
        {
            while (rs.next())
            {
                RRole tmp = new RRole();

                tmp.id = rs.getLong(1);
                tmp.name = rs.getString(2);
                tmp.description = rs.getString(3);

                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static void create(DbConn cnx, String roleName, String description, String... permissions)
    {
        QueryResult r = cnx.runUpdate("role_insert", description, roleName);
        long newId = r.getGeneratedId();

        for (String s : permissions)
        {
            cnx.runUpdate("perm_insert", s, newId);
        }
    }

}
