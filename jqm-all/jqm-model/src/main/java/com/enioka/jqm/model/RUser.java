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
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.NonUniqueResultException;
import com.enioka.jqm.jdbc.QueryResult;

public class RUser implements Serializable
{
    private static final long serialVersionUID = 1234354709423603792L;

    private long id;

    private String login;

    private String password;
    private String hashSalt;

    private Boolean locked = false;
    private Calendar expirationDate;
    private Calendar creationDate = Calendar.getInstance();
    private Calendar lastModified;

    private String email;
    private String freeText;

    private Boolean internal = false;

    public long getId()
    {
        return id;
    }

    void setId(long id)
    {
        this.id = id;
    }

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public List<RRole> getRoles(DbConn cnx)
    {
        return RRole.select(cnx, "role_select_all_for_user", this.id);
    }

    public String getHashSalt()
    {
        return hashSalt;
    }

    public void setHashSalt(String hashSalt)
    {
        this.hashSalt = hashSalt;
    }

    public Boolean getLocked()
    {
        return locked;
    }

    public void setLocked(Boolean locked)
    {
        this.locked = locked;
    }

    public Calendar getExpirationDate()
    {
        return expirationDate;
    }

    public void setExpirationDate(Calendar expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public Calendar getCreationDate()
    {
        return creationDate;
    }

    void setCreationDate(Calendar creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getFreeText()
    {
        return freeText;
    }

    public void setFreeText(String freeText)
    {
        this.freeText = freeText;
    }

    public Boolean getInternal()
    {
        return internal;
    }

    public void setInternal(Boolean internal)
    {
        this.internal = internal;
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

    public static List<RUser> select(DbConn cnx, String query_key, Object... args)
    {
        List<RUser> res = new ArrayList<>();
        try (ResultSet rs = cnx.runSelect(query_key, args))
        {
            while (rs.next())
            {
                RUser tmp = new RUser();

                tmp.id = rs.getLong(1);
                tmp.login = rs.getString(2);
                tmp.password = rs.getString(3);
                tmp.hashSalt = rs.getString(4);
                tmp.locked = rs.getBoolean(5);
                tmp.expirationDate = cnx.getCal(rs, 6);
                tmp.creationDate = cnx.getCal(rs, 7);
                tmp.lastModified = cnx.getCal(rs, 8);
                tmp.email = rs.getString(9);
                tmp.freeText = rs.getString(10);
                tmp.internal = rs.getBoolean(11);

                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static void create(DbConn cnx, String login, String password_hash, String password_salt, String... role_names)
    {
        create(cnx, login, password_hash, password_salt, null, false, role_names);
    }

    public static long create(DbConn cnx, String login, String password_hash, String password_salt, Calendar expiration, Boolean internal,
                              String... role_names)
    {
        QueryResult r = cnx.runUpdate("user_insert", null, expiration, null, password_salt, internal, false, login, password_hash);
        long newId = r.getGeneratedId();

        for (String s : role_names)
        {
            cnx.runUpdate("user_add_role_by_name", newId, s);
        }
        return newId;
    }

    public static void set_roles(DbConn cnx, int userId, String... role_names)
    {
        cnx.runUpdate("user_remove_all_roles_by_id", userId);
        Set<String> roles = new HashSet<>();
        for (String s : role_names)
        {
            roles.add(s);
        }

        for (String s : roles)
        {
            cnx.runUpdate("user_add_role_by_name", userId, s);
        }
    }

    public static RUser select_id(DbConn cnx, long id)
    {
        List<RUser> res = select(cnx, "user_select_by_id", id);
        if (res.isEmpty())
        {
            throw new DatabaseException("no result for query by key for key " + id);
        }
        if (res.size() > 1)
        {
            throw new DatabaseException("Inconsistent database! Multiple results for query by key for key " + id);
        }
        return res.get(0);
    }

    public static RUser selectlogin(DbConn cnx, String id)
    {
        List<RUser> res = select(cnx, "user_select_by_key", id);
        if (res.isEmpty())
        {
            throw new NoResultException("no result for query by key for key " + id);
        }
        if (res.size() > 1)
        {
            throw new NonUniqueResultException("Inconsistent database! Multiple results for query by key for key " + id);
        }
        return res.get(0);
    }
}
