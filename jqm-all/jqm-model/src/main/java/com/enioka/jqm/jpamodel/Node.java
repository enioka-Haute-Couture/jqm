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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.NonUniqueResultException;
import com.enioka.jqm.jdbc.QueryResult;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for storing the definition of the different nodes that are member of the JMQ cluster.<br>
 * There can be some confusion between terms: an <code>engine</code> is a Java process that represents a {@link Node}. There can only be one
 * engine running the same Node at the same time.<br>
 * <br>
 * A node is the holder of all the parameters needed for the engine to run: a list of {@link Queue}s to poll (through
 * {@link DeploymentParameter}s), the different TCP ports to use, etc.
 */
public class Node
{
    private Integer id;

    private String name;

    private String dns = "localhost";
    private Integer port;

    private String dlRepo;
    private String tmpDirectory;
    private String repo;
    private String exportRepo = "";

    private String rootLogLevel = "DEBUG";
    private Calendar lastSeenAlive;

    private Integer jmxRegistryPort = 0;
    private Integer jmxServerPort = 0;

    private Boolean loapApiSimple = true, loadApiClient = false, loadApiAdmin = false;

    private Boolean enabled = true;
    private boolean stop = false;

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * See {@link #getId()}
     */
    void setId(final Integer id)
    {
        this.id = id;
    }

    /**
     * The functional key of the node. When starting an engine, it is given this name as its only parameter. It must be unique.<br>
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
     * The TCP port used for starting the engine Jetty server (it holds the servlet API as well as the We Service API).
     */
    public Integer getPort()
    {
        return port;
    }

    /**
     * See {@link #getPort()}
     */
    public void setPort(final Integer port)
    {
        this.port = port;
    }

    /**
     * The directory that will store all the {@link Deliverable}s created by job instances.<br>
     * Max length is 1024.
     */
    public String getDlRepo()
    {
        return dlRepo;
    }

    /**
     * See {@link #getDlRepo()}
     */
    public void setDlRepo(final String dlRepo)
    {
        this.dlRepo = dlRepo;
    }

    /**
     * Directory holding the payload repository, i.e. all the jars that can be run by JQM.
     */
    public String getRepo()
    {
        return repo;
    }

    /**
     * See {@link #getRepo()}
     */
    public void setRepo(final String repo)
    {
        this.repo = repo;
    }

    /**
     * That field is polled be the engine: if true, it will stop at once. this is one of the three ways to send a stop order to an engine.
     */
    public boolean isStop()
    {
        return stop;
    }

    /**
     * See {@link #isStop()}
     */
    public void setStop(boolean stop)
    {
        this.stop = stop;
    }

    /**
     * The log level for the jqm.log file. Valid values are TRACE, DEBUG, INFO, WARN, ERROR, FATAL. Default is INFO.
     */
    public String getRootLogLevel()
    {
        return rootLogLevel;
    }

    /**
     * See {@link #getRootLogLevel()}
     */
    public void setRootLogLevel(String rootLogLevel)
    {
        this.rootLogLevel = rootLogLevel;
    }

    /**
     * @deprecated was never used
     */
    public String getExportRepo()
    {
        return exportRepo;
    }

    /**
     * @deprecated was never used
     */
    public void setExportRepo(String exportRepo)
    {
        this.exportRepo = exportRepo;
    }

    /**
     * The DNS name on which to create listeners. Default is localhost.
     */
    public String getDns()
    {
        return dns;
    }

    /**
     * See {@link #getDns()}
     */
    public void setDns(String dns)
    {
        this.dns = dns;
    }

    /**
     * Engine will periodically update this field, which can be used for monitoring. It is also used to prevent starting two engine on the
     * same node.
     */
    public Calendar getLastSeenAlive()
    {
        return lastSeenAlive;
    }

    /**
     * See {@link #getLastSeenAlive()}
     */
    public void setLastSeenAlive(Calendar lastSeenAlive)
    {
        this.lastSeenAlive = lastSeenAlive;
    }

    /**
     * The port on which to start the JMX remote registry. No remote JMX item is started if this field or jmxserverport is < 1
     */
    public Integer getJmxRegistryPort()
    {
        return jmxRegistryPort;
    }

    /**
     * See {@link #getJmxRegistryPort()}
     */
    public void setJmxRegistryPort(Integer jmxRegistryPort)
    {
        this.jmxRegistryPort = jmxRegistryPort;
    }

    /**
     * The port on which to start the JMX remote server. No remote JMX item is started if this field or jmxregistryport is < 1
     */
    public Integer getJmxServerPort()
    {
        return jmxServerPort;
    }

    /**
     * See {@link #getJmxServerPort()}
     */
    public void setJmxServerPort(Integer jmxServerPort)
    {
        this.jmxServerPort = jmxServerPort;
    }

    /**
     * If true, the basic REST services will start. Ignored if the GlobalParameter 'disableWsApiSimple' is true.
     */
    public Boolean getLoapApiSimple()
    {
        return loapApiSimple;
    }

    /**
     * See {@link #getLoapApiSimple()}
     */
    public void setLoapApiSimple(Boolean loapApiSimple)
    {
        this.loapApiSimple = loapApiSimple;
    }

    /**
     * If true, the client REST services will start. Ignored if the GlobalParameter 'disableWsApiClient' is true.
     */
    public Boolean getLoadApiClient()
    {
        return loadApiClient;
    }

    /**
     * See {@link #getLoadApiClient()}
     */
    public void setLoadApiClient(Boolean loadApiClient)
    {
        this.loadApiClient = loadApiClient;
    }

    /**
     * If true, the administration REST services will start. Ignored if the GlobalParameter 'disableWsApiAdmin' is true.
     */
    public Boolean getLoadApiAdmin()
    {
        return loadApiAdmin;
    }

    /**
     * See {@link #getLoadApiAdmin()}
     */
    public void setLoadApiAdmin(Boolean loadApiAdmin)
    {
        this.loadApiAdmin = loadApiAdmin;
    }

    /**
     * The root directory inside which temporary files will be created (see JobManager.getWorkDir).
     */
    public String getTmpDirectory()
    {
        return tmpDirectory;
    }

    /**
     * See {@link #getTmpDirectory()}
     */
    public void setTmpDirectory(String tmpDirectory)
    {
        this.tmpDirectory = tmpDirectory;
    }

    /**
     * Disabled means all the queue bindings still exist but no job instances are polled (pollers are paused, with already running job
     * instances going on normally).
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

    /**
     * Create a new entry in the database. No commit performed.
     */
    public static Node create(DbConn cnx, String nodeName, Integer port, String dlRepo, String repo, String tmpDir, String dns)
    {
        QueryResult r = cnx.runUpdate("node_insert", dlRepo, dns, true, null, 0, 0, false, false, false, nodeName, port, repo, "DEBUG",
                false, tmpDir);
        Node res = new Node();
        res.id = r.getGeneratedId();
        res.name = nodeName;
        res.dns = dns;
        res.port = port;
        res.dlRepo = dlRepo;
        res.tmpDirectory = tmpDir;
        res.repo = repo;
        res.loadApiAdmin = false;
        res.loadApiClient = false;
        res.loapApiSimple = false;
        res.enabled = true;
        res.stop = false;

        return res;
    }

    static Node map(ResultSet rs, int colShift)
    {
        try
        {
            Node tmp = new Node();

            tmp.id = rs.getInt(1 + colShift);
            tmp.dlRepo = rs.getString(2 + colShift);
            tmp.dns = rs.getString(3 + colShift);
            tmp.enabled = rs.getBoolean(4 + colShift);
            tmp.exportRepo = rs.getString(5 + colShift);
            tmp.jmxRegistryPort = rs.getInt(6 + colShift);
            tmp.jmxServerPort = rs.getInt(7 + colShift);
            tmp.loadApiAdmin = rs.getBoolean(8 + colShift);
            tmp.loadApiClient = rs.getBoolean(9 + colShift);
            tmp.loapApiSimple = rs.getBoolean(10 + colShift);
            tmp.name = rs.getString(11 + colShift);
            tmp.port = rs.getInt(12 + colShift);
            tmp.repo = rs.getString(13 + colShift);
            tmp.rootLogLevel = rs.getString(14 + colShift);
            tmp.stop = rs.getBoolean(15 + colShift);
            tmp.tmpDirectory = rs.getString(16 + colShift);

            Calendar c = null;
            if (rs.getTimestamp(17 + colShift) != null)
            {
                c = Calendar.getInstance();
                c.setTimeInMillis(rs.getTimestamp(17 + colShift).getTime());
            }
            tmp.lastSeenAlive = c;

            return tmp;
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
    }

    public static List<Node> select(DbConn cnx, String query_key, Object... args)
    {
        List<Node> res = new ArrayList<Node>();
        try
        {
            ResultSet rs = cnx.runSelect(query_key, args);
            while (rs.next())
            {
                res.add(map(rs, 0));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static Node select_single(DbConn cnx, String query_key, Object... args)
    {
        List<Node> nn = select(cnx, query_key, args);
        if (nn.size() == 0)
        {
            throw new NoResultException("No node with this ID");
        }
        if (nn.size() > 1)
        {
            throw new NonUniqueResultException("COnfiguration is broken: multiple nodes with the same ID");
        }
        return nn.get(0);
    }
}
