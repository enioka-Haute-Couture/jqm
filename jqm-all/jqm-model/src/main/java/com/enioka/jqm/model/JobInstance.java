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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.QueryResult;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for storing the execution requests. Said otherwise, <strong>this table holds the contents of the execution
 * queues</strong>.
 */
public class JobInstance implements Serializable
{
    private static final long serialVersionUID = -7710486847228806301L;

    private long id;

    private long jd_id;
    private long queue_id;
    private Long node_id;
    private State state;
    private Instruction instruction;

    private double internalPosition;
    private int priority;
    private boolean highlander;
    private boolean fromSchedule;

    private Long parentId;
    private String email;
    private Integer progress;

    private Calendar creationDate;
    private Calendar attributionDate;
    private Calendar executionDate;
    private Calendar notBefore;

    private String userName;
    private String sessionID;
    private String instanceApplication;
    private String instanceModule;
    private String instanceKeyword1;
    private String instanceKeyword2;
    private String instanceKeyword3;

    private transient JobDef jd;
    private transient Queue q;
    private transient Node n;

    private HashMap<String, String> prmCache;
    private HashMap<String, String> envVarCache;

    /**
     * Helper method to add a parameter without having to create it explicitely. The created parameter should be persisted afterwards.
     *
     * @param key
     *                  name of the parameter to add
     * @param value
     *                  value of the parameter to create
     * @return the newly created parameter
     */
    public RuntimeParameter addParameter(String key, String value)
    {
        RuntimeParameter jp = new RuntimeParameter();
        jp.setJi(this.getId());
        jp.setKey(key);
        jp.setValue(value);
        return jp;
    }

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public long getId()
    {
        return id;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getSessionID()
    {
        return sessionID;
    }

    /**
     * The {@link JobDef} from which this {@link JobInstance} was instantiated.
     */
    public long getJdId()
    {
        return jd_id;
    }

    public JobDef getJD()
    {
        return jd;
    }

    public Queue getQ()
    {
        return q;
    }

    /**
     * See {@link #getJdId()}
     */
    public void setJd(final long jd)
    {
        this.jd_id = jd;
    }

    /**
     * The current status of the request. See {@link State}.
     */
    public State getState()
    {
        return state;
    }

    /**
     * See {@link #getState()}
     */
    public void setState(State state)
    {
        this.state = state;
    }

    /**
     * See {@link #getUserName()}
     */
    public void setUserName(final String user)
    {
        this.userName = user;
    }

    /**
     * See {@link #getSessionID()}
     */
    public void setSessionID(final String sessionID)
    {
        this.sessionID = sessionID;
    }

    /**
     * The {@link Queue} on which is the {@link JobInstance} should wait. Cannot be changed once the status is ATTRIBUTED (i.e. wait is
     * over).
     */
    public long getQueue()
    {
        return queue_id;
    }

    /**
     * See {@link #getQueue()}
     */
    public void setQueue(final long queue)
    {
        this.queue_id = queue;
    }

    /**
     * The node that is running the {@link JobInstance}. Null until wait is over and status is ATTRIBUTED.
     */
    public Node getNode()
    {
        return n;
    }

    /**
     * See {@link #getNode()}
     */
    public void setNode(final Long node)
    {
        this.node_id = node;
    }

    /**
     * See {@link #getNode()}
     */
    public void setNode(final Node node)
    {
        this.n = node;
        this.node_id = node.getId();
    }

    /**
     * Null by default. If specified, an e-mail will be sent to this address at run end.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * See {@link #getEmail()}
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * User code may signal its progress through this integer. Purely optional.
     */
    public Integer getProgress()
    {
        return progress;
    }

    /**
     * See {@link #getProgress()}
     */
    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }

    /**
     * Technical queue ordering field.
     */
    public double getInternalPosition()
    {
        return internalPosition;
    }

    /**
     * See {@link #getInternalPosition()}
     */
    public void setInternalPosition(double internalPosition)
    {
        this.internalPosition = internalPosition;
    }

    /**
     * Only set when a job request is created by a running job, in which case it contains the job {@link JobInstance} ID.
     */
    public Long getParentId()
    {
        return parentId;
    }

    /**
     * See {@link #getParentId()}
     */
    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    /**
     * Time at which this {@link JobInstance} was committed inside the database.
     */
    public Calendar getCreationDate()
    {
        return creationDate;
    }

