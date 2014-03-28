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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.naming.NameNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.api.Query.SortSpec;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobHistoryParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.MessageJi;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.State;

/**
 * Main JQM client API entry point.
 */
final class HibernateClient implements JqmClient
{
    private static Logger jqmlogger = LoggerFactory.getLogger(HibernateClient.class);
    private static final String PERSISTENCE_UNIT = "jobqueue-api-pu";
    private EntityManagerFactory emf = null;
    Properties p;

    // /////////////////////////////////////////////////////////////////////
    // Construction/Connection
    // /////////////////////////////////////////////////////////////////////

    // No public constructor. MUST use factory.
    HibernateClient()
    {
        p = new Properties();
    }

    HibernateClient(Properties p)
    {
        this.p = p;
        if (p.containsKey("emf"))
        {
            jqmlogger.trace("emf present in properties");
            emf = (EntityManagerFactory) p.get("emf");
        }
    }

    private EntityManagerFactory createFactory()
    {
        jqmlogger.debug("Creating connection pool to database");

        InputStream fis = null;
        try
        {
            jqmlogger.trace("Current . is:" + (new File(".")).getAbsolutePath());
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

            fis = this.getClass().getClassLoader().getResourceAsStream("db.properties");
            if (fis == null)
            {
                jqmlogger.trace("No db.properties file found.");
            }
            else
            {
                p.load(fis);
                jqmlogger.trace("A db.properties file was found");
            }
        }
        catch (IOException e)
        {
            // We allow no configuration files, but not an unreadable configuration file.
            throw new JqmClientException("META-INF/jqm.properties file is invalid", e);
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }

        EntityManagerFactory newEmf = null;
        if (p.containsKey("javax.persistence.nonJtaDataSource"))
        {
            // This is a hack. Some containers will use root context as default for JNDI (WebSphere, Glassfish...), other will use
            // java:/comp/env/ (Tomcat...). So if we actually know the required alias, we try both, and the user only has to provide a
            // root JNDI alias that will work in both cases.
            try
            {
                newEmf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, p);
                // Do a stupid query to force EMF initialization
                EntityManager em = newEmf.createEntityManager();
                em.createQuery("SELECT n from Node n WHERE 1=0").getResultList().size();
                em.close();
            }
            catch (RuntimeException e)
            {
                if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof NameNotFoundException)
                {
                    jqmlogger.debug("JNDI alias " + p.getProperty("javax.persistence.nonJtaDataSource")
                            + " was not found. Trying with java:/comp/env/ prefix");
                    p.setProperty("javax.persistence.nonJtaDataSource",
                            "java:/comp/env/" + p.getProperty("javax.persistence.nonJtaDataSource"));
                    newEmf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, p);
                    // Do a stupid query to force EMF initialization
                    EntityManager em = newEmf.createEntityManager();
                    em.createQuery("SELECT n from Node n WHERE 1=3").getResultList().size();
                    em.close();
                }
                else
                {
                    throw e;
                }
            }
        }
        else
        {
            newEmf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, p);
        }
        return newEmf;
    }

    EntityManager getEm()
    {
        if (emf == null)
        {
            emf = createFactory();
        }

        try
        {
            return emf.createEntityManager();
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not create EM.", e);
            throw new JqmClientException("Could not create EntityManager", e);
        }
    }

    @Override
    public void dispose()
    {
        try
        {
            this.emf.close();
        }
        catch (Exception e)
        {
            // Nothing - dispose function must fail silently.
        }
        this.emf = null;
        p = null;
    }

    // /////////////////////////////////////////////////////////////////////
    // Enqueue functions
    // /////////////////////////////////////////////////////////////////////

    @Override
    public int enqueue(JobRequest jd)
    {
        jqmlogger.trace("BEGINING ENQUEUE");
        EntityManager em = getEm();
        JobDef job = null;
        try
        {
            job = em.createQuery(
                    "SELECT j FROM JobDef j LEFT JOIN FETCH j.queue LEFT JOIN FETCH j.parameters WHERE j.applicationName = :name",
                    JobDef.class).setParameter("name", jd.getApplicationName()).getSingleResult();
        }
        catch (NoResultException ex)
        {
            jqmlogger.error("Job definition named " + jd.getApplicationName() + " does not exist");
            em.close();
            throw new JqmInvalidRequestException("no such job definition");
        }

        jqmlogger.trace("Job to enqueue is from JobDef " + job.getId());
        Integer hl = null;
        List<JobParameter> jps = overrideParameter(job, jd, em);

        // Begin transaction (that will hold a lock in case of Highlander)
        em.getTransaction().begin();

        if (job.isHighlander())
        {
            hl = highlanderMode(job, em);
        }

        if (hl != null)
        {
            jqmlogger.trace("JI won't actually be enqueued because a job in highlander mode is currently submitted: " + hl);
            em.getTransaction().rollback();
            em.close();
            return hl;
        }
        jqmlogger.trace("Not in highlander mode or no currently enqueued instance");

        JobInstance ji = new JobInstance();
        ji.setJd(job);
        ji.setSessionID(jd.getSessionID());
        ji.setUserName(jd.getUser());
        ji.setState(State.SUBMITTED);
        ji.setQueue(job.getQueue());
        ji.setNode(null);
        // Can be null (if no email is asked for)
        ji.setEmail(jd.getEmail());
        ji.setCreationDate(Calendar.getInstance());
        if (jd.getParentID() != null)
        {
            ji.setParentId(jd.getParentID());
        }
        ji.setProgress(0);
        ji.setParameters(new ArrayList<JobParameter>());
        em.persist(ji);
        ji.setInternalPosition(ji.getId());

        for (JobParameter jp : jps)
        {
            jqmlogger.trace("Parameter: " + jp.getKey() + " - " + jp.getValue());
            em.persist(ji.addParameter(jp.getKey(), jp.getValue()));
        }
        jqmlogger.trace("JI just created: " + ji.getId());

        em.getTransaction().commit();
        em.close();
        return ji.getId();
    }

    @Override
    public int enqueue(String applicationName, String userName)
    {
        return enqueue(new JobRequest(applicationName, userName));
    }

    @Override
    public int enqueueFromHistory(int jobIdToCopy)
    {
        EntityManager em = getEm();
        History h = null;
        try
        {
            h = em.find(History.class, jobIdToCopy);
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("No job for this ID in the history");
        }
        return enqueue(getJobRequest(h));
    }

    // Helper
    private List<JobParameter> overrideParameter(JobDef jdef, JobRequest jdefinition, EntityManager em)
    {
        List<JobParameter> res = new ArrayList<JobParameter>();
        Map<String, String> resm = new HashMap<String, String>();

        // 1st: default parameters
        for (JobDefParameter jp : jdef.getParameters())
        {
            resm.put(jp.getKey(), jp.getValue());
        }

        // 2nd: overloads inside the user enqueue form.
        resm.putAll(jdefinition.getParameters());

        // 3rd: create the JobParameter objects
        for (Entry<String, String> e : resm.entrySet())
        {
            res.add(createJobParameter(e.getKey(), e.getValue(), em));
        }

        // Done
        return res;
    }

    // Helper. Must be called within an active JPA transaction
    private Integer highlanderMode(JobDef jd, EntityManager em)
    {
        // Synchronization is done through locking the JobDef
        em.lock(jd, LockModeType.PESSIMISTIC_WRITE);

        // Do the analysis
        Integer res = null;
        jqmlogger.trace("Highlander mode analysis is begining");
        ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em
                .createQuery("SELECT j FROM JobInstance j WHERE j.jd = :j AND j.state = :s", JobInstance.class).setParameter("j", jd)
                .setParameter("s", State.SUBMITTED).getResultList();

        for (JobInstance j : jobs)
        {
            jqmlogger.trace("JI seen by highlander: " + j.getId() + j.getState());
            if (j.getState().equals(State.SUBMITTED))
            {
                // HIGHLANDER: only one enqueued job can survive!
                // current request must be cancelled and enqueue must return the id of the existing submitted JI
                res = j.getId();
                break;
            }
        }
        jqmlogger.trace("Highlander mode will return: " + res);
        return res;
    }

    // Helper
    private JobRequest getJobRequest(History h)
    {
        JobRequest jd = new JobRequest();
        jd.setApplication(h.getApplication());
        jd.setApplicationName(h.getJd().getApplicationName());
        jd.setEmail(h.getEmail());
        jd.setKeyword1(h.getKeyword1());
        jd.setKeyword2(h.getKeyword2());
        jd.setKeyword3(h.getKeyword3());
        jd.setModule(h.getModule());
        jd.setParentID(h.getParentJobId());
        jd.setSessionID(h.getSessionId());
        jd.setUser(h.getUserName());

        for (JobHistoryParameter p : h.getParameters())
        {
            jd.addParameter(p.getKey(), p.getValue());
        }

        return jd;
    }

    // /////////////////////////////////////////////////////////////////////
    // Job destruction
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void cancelJob(int idJob)
    {
        EntityManager em = getEm();

        JobInstance ji = null;
        try
        {
            em.getTransaction().begin();
            ji = em.find(JobInstance.class, idJob, LockModeType.PESSIMISTIC_WRITE);
            if (ji.getState().equals(State.SUBMITTED))
            {
                ji.setState(State.CANCELLED);
            }
            else
            {
                throw new NoResultException();
            }
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            em.getTransaction().commit();
            em.close();
            throw new JqmClientException("the job is already running, has already finished or never existed to begin with");
        }

        em.getTransaction().begin();
        History h = new History();
        h.setId(ji.getId());
        h.setJd(ji.getJd());
        h.setSessionId(ji.getSessionID());
        h.setQueue(ji.getQueue());
        h.setMessages(new ArrayList<Message>());
        h.setEnqueueDate(ji.getCreationDate());
        h.setUserName(ji.getUserName());
        h.setEmail(ji.getEmail());
        h.setParentJobId(ji.getParentId());
        h.setApplication(ji.getApplication());
        h.setModule(ji.getModule());
        h.setKeyword1(ji.getKeyword1());
        h.setKeyword2(ji.getKeyword2());
        h.setKeyword3(ji.getKeyword3());
        h.setProgress(ji.getProgress());
        h.setParameters(new ArrayList<JobHistoryParameter>());
        h.setStatus(State.CANCELLED);
        h.setNode(ji.getNode());
        em.persist(h);

        em.createQuery("DELETE FROM MessageJi WHERE jobInstance = :i").setParameter("i", ji).executeUpdate();
        em.createQuery("DELETE FROM JobParameter WHERE jobInstance = :i").setParameter("i", ji).executeUpdate();
        em.createQuery("DELETE FROM JobInstance WHERE id = :i").setParameter("i", ji.getId()).executeUpdate();
        em.getTransaction().commit();
    }

    @Override
    public void deleteJob(int idJob)
    {
        jqmlogger.trace("Job status number " + idJob + " will be deleted");
        EntityManager em = getEm();

        try
        {
            // Two transactions against deadlock.
            JobInstance job = em.find(JobInstance.class, idJob);
            em.getTransaction().begin();
            em.refresh(job, LockModeType.PESSIMISTIC_WRITE);
            if (job.getState().equals(State.SUBMITTED))
            {
                job.setState(State.CANCELLED);
            }
            em.getTransaction().commit();

            if (!job.getState().equals(State.CANCELLED))
            {
                // Job is not in queue anymore - just return.
                return;
            }

            em.getTransaction().begin();
            em.createQuery("DELETE FROM MessageJi WHERE jobInstance = :i").setParameter("i", job).executeUpdate();
            em.createQuery("DELETE FROM JobParameter WHERE jobInstance = :i").setParameter("i", job).executeUpdate();
            em.createQuery("DELETE FROM JobInstance WHERE id = :i").setParameter("i", job.getId()).executeUpdate();
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            jqmlogger.info("An attempt was made to delete a job instance that did not exist, which can be perfectly normal.");
        }
        catch (Exception e)
        {
            jqmlogger.info("unknown exception", e);
        }
        finally
        {
            em.close();
        }
    }

    @Override
    public void killJob(int idJob)
    {
        EntityManager em = getEm();
        em.getTransaction().begin();
        JobInstance j = em.find(JobInstance.class, idJob, LockModeType.PESSIMISTIC_READ);
        jqmlogger.trace("The " + j.getState() + " job (ID: " + idJob + ")" + " will be marked for kill");

        j.setState(State.KILLED);

        MessageJi m = new MessageJi();
        m.setJobInstance(j);
        m.setTextMessage("Kill attempt on the job");
        em.persist(m);

        em.getTransaction().commit();
        em.close();
    }

    // /////////////////////////////////////////////////////////////////////
    // Job Pause/restart
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void pauseQueuedJob(int idJob)
    {
        jqmlogger.trace("Job status number " + idJob + " will be set to HOLDED");
        EntityManager em = getEm();

        em.getTransaction().begin();

        try
        {
            @SuppressWarnings("unused")
            int q = em.createQuery("UPDATE JobInstance j SET j.state = 'HOLDED' WHERE j.id = :idJob").setParameter("idJob", idJob)
                    .executeUpdate();
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not pause job", e);
        }

        em.getTransaction().commit();

        em.close();
    }

    @Override
    public void resumeJob(int idJob)
    {
        jqmlogger.trace("Job status number " + idJob + " will be resumed");
        EntityManager em = getEm();

        em.getTransaction().begin();

        try
        {
            @SuppressWarnings("unused")
            int q = em.createQuery("UPDATE JobInstance j SET j.state = 'SUBMITTED' WHERE j.id = :idJob").setParameter("idJob", idJob)
                    .executeUpdate();
        }
        catch (Exception e)
        {
            jqmlogger.error("could not resume job", e);
        }

        em.getTransaction().commit();

        em.close();
    }

    public int restartCrashedJob(int idJob)
    {
        EntityManager em = getEm();

        // History and Job ID have the same ID.
        History h = null;
        try
        {
            h = em.find(History.class, idJob);
        }
        catch (NoResultException e)
        {
            em.close();
            throw new JqmClientException("You cannot restart a job that is not done or which was purged from history");
        }

        if (!h.getState().equals(State.CRASHED))
        {
            em.close();
            throw new JqmClientException("You cannot restart a job that has not crashed");
        }

        if (!h.getJd().isCanBeRestarted())
        {
            em.close();
            throw new JqmClientException("This type of job was configured to prevent being restarded");
        }

        em.getTransaction().begin();
        em.remove(h);
        em.getTransaction().commit();
        em.close();

        return enqueue(getJobRequest(h));
    }

    // /////////////////////////////////////////////////////////////////////
    // Misc.
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void setJobQueue(int idJob, int idQueue)
    {
        EntityManager em = getEm();
        JobInstance ji = null;
        Queue q = null;

        try
        {
            q = em.find(Queue.class, idQueue);
        }
        catch (NoResultException e)
        {
            em.close();
            throw new JqmClientException("Queue does not exist");
        }

        try
        {
            em.getTransaction().begin();
            ji = em.find(JobInstance.class, idJob, LockModeType.PESSIMISTIC_WRITE);
            if (!ji.getState().equals(State.SUBMITTED))
            {
                throw new NoResultException();
            }
            ji.setQueue(q);
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            em.getTransaction().rollback();
            throw new JqmClientException("Job instance does not exist or has already started");
        }
        finally
        {
            em.close();
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
        EntityManager em = getEm();
        em.getTransaction().begin();
        JobInstance ji = null;
        try
        {
            ji = em.find(JobInstance.class, idJob, LockModeType.PESSIMISTIC_WRITE);
        }
        catch (Exception e)
        {
            em.getTransaction().rollback();
            em.close();
            throw new JqmClientException(
                    "Could not lock a job by the given ID. It may already have been executed or a timeout may have occurred.", e);
        }

        if (!ji.getState().equals(State.SUBMITTED))
        {
            em.getTransaction().rollback();
            em.close();
            throw new JqmClientException("Job is already set for execution. Too late to change its position in the queue");
        }

        int current = ji.getCurrentPosition(em);
        int betweenUp = 0;
        int betweenDown = 0;

        if (current == position)
        {
            // Nothing to do
            em.getTransaction().rollback();
            em.close();
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

        // No locking - we'll deal with exceptions
        List<JobInstance> currentJobs = em
                .createQuery("SELECT JobInstance ji from JobInstance ORDER BY ji.internalPosition", JobInstance.class)
                .setMaxResults(betweenUp).getResultList();

        if (currentJobs.size() == 0)
        {
            ji.setInternalPosition(0);
            em.getTransaction().rollback();
            em.close();
            return;
        }
        else if (currentJobs.size() < betweenUp)
        {
            ji.setInternalPosition(currentJobs.get(currentJobs.size() - 1).getInternalPosition() + 0.00001);
            em.getTransaction().rollback();
            em.close();
            return;
        }
        else
        {
            // Normal case: put the JI between the two others.
            ji.setInternalPosition((currentJobs.get(betweenUp - 1).getInternalPosition() + currentJobs.get(betweenDown - 1)
                    .getInternalPosition()) / 2);
            em.getTransaction().rollback();
            em.close();
            return;
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Job queries
    // /////////////////////////////////////////////////////////////////////

    // Helper
    private com.enioka.jqm.api.JobInstance getJob(JobInstance h)
    {
        com.enioka.jqm.api.JobInstance ji = new com.enioka.jqm.api.JobInstance();
        ji.setId(h.getId());
        ji.setApplicationName(h.getJd().getApplicationName());
        ji.setParameters(new HashMap<String, String>());
        ji.setParent(h.getParentId());
        ji.setQueue(getQueue(h.getQueue()));
        ji.setSessionID(h.getSessionID());
        ji.setState(com.enioka.jqm.api.State.valueOf(h.getState().toString()));
        ji.setUser(h.getUserName());
        ji.setProgress(h.getProgress());
        for (JobParameter p : h.getParameters())
        {
            ji.getParameters().put(p.getKey(), p.getValue());
        }
        for (MessageJi m : h.getMessages())
        {
            ji.getMessages().add(m.getTextMessage());
        }
        ji.setKeyword1(h.getKeyword1());
        ji.setKeyword2(h.getKeyword2());
        ji.setKeyword3(h.getKeyword3());
        ji.setApplication(h.getApplication());
        ji.setModule(h.getModule());
        ji.setEmail(h.getEmail());
        ji.setEnqueueDate(h.getCreationDate());
        ji.setBeganRunningDate(h.getExecutionDate());
        if (h.getNode() != null)
        {
            ji.setNodeName(h.getNode().getName());
        }

        return ji;
    }

    // Helper
    private com.enioka.jqm.api.JobInstance getJob(History h)
    {
        com.enioka.jqm.api.JobInstance ji = new com.enioka.jqm.api.JobInstance();
        ji.setId(h.getId());
        ji.setApplicationName(h.getJd().getApplicationName());
        ji.setParameters(new HashMap<String, String>());
        ji.setParent(h.getParentJobId());
        ji.setQueue(getQueue(h.getQueue()));
        ji.setSessionID(h.getSessionId());
        ji.setState(com.enioka.jqm.api.State.valueOf(h.getStatus().toString()));
        ji.setUser(h.getUserName());
        ji.setProgress(h.getProgress());
        for (JobHistoryParameter p : h.getParameters())
        {
            ji.getParameters().put(p.getKey(), p.getValue());
        }
        for (Message m : h.getMessages())
        {
            ji.getMessages().add(m.getTextMessage());
        }
        ji.setKeyword1(h.getKeyword1());
        ji.setKeyword2(h.getKeyword2());
        ji.setKeyword3(h.getKeyword3());
        ji.setApplication(h.getApplication());
        ji.setModule(h.getModule());
        ji.setEmail(h.getEmail());
        ji.setEnqueueDate(h.getEnqueueDate());
        ji.setBeganRunningDate(h.getExecutionDate());
        ji.setEndDate(h.getEndDate());
        if (h.getNode() != null)
        {
            ji.setNodeName(h.getNode().getName());
        }

        return ji;
    }

    // GetJob helper - String predicates are all created the same way, so this factors some code.
    private String getStringPredicate(String fieldName, String filterValue, Map<String, Object> prms)
    {
        if (filterValue != null)
        {
            if (!filterValue.isEmpty())
            {
                String prmName = fieldName.split("\\.")[fieldName.split("\\.").length - 1] + Math.abs(fieldName.hashCode());
                prms.put(prmName, filterValue);
                if (filterValue.contains("%"))
                {
                    return String.format("AND (%s LIKE :%s) ", fieldName, prmName);
                }
                else
                {
                    return String.format("AND (%s = :%s) ", fieldName, prmName);
                }
            }
            else
            {
                return String.format("AND (h.%s IS NULL OR h.%s = '') ", fieldName, fieldName);
            }
        }
        return "";
    }

    private String getIntPredicate(String fieldName, Integer filterValue, Map<String, Object> prms)
    {
        if (filterValue != null)
        {
            if (filterValue != -1)
            {
                String prmName = fieldName.split("\\.")[fieldName.split("\\.").length - 1];
                prms.put(prmName, filterValue);
                return String.format("AND (%s = :%s) ", fieldName, prmName);
            }
            else
            {
                return String.format("AND (h.%s IS NULL) ", fieldName);
            }
        }
        return "";
    }

    private String getCalendarPredicate(String fieldName, Calendar filterValue, String comparison, Map<String, Object> prms)
    {
        if (filterValue != null)
        {
            String prmName = fieldName.split("\\.")[fieldName.split("\\.").length - 1] + Math.abs(comparison.hashCode());
            prms.put(prmName, filterValue);
            return String.format("AND (%s %s :%s) ", fieldName, comparison, prmName);
        }
        else
        {
            return "";
        }
    }

    private String getStatusPredicate(String fieldName, List<com.enioka.jqm.api.State> status, Map<String, Object> prms)
    {
        if (status == null || status.size() == 0)
        {
            return "";
        }

        String res = String.format("AND ( %s IN ( ", fieldName);

        for (com.enioka.jqm.api.State s : status)
        {
            String prmName = "status" + s.hashCode();
            res += " :" + prmName + ",";
            prms.put(prmName, State.valueOf(s.toString()));
        }
        res = res.substring(0, res.length() - 1) + ")) ";
        return res;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getJobs(Query query)
    {
        if ((query.getFirstRow() != null || query.getPageSize() != null) && query.isQueryLiveInstances())
        {
            throw new IllegalArgumentException("cannot use paging on live instances");
        }

        EntityManager em = getEm();

        // Not using CriteriaBuilder - too much hassle for too little benefit
        String wh = "";
        Map<String, Object> prms = new HashMap<String, Object>();

        // String predicates
        wh += getStringPredicate("jd.applicationName", query.getApplicationName(), prms);
        wh += getStringPredicate("userName", query.getUser(), prms);
        wh += getStringPredicate("sessionId", query.getSessionId(), prms);
        wh += getStringPredicate("instanceKeyword1", query.getInstanceKeyword1(), prms);
        wh += getStringPredicate("instanceKeyword2", query.getInstanceKeyword2(), prms);
        wh += getStringPredicate("instanceKeyword3", query.getInstanceKeyword3(), prms);
        wh += getStringPredicate("instanceModule", query.getInstanceModule(), prms);
        wh += getStringPredicate("instanceApplication", query.getInstanceApplication(), prms);
        wh += getStringPredicate("jd.keyword1", query.getJobDefKeyword1(), prms);
        wh += getStringPredicate("jd.keyword2", query.getJobDefKeyword2(), prms);
        wh += getStringPredicate("jd.keyword3", query.getJobDefKeyword3(), prms);
        wh += getStringPredicate("jd.module", query.getJobDefModule(), prms);
        wh += getStringPredicate("jd.application", query.getJobDefApplication(), prms);
        wh += getStringPredicate("queue.name", query.getQueueName(), prms);

        // Integer
        wh += getIntPredicate("parentId", query.getParentId(), prms);
        wh += getIntPredicate("id", query.getJobInstanceId(), prms);
        wh += getIntPredicate("queue.id", query.getQueueName() == null ? null : query.getQueueId(), prms);

        // Now, run queries...
        List<com.enioka.jqm.api.JobInstance> res2 = new ArrayList<com.enioka.jqm.api.JobInstance>();

        // ////////////////////////////////////////
        // Job Instance query
        if (query.isQueryLiveInstances())
        {
            // Sort
            String sort = "";
            for (SortSpec s : query.getSorts())
            {
                sort += s.col.getJiField() == null ? "" : ",h." + s.col.getJiField() + " "
                        + (s.order == Query.SortOrder.ASCENDING ? "ASC" : "DESC");
            }
            if (sort.isEmpty())
            {
                sort = " ORDER BY h.id";
            }
            else
            {
                sort = " ORDER BY " + sort.substring(1);
            }

            // Calendar fields are specific (no common fields between History and JobInstance)
            String wh2 = "" + wh;
            Map<String, Object> prms2 = new HashMap<String, Object>();
            prms2.putAll(prms);
            wh2 += getCalendarPredicate("creationDate", query.getEnqueuedAfter(), ">=", prms2);
            wh2 += getCalendarPredicate("creationDate", query.getEnqueuedBefore(), "<=", prms2);
            wh2 += getCalendarPredicate("executionDate", query.getBeganRunningAfter(), ">=", prms2);
            wh2 += getCalendarPredicate("executionDate", query.getBeganRunningBefore(), "<=", prms2);
            wh2 += getStatusPredicate("state", query.getStatus(), prms2);
            if (wh2.length() >= 3)
            {
                wh2 = " WHERE " + wh2.substring(3);
            }

            TypedQuery<JobInstance> q2 = em.createQuery("SELECT h FROM JobInstance h " + wh2, JobInstance.class);
            for (Map.Entry<String, Object> entry : prms2.entrySet())
            {
                q2.setParameter(entry.getKey(), entry.getValue());
            }

            for (JobInstance ji : q2.getResultList())
            {
                res2.add(getJob(ji));
            }
        }

        // ////////////////////////////////////////
        // History query

        // Calendar fields are specific (no common fields between History and JobInstance)
        wh += getCalendarPredicate("enqueueDate", query.getEnqueuedAfter(), ">=", prms);
        wh += getCalendarPredicate("enqueueDate", query.getEnqueuedBefore(), "<=", prms);
        wh += getCalendarPredicate("executionDate", query.getBeganRunningAfter(), ">=", prms);
        wh += getCalendarPredicate("executionDate", query.getBeganRunningBefore(), "<=", prms);
        wh += getCalendarPredicate("endDate", query.getEndedAfter(), ">=", prms);
        wh += getCalendarPredicate("endDate", query.getEndedBefore(), "<=", prms);
        wh += getStatusPredicate("status", query.getStatus(), prms);
        if (wh.length() >= 3)
        {
            wh = " WHERE " + wh.substring(3);
        }

        // Order by
        String sort = "";
        for (SortSpec s : query.getSorts())
        {
            sort += ",h." + s.col.getHistoryField() + " " + (s.order == Query.SortOrder.ASCENDING ? "ASC" : "DESC");
        }
        if (sort.isEmpty())
        {
            sort = " ORDER BY h.id";
        }
        else
        {
            sort = " ORDER BY " + sort.substring(1);
        }

        TypedQuery<History> q1 = em.createQuery("SELECT h FROM History h " + wh + sort, History.class);
        for (Map.Entry<String, Object> entry : prms.entrySet())
        {
            q1.setParameter(entry.getKey(), entry.getValue());
        }
        if (query.getFirstRow() != null)
        {
            q1.setFirstResult(query.getFirstRow());
        }
        if (query.getPageSize() != null)
        {
            q1.setMaxResults(query.getPageSize());
        }
        if (query.getFirstRow() != null || query.getPageSize() != null)
        {
            TypedQuery<Long> qCount = em.createQuery("SELECT COUNT(h) FROM History h " + wh, Long.class);
            for (Map.Entry<String, Object> entry : prms.entrySet())
            {
                qCount.setParameter(entry.getKey(), entry.getValue());
            }
            query.setResultSize(new BigDecimal(qCount.getSingleResult()).intValueExact());
        }

        for (History ji : q1.getResultList())
        {
            res2.add(getJob(ji));
        }

        em.close();
        query.setResults(res2);
        return res2;
    }

    @Override
    public com.enioka.jqm.api.JobInstance getJob(int idJob)
    {
        EntityManager em = getEm();
        History h = em.find(History.class, idJob);
        com.enioka.jqm.api.JobInstance res = null;
        if (h != null)
        {
            res = getJob(h);
        }
        else
        {
            JobInstance ji = em.find(JobInstance.class, idJob);
            if (ji != null)
            {
                res = getJob(ji);
            }
            else
            {
                em.close();
                throw new JqmInvalidRequestException("No job instance of ID " + idJob);
            }
        }
        em.close();
        return res;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getJobs()
    {
        ArrayList<com.enioka.jqm.api.JobInstance> jobs = new ArrayList<com.enioka.jqm.api.JobInstance>();
        EntityManager em = getEm();

        try
        {
            for (JobInstance h : em.createQuery("SELECT j FROM JobInstance j ORDER BY j.id", JobInstance.class).getResultList())
            {
                jobs.add(getJob(h));
            }
            for (History h : em.createQuery("SELECT j FROM History j ORDER BY j.id", History.class).getResultList())
            {
                jobs.add(getJob(h));
            }
        }
        catch (Exception e)
        {
            throw new JqmClientException("The user cannot be found or an error occured getting his jobs", e);
        }

        em.close();
        return jobs;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getActiveJobs()
    {
        ArrayList<com.enioka.jqm.api.JobInstance> jobs = new ArrayList<com.enioka.jqm.api.JobInstance>();
        EntityManager em = getEm();

        try
        {
            for (JobInstance h : em.createQuery("SELECT j FROM JobInstance j ORDER BY j.id", JobInstance.class).getResultList())
            {
                jobs.add(getJob(h));
            }
        }
        catch (Exception e)
        {
            throw new JqmClientException("The user cannot be found or an error occured getting his jobs", e);
        }

        em.close();
        return jobs;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getUserActiveJobs(String user)
    {
        ArrayList<com.enioka.jqm.api.JobInstance> jobs = new ArrayList<com.enioka.jqm.api.JobInstance>();
        EntityManager em = getEm();

        try
        {
            for (JobInstance h : em.createQuery("SELECT j FROM JobInstance j WHERE j.userName = :u ORDER BY j.id", JobInstance.class)
                    .setParameter("u", user).getResultList())
            {
                jobs.add(getJob(h));
            }
            for (History h : em.createQuery("SELECT j FROM History j WHERE j.userName = :u ORDER BY j.id", History.class)
                    .setParameter("u", user).getResultList())
            {
                jobs.add(getJob(h));
            }
        }
        catch (Exception e)
        {
            throw new JqmClientException("The user cannot be found or an error occured getting his jobs", e);
        }

        em.close();
        return jobs;
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
        ArrayList<Deliverable> deliverables = null;

        try
        {
            deliverables = (ArrayList<Deliverable>) getEm()
                    .createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class).setParameter("idJob", idJob)
                    .getResultList();
        }
        catch (Exception e)
        {
            throw new JqmClientException("Deliverables cannot be found", e);
        }

        List<com.enioka.jqm.api.Deliverable> res = new ArrayList<com.enioka.jqm.api.Deliverable>();

        for (Deliverable d : deliverables)
        {
            res.add(new com.enioka.jqm.api.Deliverable(d.getFilePath(), d.getFileFamily(), d.getId(), d.getOriginalFileName()));
        }

        return res;
    }

    @Override
    public List<InputStream> getJobDeliverablesContent(int idJob)
    {
        EntityManager em = getEm();
        ArrayList<InputStream> streams = new ArrayList<InputStream>();
        List<Deliverable> tmp = null;

        tmp = em.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class).setParameter("idJob", idJob)
                .getResultList();

        for (Deliverable del : tmp)
        {
            streams.add(getDeliverableContent(del));
        }

        em.close();
        return streams;
    }

    @Override
    public InputStream getDeliverableContent(com.enioka.jqm.api.Deliverable d)
    {
        EntityManager em = getEm();
        Deliverable deliverable = null;

        try
        {
            deliverable = em.find(Deliverable.class, d.getId());
            em.close();
        }
        catch (Exception e)
        {
            em.close();
            throw new JqmClientException("Could not get find deliverable description inside DB - your ID may be wrong", e);
        }

        return getDeliverableContent(deliverable);
    }

    // Helper
    private InputStream getDeliverableContent(Deliverable deliverable)
    {
        EntityManager em = getEm();
        URL url = null;
        File file = null;
        History h = null;

        try
        {
            h = em.createQuery("SELECT h FROM History h WHERE h.id = :job", History.class).setParameter("job", deliverable.getJobId())
                    .getSingleResult();
        }
        catch (Exception e)
        {
            h = null;
            em.close();
            throw new JqmClientException("No ended job found with the deliverable ID", e);
        }

        String destDir = System.getProperty("java.io.tmpdir") + "/" + h.getId();
        (new File(destDir)).mkdir();
        jqmlogger.trace("File will be copied into " + destDir);

        try
        {
            url = new URL("http://" + h.getNode().getDns() + ":" + h.getNode().getPort() + "/getfile?file=" + deliverable.getRandomId());
            jqmlogger.trace("URL: " + url.toString());
        }
        catch (MalformedURLException e)
        {
            throw new JqmClientException("URL is not valid " + url, e);
        }

        try
        {
            file = new File(destDir + "/" + h.getId() + deliverable.getOriginalFileName());
            FileUtils.copyURLToFile(url, file);
            jqmlogger.trace("File was downloaded to " + file.getAbsolutePath());
        }
        catch (IOException e)
        {
            throw new JqmClientException("Could not copy the downloaded file", e);
        }
        em.close();

        FileInputStream res = null;
        try
        {
            res = new FileInputStream(file);
        }
        catch (IOException e)
        {
            throw new JqmClientException("File seems not to be present where it should have been downloaded", e);
        }
        return res;
    }

    // /////////////////////////////////////////////////////////////////////
    // Parameters retrieval
    // /////////////////////////////////////////////////////////////////////

    @Override
    public List<com.enioka.jqm.api.Queue> getQueues()
    {
        List<com.enioka.jqm.api.Queue> res = new ArrayList<com.enioka.jqm.api.Queue>();
        EntityManager em = getEm();
        com.enioka.jqm.api.Queue tmp = null;

        for (Queue q : em.createQuery("SELECT q FROM Queue q ORDER BY q.name", Queue.class).getResultList())
        {
            tmp = getQueue(q);
            res.add(tmp);
        }

        return res;
    }

    private static com.enioka.jqm.api.Queue getQueue(Queue queue)
    {
        com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

        q.setDescription(queue.getDescription());
        q.setId(queue.getId());
        q.setName(queue.getName());

        return q;
    }

    private static JobParameter createJobParameter(String key, String value, EntityManager em)
    {
        JobParameter j = new JobParameter();

        j.setKey(key);
        j.setValue(value);

        em.persist(j);
        return j;
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
        EntityManager em = getEm();
        List<JobDef> dbr = null;
        if (application == null)
        {
            dbr = em.createQuery("SELECT jd from JobDef jd ORDER BY jd.application, jd.module, jd.applicationName", JobDef.class)
                    .getResultList();
        }
        else
        {
            dbr = em.createQuery(
                    "SELECT jd from JobDef jd WHERE jd.application = :name ORDER BY jd.application, jd.module, jd.applicationName",
                    JobDef.class).setParameter("name", application).getResultList();
        }

        for (JobDef jd : dbr)
        {
            res.add(getJobDef(jd));
        }
        em.close();
        return res;
    }

    private static com.enioka.jqm.api.JobDef getJobDef(JobDef jd)
    {
        com.enioka.jqm.api.JobDef res = new com.enioka.jqm.api.JobDef();
        res.setApplication(jd.getApplication());
        res.setApplicationName(jd.getApplicationName());
        res.setCanBeRestarted(jd.isCanBeRestarted());
        res.setDescription(jd.getDescription());
        res.setHighlander(jd.isHighlander());
        res.setKeyword1(jd.getKeyword1());
        res.setKeyword2(jd.getKeyword2());
        res.setKeyword3(jd.getKeyword3());
        res.setModule(jd.getModule());
        res.setQueue(getQueue(jd.getQueue()));
        res.setId(jd.getId());
        return res;
    }
}
