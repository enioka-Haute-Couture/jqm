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

package com.enioka.jqm.api;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.naming.NameNotFoundException;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.api.Query.Sort;
import com.enioka.jqm.api.Query.SortSpec;
import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.NonUniqueResultException;
import com.enioka.jqm.jdbc.QueryResult;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.RuntimeParameter;
import com.enioka.jqm.jpamodel.State;

/**
 * Main JQM client API entry point.
 */
final class HibernateClient implements JqmClient
{
    private static Logger jqmlogger = LoggerFactory.getLogger(HibernateClient.class);
    private static final String PERSISTENCE_UNIT = "jobqueue-api-pu";
    private static final int IN_CLAUSE_LIMIT = 500;
    private Db db = null;
    private String protocol = null;
    Properties p;

    // /////////////////////////////////////////////////////////////////////
    // Construction/Connection
    // /////////////////////////////////////////////////////////////////////

    // No public constructor. MUST use factory.
    HibernateClient(Properties p)
    {
        this.p = p;
        if (p.containsKey("emf"))
        {
            jqmlogger.trace("emf present in properties");
            db = (Db) p.get("emf");
        }
    }

    private Db createFactory()
    {
        jqmlogger.debug("Creating connection factory to database");

        InputStream fis = null;
        try
        {
            fis = this.getClass().getClassLoader().getResourceAsStream("META-INF/jqm.properties");
            if (fis == null)
            {
                jqmlogger.trace("No jqm.properties file found.");
            }
            else
            {
                p.load(fis);
                jqmlogger.trace("A jqm.properties file was found");
            }
        }
        catch (IOException e)
        {
            // We allow no configuration files, but not an unreadable configuration file.
            throw new JqmClientException("META-INF/jqm.properties file is invalid", e);
        }
        finally
        {
            closeQuietly(fis);
        }

        Db newDb = null;
        if (p.containsKey("javax.persistence.nonJtaDataSource"))
        {
            // This is a hack. Some containers will use root context as default for JNDI (WebSphere, Glassfish...), other will use
            // java:/comp/env/ (Tomcat...). So if we actually know the required alias, we try both, and the user only has to provide a
            // root JNDI alias that will work in both cases.
            String dsAlias = (String) p.get("javax.persistence.nonJtaDataSource");
            try
            {
                newDb = new Db(dsAlias);
            }
            catch (DatabaseException e)
            {
                if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof NameNotFoundException)
                {
                    jqmlogger.debug("JNDI alias " + p.getProperty("javax.persistence.nonJtaDataSource")
                            + " was not found. Trying with java:/comp/env/ prefix");
                    dsAlias = "java:/comp/env/" + dsAlias;
                    newDb = new Db(dsAlias);
                }
                else
                {
                    throw e;
                }
            }
        }
        else
        {
            newDb = new Db(); // use default alias.
        }

        // Do a stupid query to force initialization
        DbConn c = newDb.getConn();
        c.runSelect("node_select_by_id", -1);
        c.close();