    /**
     * See {@link #getCreationDate()}
     */
    public void setCreationDate(Calendar creationDate)
    {
        this.creationDate = creationDate;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getApplication()
    {
        return instanceApplication;
    }

    /**
     * See {@link #getApplication()}
     */
    public void setApplication(String application)
    {
        this.instanceApplication = application;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getModule()
    {
        return instanceModule;
    }

    /**
     * See {@link #getModule()}
     */
    public void setModule(String module)
    {
        this.instanceModule = module;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getKeyword1()
    {
        return instanceKeyword1;
    }

    /**
     * See {@link #getKeyword1()}
     */
    public void setKeyword1(String keyword1)
    {
        this.instanceKeyword1 = keyword1;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getKeyword2()
    {
        return instanceKeyword2;
    }

    /**
     * See {@link #getKeyword2()}
     */
    public void setKeyword2(String keyword2)
    {
        this.instanceKeyword2 = keyword2;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getKeyword3()
    {
        return instanceKeyword3;
    }

    /**
     * See {@link #getKeyword3()}
     */
    public void setKeyword3(String keyword3)
    {
        this.instanceKeyword3 = keyword3;
    }

    void setId(long id)
    {
        this.id = id;
    }

    /**
     * Time at which the execution request entered the RUNNING status - a few milliseconds before actual execution.
     */
    public Calendar getExecutionDate()
    {
        return executionDate;
    }

    /**
     * See {@link #getExecutionDate()}
     */
    public void setExecutionDate(Calendar executionDate)
    {
        this.executionDate = executionDate;
    }

    /**
     * Time at which the job execution request (the {@link JobInstance}) was taken by an engine.
     */
    public Calendar getAttributionDate()
    {
        return attributionDate;
    }

    /**
     * See {@link #getAttributionDate()}
     */
    public void setAttributionDate(Calendar attributionDate)
    {
        this.attributionDate = attributionDate;
    }

    /**
     * True if this job instance was created from a schedule.
     */
    public boolean isFromSchedule()
    {
        return this.fromSchedule;
    }

    /**
     * See priority handling doc.
     */
    public Integer getPriority()
    {
        return this.priority;
    }

    /**
     * An instruction given to the job instance.
     *
     * @return
     */
    public Instruction getInstruction()
    {
        return this.instruction;
    }

    public Calendar getNotBefore()
    {
        return notBefore;
    }

    public void setNotBefore(Calendar notBefore)
    {
        this.notBefore = notBefore;
    }

    public Map<String, String> getPrms()
    {
        if (this.prmCache == null)
        {
            throw new IllegalStateException("cache was not loaded");
        }
        return this.prmCache;
    }

    public void loadPrmCache(DbConn cnx)
    {
        prmCache = new HashMap<>();
        for (Map.Entry<String, String> jp : RuntimeParameter.select_map(cnx, "jiprm_select_by_ji", this.id).entrySet())
        {
            prmCache.put(jp.getKey(), jp.getValue());
        }
    }

    public void addEnvVar(String key, String value)
    {
        if (envVarCache == null)
        {
            envVarCache = new HashMap<>(1);
        }
        envVarCache.put(key, value);
    }

    public Map<String, String> getEnvVarCache()
    {
        return envVarCache == null ? new HashMap<>() : envVarCache;
    }

    public static List<JobInstance> select(DbConn cnx, String query_key, Object... args)
    {
        List<JobInstance> res = new ArrayList<>();
        try (ResultSet rs = cnx.runSelect(query_key, args))
        {
            while (rs.next())
            {
                JobInstance tmp = new JobInstance();

                tmp.id = rs.getLong(1);

                tmp.attributionDate = cnx.getCal(rs, 2);
                tmp.creationDate = cnx.getCal(rs, 3);
                tmp.email = rs.getString(4);
                tmp.executionDate = cnx.getCal(rs, 5);
                tmp.instanceApplication = rs.getString(6);
                tmp.instanceKeyword1 = rs.getString(7);
                tmp.instanceKeyword2 = rs.getString(8);
                tmp.instanceKeyword3 = rs.getString(9);
                tmp.instanceModule = rs.getString(10);
                tmp.internalPosition = rs.getDouble(11);
                tmp.parentId = rs.getLong(12);
                tmp.progress = rs.getInt(13);
                tmp.sessionID = rs.getString(14);
                tmp.state = State.valueOf(rs.getString(15));
                tmp.userName = rs.getString(16);
                tmp.jd_id = rs.getLong(17);
                tmp.node_id = rs.getLong(18);
                tmp.queue_id = rs.getLong(19);
                tmp.highlander = rs.getBoolean(20);
                tmp.fromSchedule = rs.getBoolean(21);
                tmp.priority = rs.getInt(22);
                tmp.instruction = Instruction.valueOf(rs.getString(23));
                tmp.notBefore = cnx.getCal(rs, 24);

                tmp.q = Queue.map(rs, 24);
                tmp.jd = JobDef.map(rs, 28);
                tmp.n = Node.map(cnx, rs, 47);

                res.add(tmp);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException(e);
        }
        return res;
    }

    public static JobInstance select_id(DbConn cnx, long id)
    {
        List<JobInstance> res = select(cnx, "ji_select_by_id", id);
        if (res.isEmpty())
        {
            throw new DatabaseException("no result for query by ID for ID " + id);
        }
        if (res.size() > 1)
        {
            throw new DatabaseException("Inconsistent database! Multiple results for query by ID for ID " + id);
        }

        return res.get(0);
    }

    public static void delete_id(DbConn cnx, long id)
    {
        QueryResult res = cnx.runUpdate("ji_delete_by_id", id);
        if (res.nbUpdated != 1)
        {
            throw new DatabaseException("Delete failed: row does not exist");
        }
    }

    public static long enqueue(DbConn cnx, State status, long queue_id, long job_id, String application, Long parentId, String module,
                               String keyword1, String keyword2, String keyword3, String sessionId, String userName, String email, boolean highlander,
                               boolean fromSchedule, Calendar notBefore, int priority, Instruction instruction, Map<String, String> prms)
    {
        QueryResult qr = cnx.runUpdate("ji_insert_enqueue", email, application, keyword1, keyword2, keyword3, module, parentId, sessionId,
                status, userName, job_id, queue_id, highlander, fromSchedule, notBefore, priority, instruction);

        long newId = qr.getGeneratedId();

        if (prms != null)
        {
            for (Map.Entry<String, String> prm : prms.entrySet())
            {
                RuntimeParameter.create(cnx, newId, prm.getKey(), prm.getValue());
            }
        }

        return newId;
    }
}
