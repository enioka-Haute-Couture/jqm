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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.naming.spi.ObjectFactory;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.NonUniqueResultException;
import com.enioka.jqm.jdbc.QueryResult;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for storing the JNDI object resources. This table is the actual JNDI directory back-end.
 */
public class JndiObjectResource implements Serializable
{
    private static final long serialVersionUID = 5387852232057745693L;

    private int id;
    private String name;
    private String auth = null;
    private String type;
    private String factory;
    private String description;
    private String template = null;
    private Boolean singleton = false;

    private Calendar lastModified;

    /**
     * If true: loaded by the engine CL and cached. If not, loaded by payload CL and created on each lookup call.
     */
    public Boolean getSingleton()
    {
        return singleton;
    }

    /**
     * See {@link #getSingleton()}
     */
    public void setSingleton(Boolean singleton)
    {
        this.singleton = singleton;
    }

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public int getId()
    {
        return id;
    }

    void setId(final int id)
    {
        this.id = id;
    }

    /**
     * JNDI alias. JQM interprets all names as global aliases (no subcontexts). E.g.: jms/myqueueconnectionfactory.<br>
     * Max length is 100.
     */
    public String getName()
    {
        return name;
    }

    /**
     * See {@link #getName()}
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Not used in JQM. Here for completion sake (possible values: Container, ?). JQM always assumes Container.
     */
    public String getAuth()
    {
        return auth;
    }

    /**
     * See {@link #getAuth()}
     */
    public void setAuth(final String auth)
    {
        this.auth = auth;
    }

    /**
     * Class name of the resource, i.e. the class that should be returned by a <code>lookup</code> call. E.g.:
     * <code>com.ibm.mq.jms.MQQueueConnectionFactory</code><br>
     * Max length is 100.
     */
    public String getType()
    {
        return type;
    }

    public void setType(final String type)
    {
        this.type = type;
    }

    /**
     * Class name of the factory which will create the resource. It must implement {@link ObjectFactory} and respect the JNDI ObjectFactory
     * conventions.<br>
     * Max length is 100.
     */
    public String getFactory()
    {
        return factory;
    }

    /**
     * See {@link #getFactory()}
     */
    public void setFactory(final String factory)
    {
        this.factory = factory;
    }

    /**
     * A free text description. Max length is 250.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * See {@link #getDescription()}
     */
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * The parameters. These are specific to each resource type (i.e. each factory as specified inside {@link #getFactory()} has its own
     * parameter needs). E.g. for MQSeries: HOST, PORT, CHAN, TRAN, QMGR, ...
     */
    public Collection<JndiObjectResourceParameter> getParameters(DbConn cnx)
    {
        ResultSet rs = cnx.runSelect("jndiprm_select_all_in_jndisrc", this.id);
        List<JndiObjectResourceParameter> res = new ArrayList<>();
        JndiObjectResourceParameter tmp = null;
        try
        {
            while (rs.next())
            {
                tmp = new JndiObjectResourceParameter();
                tmp.setId(rs.getInt(1));
                tmp.setKey(rs.getString(2));
                tmp.setLastModified(cnx.getCal(rs, 3));
                tmp.setValue(rs.getString(4));

                res.add(tmp);
            }
        }
        catch (Exception e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    /**
     * This is an optional tag to be used by user interfaces so as to identify how to display the resource
     */
    public String getTemplate()
    {
        return template;
    }

    /**
     * See {@link #getTemplate()}
     */
    public void setTemplate(String template)
    {
        this.template = template;
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

    public static int create(DbConn cnx, String jndiAlias, String className, String factoryClass, String description, boolean singleton,
            Map<String, String> parameters)
    {
        QueryResult r = cnx.runUpdate("jndi_insert", description, factoryClass, jndiAlias, singleton, (String) null, className);
        int newId = r.getGeneratedId();

        for (Map.Entry<String, String> prms : parameters.entrySet())
        {
            cnx.runUpdate("jndiprm_insert", prms.getKey(), prms.getValue(), newId);
        }
        return newId;
    }

    public static List<JndiObjectResource> select(DbConn cnx, String query_key, Object... args)
    {
        List<JndiObjectResource> res = new ArrayList<>();
        try
        {
            ResultSet rs = cnx.runSelect(query_key, args);
            while (rs.next())
            {
                JndiObjectResource tmp = new JndiObjectResource();

                tmp.id = rs.getInt(1);
                tmp.name = rs.getString(2);
                tmp.auth = rs.getString(3);
                tmp.type = rs.getString(4);
                tmp.factory = rs.getString(5);
                tmp.description = rs.getString(6);
                tmp.template = rs.getString(7);
                tmp.singleton = rs.getBoolean(8);
                tmp.lastModified = cnx.getCal(rs, 9);

                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static JndiObjectResource select_alias(DbConn cnx, String alias)
    {
        List<JndiObjectResource> res = select(cnx, "jndi_select_by_key", alias);
        if (res.isEmpty())
        {
            throw new NoResultException("no result for query by key for key " + alias);
        }
        if (res.size() > 1)
        {
            throw new NonUniqueResultException("Inconsistent database! Multiple results for query by key for key " + alias);
        }
        return res.get(0);
    }
}