        return newDb;
    }

    DbConn getDbSession()
    {
        if (db == null)
        {
            db = createFactory();
        }

        try
        {
            return db.getConn();
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not create database session.", e);
            throw new JqmClientException("Could not create database session", e);
        }
    }

    private void closeQuietly(Closeable closeable)
    {
        try
        {
            if (closeable != null)
            {
                closeable.close();
            }
        }
        catch (IOException ioe)
        {
            // ignore
        }
    }

    @Override
    public void dispose()
    {
        SimpleApiSecurity.dispose();
        this.db = null;
        p = null;
    }

    // /////////////////////////////////////////////////////////////////////
    // Enqueue functions
    // /////////////////////////////////////////////////////////////////////

    @Override
    public int enqueue(JobRequest runRequest)
    {
        jqmlogger.trace("BEGINING ENQUEUE - request is for application name " + runRequest.getApplicationName());
        DbConn cnx = getDbSession();

        // First, get the JobDef.
        JobDef jobDef = null;
        List<JobDef> jj = JobDef.select(cnx, "jd_select_by_key", runRequest.getApplicationName());
        if (jj.size() == 0)
        {
            jqmlogger.error("Job definition named " + runRequest.getApplicationName() + " does not exist");
            closeQuietly(cnx);
            throw new JqmInvalidRequestException("no job definition named " + runRequest.getApplicationName());
        }
        if (jj.size() > 1)
        {
            jqmlogger.error("There are multiple Job definition named " + runRequest.getApplicationName() + ". Inconsistent configuration.");
            closeQuietly(cnx);
            throw new JqmInvalidRequestException("There are multiple Job definition named " + runRequest.getApplicationName());
        }
        jobDef = jj.get(0);
        jqmlogger.trace("Job to enqueue is from JobDef " + jobDef.getId());

        // Then check Highlander.
        Integer existing = highlanderMode(jobDef, cnx);
        if (existing != null)
        {
            closeQuietly(cnx);
            jqmlogger.trace("JI won't actually be enqueued because a job in highlander mode is currently submitted: " + existing);
            return existing;
        }

        // If here, need to enqueue a new execution request.
        jqmlogger.trace("Not in highlander mode or no currently enqueued instance");

        // Parameters are both from the JobDef and the execution request.
        Map<String, String> prms = RuntimeParameter.select_map(cnx, "jdprm_select_all_for_jd", jobDef.getId());
        prms.putAll(runRequest.getParameters());

        // On which queue?
        Integer queue_id;
        if (runRequest.getQueueName() != null)
        {
            // use requested key if given.
            queue_id = cnx.runSelectSingle("q_select_by_key", 1, Integer.class, runRequest.getQueueName());
        }
        else
        {
            // use default queue otherwise
            queue_id = jobDef.getQueue();
        }

        // Now create the JI
        try
        {
            int id = JobInstance.enqueue(cnx, queue_id, jobDef.getId(), runRequest.getApplication(), runRequest.getParentID(),
                    runRequest.getModule(), runRequest.getKeyword1(), runRequest.getKeyword2(), runRequest.getKeyword3(),
                    runRequest.getSessionID(), runRequest.getUser(), runRequest.getEmail(), jobDef.isHighlander(), prms);

            jqmlogger.trace("JI just created: " + id);
            cnx.commit();
            return id;
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("An entity specified in the execution request does not exist", e);
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not create new JobInstance", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    @Override
    public int enqueue(String applicationName, String userName)
    {
        return enqueue(new JobRequest(applicationName, userName));
    }

    @Override
    public int enqueueFromHistory(int jobIdToCopy)
    {
        DbConn cnx = null;
        try
        {
            cnx = getDbSession();
            return enqueue(getJobRequest(jobIdToCopy, cnx));
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("No job for this ID in the history");
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    // Helper. Current transaction is committed in some cases.
    private Integer highlanderMode(JobDef jd, DbConn cnx)
    {
        if (!jd.isHighlander())
        {
            return null;
        }

        try
        {
            Integer existing = cnx.runSelectSingle("ji_select_existing_highlander", Integer.class, jd.getId());
            return existing;
        }
        catch (NoResultException ex)
        {
            // Just continue, this means no existing waiting JI in queue.
        }

        // Now we need to actually synchronize through the database to avoid double posting
        // TODO: use a dedicated table, not the JobDef one. Will avoid locking the configuration.
        ResultSet rs = cnx.runSelect(true, "jd_select_by_id", jd.getId());

        // Now we have a lock, just retry - some other client may have created a job instance recently.
        try
        {
            Integer existing = cnx.runSelectSingle("ji_select_existing_highlander", Integer.class, jd.getId());
            rs.close();
            cnx.commit(); // Do not keep the lock!
            return existing;
        }
        catch (NoResultException ex)
        {
            // Just continue, this means no existing waiting JI in queue.
        }
        catch (SQLException e)
        {
            // Who cares.
            jqmlogger.warn("Issue when closing a ResultSet. Transaction or session leak is possible.", e);
        }

        jqmlogger.trace("Highlander mode analysis is done: nor existing JO, must create a new one. Lock is hold.");
        return null;
    }

    /**
     * Internal helper to create a new execution request from an History row.<br>
     * To be called for a single row only, not for converting multiple History elements.<br>
     * Does not create a transaction, and no need for an active transaction.
     * 
     * @param launchId
     *            the ID of the launch (was the ID of the JI, now the ID of the History object)
     * @param cnx
     *            an open DB session
     * @return a new execution request
     */
    private JobRequest getJobRequest(int launchId, DbConn cnx)
    {
        Map<String, String> prms = RuntimeParameter.select_map(cnx, "jiprm_select_by_jd", launchId);
        ResultSet rs = cnx.runSelect("history_select_reenqueue_by_id", launchId);
        JobRequest jd = new JobRequest();

        try
        {
            jd.setApplication(rs.getString(1));
            jd.setApplicationName(rs.getString(2));
            jd.setEmail(rs.getString(3));
            jd.setKeyword1(rs.getString(4));
            jd.setKeyword2(rs.getString(5));
            jd.setKeyword3(rs.getString(6));
            jd.setModule(rs.getString(7));
            jd.setParentID(rs.getInt(8));
            jd.setSessionID(rs.getString(9));
            jd.setUser(rs.getString(10));

            for (Map.Entry<String, String> p : prms.entrySet())
            {
                jd.addParameter(p.getKey(), p.getValue());
            }
        }
        catch (SQLException e)
        {
            throw new JqmClientException("Could not extract History data for launch " + launchId, e);
        }

        return jd;
    }

    // /////////////////////////////////////////////////////////////////////
    // Job destruction
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void cancelJob(int idJob)
    {
        jqmlogger.trace("Job instance number " + idJob + " will be cancelled");
        DbConn cnx = null;
        try
        {
            cnx = getDbSession();
            QueryResult res = cnx.runUpdate("jj_update_cancel_by_id", idJob);
            if (res.nbUpdated != 1)
            {
                throw new JqmClientException("the job is already running, has already finished or never existed to begin with");
            }
        }
        catch (RuntimeException e)
        {
            closeQuietly(cnx);
            throw e;
        }

        try
        {
            History.create(cnx, idJob, State.CANCELLED, null);
            JobInstance.delete_id(cnx, idJob);
            cnx.commit();
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not historise the job instance after it was cancelled", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    @Override
    public void deleteJob(int idJob)
    {
        jqmlogger.trace("Job instance number " + idJob + " will be deleted");
        DbConn cnx = null;

        try
        {
            cnx = getDbSession();

            // Two transactions against deadlock.
            QueryResult res = cnx.runUpdate("jj_update_cancel_by_id", idJob);
            cnx.commit();
            if (res.nbUpdated != 1)
            {
                new JqmInvalidRequestException(
                        "An attempt was made to delete a job instance that either did not exist or was already running");
            }

            cnx.runUpdate("jiprm_delete_by_ji", idJob);
            cnx.runUpdate("message_delete_by_ji", idJob);
            JobInstance.delete_id(cnx, idJob);
            cnx.commit();
        }
        catch (JqmClientException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not delete a job (internal error)", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    @Override
    public void killJob(int idJob)
    {
        jqmlogger.trace("Job instance number " + idJob + " will be killed (if possible)o");

        // First try to cancel the JI (works if it is not already running)
        try
        {
            cancelJob(idJob);
            return;
        }
        catch (JqmClientException e)
        {
            // Nothing to do - this is thrown if already running. Just go on, this is a standard kill.
        }

        DbConn cnx = null;
        try
        {
            cnx = getDbSession();

            QueryResult res = cnx.runUpdate("jj_update_kill_by_id", idJob);
            if (res.nbUpdated != 1)
            {
                throw new JqmInvalidRequestException("Job instance does not exist or has already finished");
            }

            Message.create(cnx, "Kill attempt on the job", idJob);
            cnx.commit();
        }
        catch (JqmClientException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not kill a job (internal error)", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Job Pause/restart
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void pauseQueuedJob(int idJob)
    {
        jqmlogger.trace("Job instance number " + idJob + " status will be set to HOLDED");
        DbConn cnx = null;

        try
        {
            cnx = getDbSession();
            QueryResult qr = cnx.runUpdate("jj_update_pause_by_id", idJob);
            if (qr.nbUpdated != 1)
            {
                throw new JqmInvalidRequestException(
                        "An attempt was made to pause a job instance that did not exist or was already running.");
            }
            cnx.commit();
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not pause a job (internal error)", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    @Override
    public void resumeJob(int idJob)
    {
        jqmlogger.trace("Job status number " + idJob + " will be resumed");
        DbConn cnx = null;

        try
        {
            cnx = getDbSession();
            QueryResult qr = cnx.runUpdate("jj_update_resume_by_id", idJob);
            if (qr.nbUpdated != 1)
            {
                throw new JqmInvalidRequestException("An attempt was made to pause a job instance that did not exist or was not paused.");
            }
            cnx.commit();
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not resume a job (internal error)", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    public int restartCrashedJob(int idJob)
    {
        DbConn cnx = null;

        // History and Job ID have the same ID.
        try
        {
            cnx = getDbSession();
            ResultSet rs = cnx.runSelect("history_select_reenqueue_by_id", idJob);

            if (!rs.next())
            {
                throw new JqmClientException("You cannot restart a job that is not done or which was purged from history");
            }

            JobRequest jr = new JobRequest();
            jr.setApplication(rs.getString(1));
            jr.setApplicationName(rs.getString(2));
            jr.setEmail(rs.getString(3));
            jr.setKeyword1(rs.getString(4));
            jr.setKeyword2(rs.getString(5));
            jr.setKeyword3(rs.getString(6));
            jr.setModule(rs.getString(7));
            jr.setParentID(rs.getInt(8));
            jr.setSessionID(rs.getString(9));
            jr.setUser(rs.getString(10));

            State s = State.valueOf(rs.getString(11));
            if (!s.equals(State.CRASHED))
            {
                throw new JqmClientException("You cannot restart a job that has not crashed");
            }

            int res = enqueue(jr);
            cnx.runUpdate("history_delete_by_id", idJob);
            cnx.commit();
            return res;
        }
        catch (JqmClientException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not restart a job (internal error)", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Misc.
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void setJobQueue(int idJob, int idQueue)
    {
        DbConn cnx = null;

        try
        {
            cnx = getDbSession();
            QueryResult qr = cnx.runUpdate("jj_update_queue_by_id", idQueue, idJob);

            if (qr.nbUpdated != 1)
            {
                throw new JqmClientException("Job instance does not exist or has already started");
            }

        }
        catch (DatabaseException e)
        {
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException)
                throw new JqmClientException("Queue does not exist", e);
            else
                throw new JqmClientException("could not change the queue of a job (internal error)", e);
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not change the queue of a job (internal error)", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    @Override
    public void setJobQueue(int idJob, com.enioka.jqm.api.Queue queue)
    {
        setJobQueue(idJob, queue.getId());
    }

    @Override
    public void setJobQueuePosition(int idJob, int position)
    {
        DbConn cnx = null;
        try
        {
            // Step 1 : get the queue of this job.
            cnx = getDbSession();
            ResultSet rs1 = cnx.runSelect("ji_select_changequeuepos_by_id", idJob);
            if (!rs1.next())
            {
                throw new JqmInvalidRequestException("Job does not exist or is already running.");
            }
            int queue_id = rs1.getInt(1);
            int internal_id = rs1.getInt(2);
            rs1.close();

            // Step 2 : get the current rank of the JI.
            int current = cnx.runSelectSingle("ji_select_current_pos", Integer.class, internal_id, queue_id);

            // Step 3 : select target position
            int betweenUp = 0;
            int betweenDown = 0;

            if (current == position)
            {
                // Nothing to do
                return;
            }
            else if (current < position)
            {
                betweenDown = position;
                betweenUp = position + 1;
            }
            else
            {
                betweenDown = position - 1;
                betweenUp = position;
            }

            // Step 4 : update the JI.
            List<JobInstance> currentJobs = JobInstance.select(cnx, "ji_select_by_queue", queue_id);
            if (currentJobs.isEmpty())
            {
                cnx.runUpdate("jj_update_rank_by_id", 0, idJob);
            }
            else if (currentJobs.size() < betweenUp)
            {
                cnx.runUpdate("jj_update_rank_by_id", currentJobs.get(currentJobs.size() - 1).getInternalPosition() + 0.00001, idJob);
            }
            else
            {
                // Normal case: put the JI between the two others.
                QueryResult qr = cnx.runUpdate("jj_update_rank_by_id",
                        (currentJobs.get(betweenUp - 1).getInternalPosition() + currentJobs.get(betweenDown - 1).getInternalPosition()) / 2,
                        idJob);
                if (qr.nbUpdated != 1)
                {
                    throw new JqmInvalidRequestException("Job is already running.");
                }
            }
            cnx.commit();
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not change the queue position of a job (internal error)", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Job queries
    // /////////////////////////////////////////////////////////////////////

    private String getStringPredicate(String fieldName, String filterValue, List<Object> prms)
    {
        if (filterValue == null)
        {
            return "";
        }
        return getStringPredicate(fieldName, Arrays.asList(filterValue), prms);
    }

    // GetJob helper - String predicates are all created the same way, so this factors some code.
    private String getStringPredicate(String fieldName, List<String> filterValues, List<Object> prms)
    {
        if (filterValues != null && !filterValues.isEmpty())
        {
            String res = "";
            for (String filterValue : filterValues)
            {
                if (filterValue == null)
                {
                    continue;
                }
                if (!filterValue.isEmpty())
                {
                    String prmName = fieldName.split("\\.")[fieldName.split("\\.").length - 1] + System.identityHashCode(filterValue);
                    prms.add(filterValue);
                    if (filterValue.contains("%"))
                    {
                        res += String.format("(%s LIKE ?) OR ", fieldName);
                    }
                    else
                    {
                        res += String.format("(%s = ?) OR ", fieldName);
                    }
                }
                else
                {
                    res += String.format("(%s IS NULL OR %s = '') OR ", fieldName, fieldName);
                }
            }
            if (!res.isEmpty())
            {
                res = "AND (" + res.substring(0, res.length() - 4) + ") ";
                return res;
            }
        }
        return "";
    }

    private String getIntPredicate(String fieldName, Integer filterValue, List<Object> prms)
    {
        if (filterValue != null)
        {
            if (filterValue != -1)
            {
                String prmName = fieldName.split("\\.")[fieldName.split("\\.").length - 1];
                prms.add(filterValue);
                return String.format("AND (%s = ?) ", fieldName);
            }
            else
            {
                return String.format("AND (%s IS NULL) ", fieldName);
            }
        }
        return "";
    }

    private String getCalendarPredicate(String fieldName, Calendar filterValue, String comparison, List<Object> prms)
    {
        if (filterValue != null)
        {
            String prmName = fieldName.split("\\.")[fieldName.split("\\.").length - 1] + Math.abs(comparison.hashCode());
            prms.add(filterValue);
            return String.format("AND (%s %s ?) ", fieldName, comparison);
        }
        else
        {
            return "";
        }
    }

    private String getStatusPredicate(String fieldName, List<com.enioka.jqm.api.State> status, List<Object> prms)
    {
        if (status == null || status.isEmpty())
        {
            return "";
        }

        String res = String.format("AND ( %s IN ( ", fieldName);

        for (com.enioka.jqm.api.State s : status)
        {
            String prmName = "status" + s.hashCode();
            res += " :" + prmName + ",";
            prms.add(State.valueOf(s.toString()));
        }
        res = res.substring(0, res.length() - 1) + ")) ";
        return res;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getJobs(Query query)
    {
        if ((query.getFirstRow() != null || query.getPageSize() != null) && query.isQueryLiveInstances() && query.isQueryHistoryInstances())
        {
            throw new JqmInvalidRequestException("cannot use paging when querying both live and historical instances");
        }
        if (query.isQueryLiveInstances() && query.isQueryHistoryInstances() && query.getSorts().size() > 0)
        {
            throw new JqmInvalidRequestException("cannot use sorting when querying both live and historical instances");
        }
        if (!query.isQueryHistoryInstances() && !query.isQueryLiveInstances())
        {
            throw new JqmInvalidRequestException(
                    "cannot query nothing - either query live instances, historical instances or both, but not nothing");
        }

        DbConn cnx = null;
        try
        {
            cnx = getDbSession();
            Map<Integer, com.enioka.jqm.api.JobInstance> res = new HashMap<Integer, com.enioka.jqm.api.JobInstance>();

            String wh = "";
            List<Object> prms = new ArrayList<Object>();

            String q = "";
            String filterCountQuery = "SELECT ";
            String totalCountQuery = "SELECT ";

            // ////////////////////////////////////////
            // Job Instance query
            if (query.isQueryLiveInstances())
            {
                // WHERE
                wh += getIntPredicate("ji.ID", query.getJobInstanceId(), prms);
                wh += getIntPredicate("ji.PARENTID", query.getParentId(), prms);
                wh += getStringPredicate("ji.APPLICATION", query.getInstanceApplication(), prms);
                wh += getStringPredicate("ji.MODULE", query.getInstanceModule(), prms);
                wh += getStringPredicate("ji.KEYWORD1", query.getInstanceKeyword1(), prms);
                wh += getStringPredicate("ji.KEYWORD2", query.getInstanceKeyword2(), prms);
                wh += getStringPredicate("ji.KEYWORD3", query.getInstanceKeyword3(), prms);
                wh += getStringPredicate("ji.USERNAME", query.getUser(), prms);
                wh += getStringPredicate("ji.SESSIONID", query.getSessionId(), prms);
                wh += getStatusPredicate("ji.STATE", query.getStatus(), prms);

                wh += getStringPredicate("jd.APPLICATIONNAME", query.getInstanceApplication(), prms);
                wh += getStringPredicate("jd.APPLICATION", query.getJobDefApplication(), prms);
                wh += getStringPredicate("jd.MODULE", query.getJobDefModule(), prms);
                wh += getStringPredicate("jd.KEYWORD1", query.getJobDefKeyword1(), prms);
                wh += getStringPredicate("jd.KEYWORD2", query.getJobDefKeyword2(), prms);
                wh += getStringPredicate("jd.KEYWORD3", query.getJobDefKeyword3(), prms);

                wh += getStringPredicate("n.NODENAME", query.getNodeName(), prms);

                wh += getStringPredicate("q.NAME", query.getQueueName(), prms);
                wh += getIntPredicate("q.ID", query.getQueueId() == null ? null : query.getQueueId(), prms);

                wh += getCalendarPredicate("ji.CREATIONDATE", query.getEnqueuedAfter(), ">=", prms);
                wh += getCalendarPredicate("ji.CREATIONDATE", query.getEnqueuedBefore(), "<=", prms);
                wh += getCalendarPredicate("ji.EXECUTIONDATE", query.getBeganRunningAfter(), ">=", prms);
                wh += getCalendarPredicate("ji.EXECUTIONDATE", query.getBeganRunningBefore(), "<=", prms);

                q = "SELECT ji.ID, jd.APPLICATION AS JD_APPLICATION, jd.APPLICATIONNAME AS APPLICATION_NAME, ji.ATTRIBUTIONDATE, "
                        + "ji.SENDEMAIL AS EMAIL, NULL AS END_DATE, ji.CREATIONDATE AS ENQUEUE_DATE, ji.EXECUTIONDATE AS EXECUTION_DATE, "
                        + "ji.HIGHLANDER, ji.APPLICATION AS INSTANCE_APPLICATION, ji.KEYWORD1 AS INSTANCE_KEYWORD1, "
                        + "ji.KEYWORD2 AS INSTANCE_KEYWORD2, ji.KEYWORD3 AS INSTANCE_KEYWORD3, ji.MODULE AS INSTANCE_MODULE, "
                        + "jd.KEYWORD1 AS JD_KEYWORD1, jd.KEYWORD2 AS JD_KEYWORD2, jd.KEYWORD3 AS JD_KEYWORD3, jd.MODULE AS JD_MODULE,"
                        + "n.NODENAME AS NODENAME,ji.PARENTID AS PARENT_JOB_ID, ji.PROGRESS, q.NAME AS QUEUE_NAME, NULL AS RETURN_CODE,"
                        + "ji.SESSIONID AS SESSION_ID, ji.STATE AS STATUS, ji.USERNAME, ji.JD_ID, ji.NODE_ID, ji.QUEUE_ID, ji.INTERNALPOSITION AS POSITION "
                        + "FROM JOBINSTANCE ji LEFT JOIN QUEUE q ON ji.QUEUE_ID=q.ID LEFT JOIN JOBDEF jd ON ji.JD_ID=jd.ID LEFT JOIN NODE n ON ji.NODE_ID=n.ID ";

                filterCountQuery += " (SELECT COUNT(1) FROM JOBINSTANCE %s) ,";
                totalCountQuery += " (SELECT COUNT(1) FROM JOBINSTANCE) ,";

                if (wh.length() > 3)
                {
                    q += wh;
                    filterCountQuery = String.format(filterCountQuery, wh);
                }
            }

            /////////////////////////////////////
            // HISTORY QUERY
            if (query.isQueryHistoryInstances())
            {
                if (q.length() > 3)
                {
                    q += " UNION ALL ";
                }
                wh = "";

                wh += getIntPredicate("ID", query.getJobInstanceId(), prms);
                wh += getIntPredicate("PARENT_JOB_ID", query.getParentId(), prms);
                wh += getStringPredicate("INSTANCE_APPLICATION", query.getInstanceApplication(), prms);
                wh += getStringPredicate("INSTANCE_MODULE", query.getInstanceModule(), prms);
                wh += getStringPredicate("INSTANCE_KEYWORD1", query.getInstanceKeyword1(), prms);
                wh += getStringPredicate("INSTANCE_KEYWORD2", query.getInstanceKeyword2(), prms);
                wh += getStringPredicate("INSTANCE_KEYWORD3", query.getInstanceKeyword3(), prms);
                wh += getStringPredicate("USERNAME", query.getUser(), prms);
                wh += getStringPredicate("SESSION_ID", query.getSessionId(), prms);
                wh += getStatusPredicate("STATUS", query.getStatus(), prms);

                wh += getStringPredicate("APPLICATIONNAME", query.getInstanceApplication(), prms);
                wh += getStringPredicate("JD_APPLICATION", query.getJobDefApplication(), prms);
                wh += getStringPredicate("JD_MODULE", query.getJobDefModule(), prms);
                wh += getStringPredicate("JD_KEYWORD1", query.getJobDefKeyword1(), prms);
                wh += getStringPredicate("JD_KEYWORD2", query.getJobDefKeyword2(), prms);
                wh += getStringPredicate("JD_KEYWORD3", query.getJobDefKeyword3(), prms);

                wh += getStringPredicate("NODENAME", query.getNodeName(), prms);

                wh += getStringPredicate("QUEUE_NAME", query.getQueueName(), prms);
                wh += getIntPredicate("QUEUE_ID", query.getQueueId() == null ? null : query.getQueueId(), prms);

                wh += getCalendarPredicate("ENQUEUE_DATE", query.getEnqueuedAfter(), ">=", prms);
                wh += getCalendarPredicate("ENQUEUE_DATE", query.getEnqueuedBefore(), "<=", prms);
                wh += getCalendarPredicate("EXECUTION_DATE", query.getBeganRunningAfter(), ">=", prms);
                wh += getCalendarPredicate("EXECUTION_DATE", query.getBeganRunningBefore(), "<=", prms);
                wh += getCalendarPredicate("END_DATE", query.getEndedAfter(), ">=", prms);
                wh += getCalendarPredicate("END_DATE", query.getEndedBefore(), "<=", prms);

                q += "SELECT ID, APPLICATION AS JD_APPLICATION, APPLICATIONNAME, ATTRIBUTIONDATE, EMAIL, "
                        + "END_DATE, ENQUEUE_DATE, EXECUTION_DATE, HIGHLANDER, INSTANCE_APPLICATION, "
                        + "INSTANCE_KEYWORD1, INSTANCE_KEYWORD2, INSTANCE_KEYWORD3, INSTANCE_MODULE, "
                        + "KEYWORD1 AS JD_KEYWORD1, KEYWORD2 AS JD_KEYWORD2, KEYWORD3 AS JD_KEYWORD3, "
                        + "MODULE AS JD_MODULE, NODENAME, PARENT_JOB_ID, PROGRESS, QUEUE_NAME, "
                        + "RETURN_CODE, SESSION_ID, STATUS, USERNAME, JOBDEF_ID as JD_ID, NODE_ID, QUEUE_ID, 0 as POSITION FROM HISTORY ";

                filterCountQuery += " (SELECT COUNT(1) FROM HISTORY %s) ,";
                totalCountQuery += " (SELECT COUNT(1) FROM HISTORY) ,";

                if (wh.length() > 3)
                {
                    q += wh;
                }
            }

            ///////////////////////////////////////////////
            // Sort (on the union, not the sub queries)
            String sort = "";
            for (SortSpec s : query.getSorts())
            {
                sort += s.col.getJiField() == null ? ""
                        : "," + s.col.getHistoryField() + " " + (s.order == Query.SortOrder.ASCENDING ? "ASC" : "DESC");
            }
            if (sort.isEmpty())
            {
                sort = " ORDER BY h.id";
            }
            else
            {
                sort = " ORDER BY " + sort.substring(1);
            }

            ///////////////////////////////////////////////
            // Set pagination parameters
            // TODO: save this.
            // if (query.getFirstRow() != null)
            // {
            // q2.setFirstResult(query.getFirstRow());
            // }
            // if (query.getPageSize() != null)
            // {
            // q2.setMaxResults(query.getPageSize());
            // }

            ///////////////////////////////////////////////
            // Run the query
            ResultSet rs = cnx.runRawSelect(q, prms);
            while (rs.next())
            {
                com.enioka.jqm.api.JobInstance tmp = getJob(rs);
                res.put(tmp.getId(), tmp);
            }
            rs.close();

            // If needed, fetch the total result count (without pagination). Note that without pagination, the Query object does not
            // need this indication.
            if (query.getFirstRow() != null || (query.getPageSize() != null && res.size() >= query.getPageSize()))
            {
                ResultSet rs2 = cnx.runRawSelect(filterCountQuery, prms);
                rs2.next();

                query.setResultSize(rs2.getInt(0));
            }

            ///////////////////////////////////////////////
            // Fetch messages and parameters in batch

            // Optimization: fetch messages and parameters in batches of 50 (limit accepted by most databases for IN clauses).
            List<List<Integer>> ids = new ArrayList<List<Integer>>();
            List<Integer> currentList = null;
            int i = 0;
            for (com.enioka.jqm.api.JobInstance ji : res.values())
            {
                if (currentList == null || i % IN_CLAUSE_LIMIT == 0)
                {
                    currentList = new ArrayList<Integer>(IN_CLAUSE_LIMIT);
                    ids.add(currentList);
                }
                currentList.add(ji.getId());
                i++;
            }
            if (currentList != null && !currentList.isEmpty())
            {
                List<RuntimeParameter> rps = new ArrayList<RuntimeParameter>(IN_CLAUSE_LIMIT * ids.size());
                List<Message> msgs = new ArrayList<Message>(IN_CLAUSE_LIMIT * ids.size());

                for (List<Integer> idsBatch : ids)
                {
                    ResultSet run = cnx.runSelect("jiprm_select_by_ji_list", idsBatch);
                    while (run.next())
                    {
                        res.get(run.getInt(2)).getParameters().put(run.getString(3), run.getString(4));
                    }
                    run.close();

                    ResultSet msg = cnx.runSelect("message_select_by_ji_list", idsBatch);
                    while (msg.next())
                    {
                        res.get(run.getInt(2)).getMessages().add(run.getString(3));
                    }
                    run.close();
                }
            }

            ///////////////////////////////////////////////
            // DONE AT LAST
            query.setResults(new ArrayList<com.enioka.jqm.api.JobInstance>(res.values()));
            return query.getResults();
        }
        catch (

        Exception e)
        {
            throw new JqmClientException("an error occured during query execution", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    private com.enioka.jqm.api.JobInstance getJob(ResultSet rs) throws SQLException
    {
        com.enioka.jqm.api.JobInstance res = new com.enioka.jqm.api.JobInstance();

        res.setId(rs.getInt(1));
        // res.setApplication(rs.getString(2));
        res.setApplicationName(rs.getString(3));
        res.setEmail(rs.getString(5));
        res.setEndDate(getCal(rs, 6));
        res.setEnqueueDate(getCal(rs, 7));
        res.setBeganRunningDate(getCal(rs, 8));
        res.setApplication(rs.getString(10));
        res.setKeyword1(rs.getString(11));
        res.setKeyword2(rs.getString(12));
        res.setKeyword3(rs.getString(13));
        res.setModule(rs.getString(14));
        res.setDefinitionKeyword1(rs.getString(15));
        res.setDefinitionKeyword2(rs.getString(16));
        res.setDefinitionKeyword3(rs.getString(17));
        res.setNodeName(rs.getString(19));
        res.setParent(rs.getInt(20));
        res.setProgress(rs.getInt(21));
        res.setQueueName(rs.getString(22));
        res.setSessionID(rs.getString(24));
        res.setState(com.enioka.jqm.api.State.valueOf(rs.getString(25)));
        res.setUser(rs.getString(26));

        com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();
        q.setId(rs.getInt(29));
        q.setName(rs.getString(22));
        res.setQueue(q);

        res.setPosition(rs.getInt(30));

        return res;
    }

    private Calendar getCal(ResultSet rs, int colIdx) throws SQLException
    {
        Calendar c = null;
        if (rs.getTimestamp(colIdx) != null)
        {
            c = Calendar.getInstance();
            c.setTimeInMillis(rs.getTimestamp(colIdx).getTime());
        }
        return c;
    }

    @Override
    public com.enioka.jqm.api.JobInstance getJob(int idJob)
    {
        // TODO: direct queries following previous logic, but after we have common table structures.
        return Query.create().setJobInstanceId(idJob).setQueryHistoryInstances(true).setQueryLiveInstances(true).run().get(0);

        // DbConn cnx = null;
        // try
        // {
        // // Three steps: first, query History as:
        // // * this is supposed to be the most frequent query.
        // // * we try to avoid hitting the queues if possible
        // // Second, query live queues
        // // Third, query history again (because a JI may have ended between the first two queries, so we may miss a JI)
        // // Outside this case, this third query will be very rare, as the method is always called with an ID that cannot be
        // // guessed as its only parameter, so the existence of the JI is nearly always a given.
        // cnx = getDbSession();
        // History h = em.find(History.class, idJob);
        // com.enioka.jqm.api.JobInstance res = null;
        // if (h != null)
        // {
        // res = getJob(h, em);
        // }
        // else
        // {
        // JobInstance ji = em.find(JobInstance.class, idJob);
        // if (ji != null)
        // {
        // res = getJob(ji, em);
        // }
        // else
        // {
        // h = em.find(History.class, idJob);
        // if (h != null)
        // {
        // res = getJob(h, em);
        // }
        // else
        // {
        // throw new JqmInvalidRequestException("No job instance of ID " + idJob);
        // }
        // }
        // }
        // return res;
        // }
        // catch (JqmInvalidRequestException e)
        // {
        // throw e;
        // }
        // catch (Exception e)
        // {
        // throw new JqmClientException("an error occured during query execution", e);
        // }
        // finally
        // {
        // closeQuietly(em);
        // }
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getJobs()
    {
        return Query.create().setQueryHistoryInstances(true).setQueryLiveInstances(true).run();
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getActiveJobs()
    {
        return Query.create().setQueryHistoryInstances(false).setQueryLiveInstances(true).addSortAsc(Sort.ID).run();
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getUserActiveJobs(String user)
    {
        if (user == null || user.isEmpty())
        {
            throw new JqmInvalidRequestException("user cannot be null or empty");
        }

        return Query.create().setUser(user).setQueryHistoryInstances(false).setQueryLiveInstances(true).addSortAsc(Sort.ID).run();
    }

    // /////////////////////////////////////////////////////////////////////
    // Helpers to quickly access some job instance properties
    // /////////////////////////////////////////////////////////////////////

    @Override
    public List<String> getJobMessages(int idJob)
    {
        return getJob(idJob).getMessages();
    }

    @Override
    public int getJobProgress(int idJob)
    {
        return getJob(idJob).getProgress();
    }

    // /////////////////////////////////////////////////////////////////////
    // Deliverables retrieval
    // /////////////////////////////////////////////////////////////////////

    @Override
    public List<com.enioka.jqm.api.Deliverable> getJobDeliverables(int idJob)
    {
        DbConn cnx = null;

        try
        {
            cnx = getDbSession();

            // TODO: no intermediate entity here: directly SQL => API object.
            List<Deliverable> deliverables = Deliverable.select(cnx, "deliverable_select_all_for_ji", idJob);
            List<com.enioka.jqm.api.Deliverable> res = new ArrayList<com.enioka.jqm.api.Deliverable>();
            for (Deliverable d : deliverables)
            {
                res.add(new com.enioka.jqm.api.Deliverable(d.getFilePath(), d.getFileFamily(), d.getId(), d.getOriginalFileName()));
            }

            return res;
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not query files for job instance " + idJob, e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    @Override
    public List<InputStream> getJobDeliverablesContent(int idJob)
    {
        DbConn cnx = null;
        ArrayList<InputStream> streams = new ArrayList<InputStream>();

        try
        {
            cnx = getDbSession();
            for (Deliverable del : Deliverable.select(cnx, "deliverable_select_all_for_ji", idJob))
            {
                streams.add(getDeliverableContent(del));
            }
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not retrieve file streams", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
        return streams;
    }

    @Override
    public InputStream getDeliverableContent(com.enioka.jqm.api.Deliverable d)
    {
        return getDeliverableContent(d.getId());
    }

    @Override
    public InputStream getDeliverableContent(int delId)
    {
        DbConn cnx = null;
        Deliverable deliverable = null;

        try
        {
            cnx = getDbSession();
            List<Deliverable> dd = Deliverable.select(cnx, "deliverable_select_by_id", delId);
            if (dd.size() == 0)
            {
                throw new JqmClientException("There is no deliverable with the given ID - check your ID");
            }
            deliverable = dd.get(0);
        }
        catch (Exception e)
        {
            throw new JqmInvalidRequestException("Could not get find deliverable description inside DB - your ID may be wrong", e);
        }
        finally
        {
            closeQuietly(cnx);
        }

        return getDeliverableContent(deliverable);
    }

    InputStream getEngineLog(String nodeName, int latest)
    {
        DbConn cnx = getDbSession();
        URL url = null;

        try
        {
            ResultSet rs = cnx.runSelect("node_select_connectdata_by_key", nodeName);
            if (!rs.next())
            {
                throw new NoResultException("no node named " + nodeName);
            }
            String dns = rs.getString(1);
            Integer port = rs.getInt(2);

            if (rs.next())
            {
                throw new NonUniqueResultException("configuration issue: multiple nodes named " + nodeName);
            }

            url = new URL(getFileProtocol(cnx) + dns + ":" + port + "/ws/simple/enginelog?latest=" + latest);
            jqmlogger.trace("Will invoke engine log URL: " + url.toString());
        }
        catch (MalformedURLException e)
        {
            throw new JqmInvalidRequestException("URL is not valid " + url);
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("Node with name " + nodeName + " does not exist");
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not process request", e);
        }
        finally
        {
            closeQuietly(cnx);
        }

        return getFile(url.toString());
    }

    // Helper
    private InputStream getDeliverableContent(Deliverable deliverable)
    {
        URL url = null;
        try
        {
            String uriStart = getHostForLaunch(deliverable.getJobId());
            url = new URL(uriStart + "/ws/simple/file?id=" + deliverable.getRandomId());
            jqmlogger.trace("URL: " + url.toString());
        }
        catch (MalformedURLException e)
        {
            throw new JqmClientException("URL is not valid " + url, e);
        }

        return getFile(url.toString());
    }

    private String getFileProtocol(DbConn cnx)
    {
        if (protocol == null)
        {
            protocol = "http://";
            try
            {
                String prm = GlobalParameter.getParameter(cnx, "enableWsApiSsl", "false");
                if (Boolean.parseBoolean(prm))
                {
                    protocol = "https://";
                }
            }
            catch (NoResultException e)
            {
                protocol = "http://";
            }
        }
        return protocol;
    }

    private InputStream getFile(String url)
    {
        DbConn cnx = getDbSession();
        File file = null;
        FileOutputStream fos = null;
        CloseableHttpClient cl = null;
        CloseableHttpResponse rs = null;
        String nameHint = null;

        File destDir = new File(System.getProperty("java.io.tmpdir"));
        if (!destDir.isDirectory() && !destDir.mkdir())
        {
            throw new JqmClientException("could not create temp directory " + destDir.getAbsolutePath());
        }
        jqmlogger.trace("File will be copied into " + destDir);

        try
        {
            file = new File(destDir + "/" + UUID.randomUUID().toString());

            CredentialsProvider credsProvider = null;
            if (SimpleApiSecurity.getId(cnx).usr != null)
            {
                credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(SimpleApiSecurity.getId(cnx).usr, SimpleApiSecurity.getId(cnx).pass));
            }
            SSLContext ctx = null;
            if (getFileProtocol(cnx).equals("https://"))
            {
                try
                {
                    if (p.containsKey("com.enioka.jqm.ws.truststoreFile"))
                    {
                        KeyStore trust = null;
                        InputStream trustIs = null;

                        try
                        {
                            trust = KeyStore.getInstance(this.p.getProperty("com.enioka.jqm.ws.truststoreType", "JKS"));
                        }
                        catch (KeyStoreException e)
                        {
                            throw new JqmInvalidRequestException("Specified trust store type ["
                                    + this.p.getProperty("com.enioka.jqm.ws.truststoreType", "JKS") + "] is invalid", e);
                        }

                        try
                        {
                            trustIs = new FileInputStream(this.p.getProperty("com.enioka.jqm.ws.truststoreFile"));
                        }
                        catch (FileNotFoundException e)
                        {
                            throw new JqmInvalidRequestException(
                                    "Trust store file [" + this.p.getProperty("com.enioka.jqm.ws.truststoreFile") + "] cannot be found", e);
                        }

                        String trustp = this.p.getProperty("com.enioka.jqm.ws.truststorePass", null);
                        try
                        {
                            trust.load(trustIs, (trustp == null ? null : trustp.toCharArray()));
                        }
                        catch (Exception e)
                        {
                            throw new JqmInvalidRequestException("Could not load the trust store file", e);
                        }
                        finally
                        {
                            try
                            {
                                trustIs.close();
                            }
                            catch (IOException e)
                            {
                                // Nothing to do.
                            }
                        }
                        ctx = SSLContexts.custom().loadTrustMaterial(trust).build();
                    }
                    else
                    {
                        ctx = SSLContexts.createSystemDefault();
                    }
                }
                catch (Exception e)
                {
                    // Cannot happen - not trust store is actually loaded!
                    jqmlogger.error("An supposedly impossible error has happened. Downloading files through the API may not work.", e);
                }
            }
            cl = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setSslcontext(ctx).build();

            // Run HTTP request
            HttpUriRequest rq = new HttpGet(url.toString());
            rs = cl.execute(rq);
            if (rs.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                throw new JqmClientException(
                        "Could not retrieve file from JQM node. The file may have been purged, or the node may be unreachable. HTTP code was: "
                                + rs.getStatusLine().getStatusCode());
            }

            // There may be a filename hint inside the response
            Header[] hs = rs.getHeaders("Content-Disposition");
            if (hs.length == 1)
            {
                Header h = hs[0];
                if (h.getValue().contains("filename="))
                {
                    nameHint = h.getValue().split("=")[1];
                }
            }

            // Save the file to a temp local file
            fos = new FileOutputStream(file);
            rs.getEntity().writeTo(fos);
            jqmlogger.trace("File was downloaded to " + file.getAbsolutePath());
        }
        catch (IOException e)
        {
            throw new JqmClientException("Could not create a webserver-local copy of the file. The remote node may be down.", e);
        }
        finally
        {
            closeQuietly(cnx);
            closeQuietly(fos);
            closeQuietly(rs);
            closeQuietly(cl);
        }

        SelfDestructFileStream res = null;
        try
        {
            res = new SelfDestructFileStream(file);
        }
        catch (IOException e)
        {
            throw new JqmClientException("File seems not to be present where it should have been downloaded", e);
        }
        res.nameHint = nameHint;
        return res;
    }

    @Override
    public InputStream getJobLogStdOut(int jobId)
    {
        return getJobLog(jobId, ".stdout", "stdout");
    }

    @Override
    public InputStream getJobLogStdErr(int jobId)
    {
        return getJobLog(jobId, ".stderr", "stderr");
    }

    private String getHostForLaunch(int launchId)
    {
        String host;
        DbConn cnx = null;
        try
        {
            cnx = getDbSession();
            try
            {
                host = cnx.runSelectSingle("history_select_cnx_data_by_id", String.class, launchId);
            }
            catch (NoResultException e)
            {
                try
                {
                    host = cnx.runSelectSingle("ji_select_cnx_data_by_id", String.class, launchId);
                }
                catch (NoResultException r)
                {
                    throw new JqmInvalidRequestException("No ended or running job instance found for this file");
                }
            }

            if (host == null || host.isEmpty() || host.split(":")[0].isEmpty())
            {
                throw new JqmInvalidRequestException("cannot retrieve a file from a deleted node");
            }

            protocol = getFileProtocol(cnx);
            return protocol + "//" + host;
        }
        catch (JqmException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not process request", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    private InputStream getJobLog(int jobId, String extension, String param)
    {
        // 1: retrieve node to address
        String uriStart = getHostForLaunch(jobId);

        // 2: build URL
        URL url = null;
        try
        {
            url = new URL(uriStart + "/ws/simple/" + param + "?id=" + jobId);
            jqmlogger.trace("URL: " + url.toString());
        }
        catch (MalformedURLException e)
        {
            throw new JqmClientException("URL is not valid " + url, e);
        }

        return getFile(url.toString());
    }

    // /////////////////////////////////////////////////////////////////////
    // Parameters retrieval
    // /////////////////////////////////////////////////////////////////////

    @Override
    public List<com.enioka.jqm.api.Queue> getQueues()
    {
        List<com.enioka.jqm.api.Queue> res = new ArrayList<com.enioka.jqm.api.Queue>();
        DbConn cnx = null;
        com.enioka.jqm.api.Queue tmp = null;

        try
        {
            cnx = getDbSession();
            for (Queue q : Queue.select(cnx, "q_select_all"))
            {
                tmp = getQueue(q);
                res.add(tmp);
            }
            return res;
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not query queues", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }

    private static com.enioka.jqm.api.Queue getQueue(Queue queue)
    {
        com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

        q.setDescription(queue.getDescription());
        q.setId(queue.getId());
        q.setName(queue.getName());

        return q;
    }

    @Override
    public List<com.enioka.jqm.api.JobDef> getJobDefinitions()
    {
        return getJobDefinitions(null);
    }

    @Override
    public List<com.enioka.jqm.api.JobDef> getJobDefinitions(String application)
    {
        List<com.enioka.jqm.api.JobDef> res = new ArrayList<com.enioka.jqm.api.JobDef>();
        DbConn cnx = null;
        List<JobDef> dbr = null;

        try
        {
            // TODO: remove model objects and go directly from RS to API objects. Also, join to avoid multiple queries.

            cnx = getDbSession();
            if (application == null)
            {
                dbr = JobDef.select(cnx, "jd_select_all");
            }
            else
            {
                dbr = JobDef.select(cnx, "jd_select_by_tag_app", application);
            }

            for (JobDef jd : dbr)
            {
                com.enioka.jqm.api.JobDef tmp = new com.enioka.jqm.api.JobDef();
                tmp.setApplication(jd.getApplication());
                tmp.setApplicationName(jd.getApplicationName());
                tmp.setCanBeRestarted(jd.isCanBeRestarted());
                tmp.setDescription(jd.getDescription());
                tmp.setHighlander(jd.isHighlander());
                tmp.setKeyword1(jd.getKeyword1());
                tmp.setKeyword2(jd.getKeyword2());
                tmp.setKeyword3(jd.getKeyword3());
                tmp.setModule(jd.getModule());
                tmp.setQueue(getQueue(jd.getQueue(cnx)));
                tmp.setId(jd.getId());

                for (JobDefParameter jdf : jd.getParameters(cnx))
                {
                    tmp.addParameter(jdf.getKey(), jdf.getValue());
                }

                res.add(tmp);
            }
            return res;
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not query JobDef", e);
        }
        finally
        {
            closeQuietly(cnx);
        }
    }
}
