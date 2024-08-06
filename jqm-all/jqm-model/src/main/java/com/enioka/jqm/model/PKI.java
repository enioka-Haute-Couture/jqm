/**
 * Copyright © 2013 enioka. All rights reserved
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
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.NonUniqueResultException;
import com.enioka.jqm.jdbc.QueryResult;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for storing the PKI root key.
 */
public class PKI implements Serializable
{
    private static final long serialVersionUID = -1830546620049033739L;

    private long id;
    private String prettyName;

    private String pemPK;
    private String pemCert;

    public long getId()
    {
        return id;
    }

    void setId(long id)
    {
        this.id = id;
    }

    public String getPrettyName()
    {
        return prettyName;
    }

    public void setPrettyName(String prettyName)
    {
        this.prettyName = prettyName;
    }

    public String getPemPK()
    {
        return pemPK;
    }

    public void setPemPK(String pemPK)
    {
        this.pemPK = pemPK;
    }

    public String getPemCert()
    {
        return pemCert;
    }

    public void setPemCert(String pemCert)
    {
        this.pemCert = pemCert;
    }

    public static List<PKI> select(DbConn cnx, String query_key, Object... args)
    {
        List<PKI> res = new ArrayList<>();
        try (ResultSet rs = cnx.runSelect(query_key, args))
        {
            while (rs.next())
            {
                PKI tmp = new PKI();

                tmp.id = rs.getLong(1);
                tmp.pemCert = rs.getString(2);
                tmp.pemPK = rs.getString(3);
                tmp.prettyName = rs.getString(4);

                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static PKI select_key(DbConn cnx, String key)
    {
        List<PKI> pp = select(cnx, "pki_select_by_key", key);
        if (pp.size() == 0)
        {
            throw new NoResultException("No PKI with key " + key);
        }
        if (pp.size() > 1)
        {
            throw new NonUniqueResultException("Configuration is not valid");
        }

        return pp.get(0);
    }

    public static long create(DbConn cnx, String alias, String pemPK, String pemCert)
    {
        QueryResult qr = cnx.runUpdate("pki_insert", pemCert, pemPK, alias);
        return qr.getGeneratedId();
    }
}
