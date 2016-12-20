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
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import javax.naming.NameNotFoundException;
import javax.net.ssl.SSLContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

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

import com.enioka.jqm.api.Query.SortSpec;
import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Node;
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
    private EntityManagerFactory emf = null;
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
            emf = (EntityManagerFactory) p.get("emf");
        }
    }

    private EntityManagerFactory createFactory()
    {
        jqmlogger.debug("Creating connection pool to database");

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

    private void closeQuietly(EntityManager em)
    {
        try
        {
            if (em != null)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        catch (Exception e)
        {
            // fail silently
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
            job = em.createNamedQuery("HibApi.findJobDef", JobDef.class).setParameter("applicationName", jd.getApplicationName())
                    .getSingleResult();
        }
        catch (NoResultException ex)
        {
            jqmlogger.error("Job definition named " + jd.getApplicationName() + " does not exist");
            closeQuietly(em);
            throw new JqmInvalidRequestException("no job definition named " + jd.getApplicationName());
        }

        jqmlogger.trace("Job to enqueue is from JobDef " + job.getId());
        Integer hl = null;
        List<RuntimeParameter> jps = overrideParameter(job, jd, em);

        // Begin transaction (that will hold a lock in case of Highlander)
        try
        {
            em.getTransaction().begin();

            if (job.isHighlander())
            {
                hl = highlanderMode(job, em);
            }

            if (hl != null)
            {
                jqmlogger.trace("JI won't actually be enqueued because a job in highlander mode is currently submitted: " + hl);
                closeQuietly(em);
                return hl;
            }
            jqmlogger.trace("Not in highlander mode or no currently enqueued instance");
        }
        catch (Exception e)
        {
            closeQuietly(em);
            throw new JqmClientException("Could not do highlander analysis", e);
        }

        try
        {
            Queue q = job.getQueue();
            if (jd.getQueueName() != null)
            {
                q = em.createNamedQuery("HibApi.findQueue", Queue.class).setParameter("name", jd.getQueueName()).getSingleResult();
            }

            JobInstance ji = new JobInstance();
            ji.setJd(job);

            ji.setState(State.SUBMITTED);
            ji.setQueue(q);
            ji.setNode(null);
            ji.setApplication(jd.getApplication());
            ji.setEmail(jd.getEmail());
            ji.setKeyword1(jd.getKeyword1());
            ji.setKeyword2(jd.getKeyword2());
            ji.setKeyword3(jd.getKeyword3());
            ji.setModule(jd.getModule());
            ji.setProgress(0);
            ji.setSessionID(jd.getSessionID());
            ji.setUserName(jd.getUser());

            ji.setCreationDate(Calendar.getInstance());
            if (jd.getParentID() != null)
            {
                ji.setParentId(jd.getParentID());
            }
            em.persist(ji);

            // There is sadly no portable and easy way to get DB time before insert... so we update afterwards.
            // Also updates the internal queue position marker (done in update and not setter to avoid full stupid JPA update).
            em.createNamedQuery("HibApi.updateJiWithDbTime").setParameter("i", ji.getId()).executeUpdate();

            for (RuntimeParameter jp : jps)
            {
                jqmlogger.trace("Parameter: " + jp.getKey() + " - " + jp.getValue());
                em.persist(ji.addParameter(jp.getKey(), jp.getValue()));
            }

            jqmlogger.trace("JI just created: " + ji.getId());
            em.getTransaction().commit();
            return ji.getId();
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
            closeQuietly(em);
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
        EntityManager em = null;
        History h = null;
        try
        {
            em = getEm();
            h = em.find(History.class, jobIdToCopy);
            return enqueue(getJobRequest(h, em));
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("No job for this ID in the history");
        }
        finally
        {
            closeQuietly(em);
        }
    }

    // Helper
    private List<RuntimeParameter> overrideParameter(JobDef jdef, JobRequest jdefinition, EntityManager em)
    {
        List<RuntimeParameter> res = new ArrayList<RuntimeParameter>();
        Map<String, String> resm = new HashMap<String, String>();

        // 1st: default parameters
        for (JobDefParameter jp : jdef.getParameters())
        {
            resm.put(jp.getKey(), jp.getValue());
        }

        // 2nd: overloads inside the user enqueue form.
        resm.putAll(jdefinition.getParameters());

        // 3rd: create the RuntimeParameter objects
        for (Entry<String, String> e : resm.entrySet())
        {
            if (e.getValue() == null)
            {
                throw new JqmInvalidRequestException("Parameter " + e.getKey() + " is null which is forbidden");
            }
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
    private JobRequest getJobRequest(History h, EntityManager em)
    {
        JobRequest jd = new JobRequest();
        jd.setApplication(h.getApplication());
        jd.setApplicationName(h.getApplicationName());
        jd.setEmail(h.getEmail());
        jd.setKeyword1(h.getKeyword1());
        jd.setKeyword2(h.getKeyword2());
        jd.setKeyword3(h.getKeyword3());
        jd.setModule(h.getModule());
        jd.setParentID(h.getParentJobId());
        jd.setSessionID(h.getSessionId());
        jd.setUser(h.getUserName());

        for (RuntimeParameter p : em.createQuery("SELECT p FROM RuntimeParameter p WHERE p.ji = :i", RuntimeParameter.class)
                .setParameter("i", h.getId()).getResultList())
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
        EntityManager em = null;
        JobInstance ji = null;
        try
        {
            em = getEm();
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
            closeQuietly(em);
            throw new JqmClientException("the job is already running, has already finished or never existed to begin with");
        }

        try
        {
            em.getTransaction().begin();
            History h = new History();
            h.setId(ji.getId());
            h.setJd(ji.getJd());
            h.setApplicationName(ji.getJd().getApplicationName());
            h.setSessionId(ji.getSessionID());
            h.setQueue(ji.getQueue());
            h.setQueueName(ji.getQueue().getName());
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
            h.setStatus(State.CANCELLED);
            h.setNode(ji.getNode());
            if (ji.getNode() != null)
            {
                h.setNodeName(ji.getNode().getName());
            }
            em.persist(h);

            em.createQuery("DELETE FROM JobInstance WHERE id = :i").setParameter("i", ji.getId()).executeUpdate();
            em.getTransaction().commit();
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not cancel job instance", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    @Override
    public void deleteJob(int idJob)
    {
        jqmlogger.trace("Job status number " + idJob + " will be deleted");
        EntityManager em = null;

        try
        {
            em = getEm();

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
            em.createQuery("DELETE FROM Message WHERE ji = :i").setParameter("i", job.getId()).executeUpdate();
            em.createQuery("DELETE FROM RuntimeParameter WHERE ji = :i").setParameter("i", job.getId()).executeUpdate();
            em.createQuery("DELETE FROM JobInstance WHERE id = :i").setParameter("i", job.getId()).executeUpdate();
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("An attempt was made to delete a job instance that did not exist.");
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not delete a job (internal error)", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    @Override
    public void killJob(int idJob)
    {
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

        EntityManager em = null;
        try
        {
            em = getEm();
            em.getTransaction().begin();
            JobInstance j = em.find(JobInstance.class, idJob, LockModeType.PESSIMISTIC_READ);
            if (j == null)
            {
                throw new NoResultException("Job instance does not exist or has already finished");
            }
            jqmlogger.trace("The " + j.getState() + " job (ID: " + idJob + ")" + " will be marked for kill");

            j.setState(State.KILLED);

            Message m = new Message();
            m.setJi(idJob);
            m.setTextMessage("Kill attempt on the job");
            em.persist(m);
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("An attempt was made to kill a job instance that did not exist.");
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not kill a job (internal error)", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Job Pause/restart
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void pauseQueuedJob(int idJob)
    {
        jqmlogger.trace("Job status number " + idJob + " will be set to HOLDED");
        EntityManager em = null;

        try
        {
            em = getEm();
            em.getTransaction().begin();
            em.createQuery("UPDATE JobInstance j SET j.state = 'HOLDED' WHERE j.id = :idJob").setParameter("idJob", idJob).executeUpdate();
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("An attempt was made to pause a job instance that did not exist.");
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not pause a job (internal error)", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    @Override
    public void resumeJob(int idJob)
    {
        jqmlogger.trace("Job status number " + idJob + " will be resumed");
        EntityManager em = null;

        try
        {
            em = getEm();
            em.getTransaction().begin();
            em.createQuery("UPDATE JobInstance j SET j.state = 'SUBMITTED' WHERE j.id = :idJob").setParameter("idJob", idJob)
                    .executeUpdate();
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            throw new JqmInvalidRequestException("An attempt was made to resume a job instance that did not exist.");
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not resume a job (internal error)", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    public int restartCrashedJob(int idJob)
    {
        EntityManager em = null;

        // History and Job ID have the same ID.
        History h = null;
        try
        {
            em = getEm();
            h = em.find(History.class, idJob);
        }
        catch (NoResultException e)
        {
            closeQuietly(em);
            throw new JqmClientException("You cannot restart a job that is not done or which was purged from history");
        }
        catch (Exception e)
        {
            closeQuietly(em);
            throw new JqmClientException("could not restart a job (internal error)", e);
        }

        if (!h.getState().equals(State.CRASHED))
        {
            closeQuietly(em);
            throw new JqmClientException("You cannot restart a job that has not crashed");
        }

        if (!h.getJd().isCanBeRestarted())
        {
            closeQuietly(em);
            throw new JqmClientException("This type of job was configured to prevent being restarded");
        }

        try
        {
            em.getTransaction().begin();
            em.remove(h);
            em.getTransaction().commit();
            return enqueue(getJobRequest(h, em));
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not purge & restart a job (internal error)", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Misc.
    // /////////////////////////////////////////////////////////////////////

    @Override
    public void setJobQueue(int idJob, int idQueue)
    {
        EntityManager em = null;
        JobInstance ji = null;
        Queue q = null;

        try
        {
            em = getEm();
            q = em.find(Queue.class, idQueue);
        }
        catch (NoResultException e)
        {
            closeQuietly(em);
            throw new JqmClientException("Queue does not exist");
        }
        catch (Exception e)
        {
            closeQuietly(em);
            throw new JqmClientException("Cannot retrieve queue", e);
        }

        try
        {
            em.getTransaction().begin();
            ji = em.find(JobInstance.class, idJob, LockModeType.PESSIMISTIC_WRITE);
            if (ji == null || !ji.getState().equals(State.SUBMITTED))
            {
                throw new NoResultException();
            }
            ji.setQueue(q);
            em.getTransaction().commit();
        }
        catch (NoResultException e)
        {
            throw new JqmClientException("Job instance does not exist or has already started");
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not change the queue of a job (internal error)", e);
        }
        finally
        {
            closeQuietly(em);
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
        EntityManager em = null;
        JobInstance ji = null;
        try
        {
            em = getEm();
            em.getTransaction().begin();
            ji = em.find(JobInstance.class, idJob, LockModeType.PESSIMISTIC_WRITE);
        }
        catch (Exception e)
        {
            closeQuietly(em);
            throw new JqmClientException(
                    "Could not lock a job by the given ID. It may already have been executed or a timeout may have occurred.", e);
        }

        if (!ji.getState().equals(State.SUBMITTED))
        {
            closeQuietly(em);
            throw new JqmInvalidRequestException("Job is already set for execution. Too late to change its position in the queue");
        }

        try
        {
            int current = ji.getCurrentPosition(em);
            int betweenUp = 0;
            int betweenDown = 0;

            if (current == position)
            {
                // Nothing to do
                em.getTransaction().rollback();
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
            List<JobInstance> currentJobs = em.createQuery("SELECT ji from JobInstance ji ORDER BY ji.internalPosition", JobInstance.class)
                    .setMaxResults(betweenUp).getResultList();

            if (currentJobs.isEmpty())
            {
                ji.setInternalPosition(0);
            }
            else if (currentJobs.size() < betweenUp)
            {
                ji.setInternalPosition(currentJobs.get(currentJobs.size() - 1).getInternalPosition() + 0.00001);
            }
            else
            {
                // Normal case: put the JI between the two others.
                ji.setInternalPosition(
                        (currentJobs.get(betweenUp - 1).getInternalPosition() + currentJobs.get(betweenDown - 1).getInternalPosition())
                                / 2);
            }
            em.getTransaction().commit();
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not change the queue position of a job (internal error)", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Job queries
    // /////////////////////////////////////////////////////////////////////

    // Helper
    private com.enioka.jqm.api.JobInstance getJob(JobInstance h, EntityManager em)
    {
        com.enioka.jqm.api.JobInstance ji = new com.enioka.jqm.api.JobInstance();
        ji.setId(h.getId());
        ji.setApplicationName(h.getJd().getApplicationName());
        ji.setParameters(new HashMap<String, String>());
        ji.setParent(h.getParentId());
        ji.setQueue(getQueue(h.getQueue()));
        ji.setQueueName(h.getQueue().getName());
        ji.setSessionID(h.getSessionID());
        ji.setState(com.enioka.jqm.api.State.valueOf(h.getState().toString()));
        ji.setUser(h.getUserName());
        ji.setProgress(h.getProgress());
        for (RuntimeParameter p : em.createQuery("SELECT m from RuntimeParameter m where m.ji = :i", RuntimeParameter.class)
                .setParameter("i", h.getId()).getResultList())
        {
            ji.getParameters().put(p.getKey(), p.getValue());
        }
        for (Message m : em.createQuery("SELECT m from Message m where m.ji = :i", Message.class).setParameter("i", h.getId())
                .getResultList())
        {
            ji.getMessages().add(m.getTextMessage());
        }
        ji.setKeyword1(h.getKeyword1());
        ji.setKeyword2(h.getKeyword2());
        ji.setKeyword3(h.getKeyword3());
        ji.setDefinitionKeyword1(h.getJd().getKeyword1());
        ji.setDefinitionKeyword2(h.getJd().getKeyword2());
        ji.setDefinitionKeyword3(h.getJd().getKeyword3());
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
    private com.enioka.jqm.api.JobInstance getJob(History h, EntityManager em)
    {
        return getJob(h, em, null, null);
    }

    private com.enioka.jqm.api.JobInstance getJob(History h, EntityManager em, List<RuntimeParameter> rps, List<Message> msgs)
    {
        com.enioka.jqm.api.JobInstance ji = new com.enioka.jqm.api.JobInstance();
        ji.setId(h.getId());
        ji.setApplicationName(h.getApplicationName());
        ji.setParameters(new HashMap<String, String>());
        ji.setParent(h.getParentJobId());
        if (h.getQueue() != null)
        {
            ji.setQueue(getQueue(h.getQueue()));
        }
        ji.setQueueName(h.getQueueName());
        ji.setSessionID(h.getSessionId());
        ji.setState(com.enioka.jqm.api.State.valueOf(h.getStatus().toString()));
        ji.setUser(h.getUserName());
        ji.setProgress(h.getProgress());
        ji.setKeyword1(h.getInstanceKeyword1());
        ji.setKeyword2(h.getInstanceKeyword2());
        ji.setKeyword3(h.getInstanceKeyword3());
        ji.setDefinitionKeyword1(h.getKeyword1());
        ji.setDefinitionKeyword2(h.getKeyword2());
        ji.setDefinitionKeyword3(h.getKeyword3());
        ji.setApplication(h.getApplication());
        ji.setModule(h.getModule());
        ji.setEmail(h.getEmail());
        ji.setEnqueueDate(h.getEnqueueDate());
        ji.setBeganRunningDate(h.getExecutionDate());
        ji.setEndDate(h.getEndDate());
        ji.setNodeName(h.getNodeName());

        if (rps == null)
        {
            for (RuntimeParameter p : em.createQuery("SELECT m from RuntimeParameter m where m.ji = :i", RuntimeParameter.class)
                    .setParameter("i", h.getId()).getResultList())
            {
                ji.getParameters().put(p.getKey(), p.getValue());
            }
        }
        else
        {
            for (RuntimeParameter rp : rps)
            {
                if (rp.getJi() == h.getId())
                {
                    ji.getParameters().put(rp.getKey(), rp.getValue());
                }
            }
        }

        if (msgs == null)
        {
            for (Message m : em.createQuery("SELECT m from Message m where m.ji = :i", Message.class).setParameter("i", h.getId())
                    .getResultList())
            {
                ji.getMessages().add(m.getTextMessage());
            }
        }
        else
        {
            for (Message msg : msgs)
            {
                if (msg.getJi() == h.getId())
                {
                    ji.getMessages().add(msg.getTextMessage());
                }
            }
        }

        return ji;
    }

    private String getStringPredicate(String fieldName, String filterValue, Map<String, Object> prms)
    {
        if (filterValue == null)
        {
            return "";
        }
        return getStringPredicate(fieldName, Arrays.asList(filterValue), prms);
    }

    // GetJob helper - String predicates are all created the same way, so this factors some code.
    private String getStringPredicate(String fieldName, List<String> filterValues, Map<String, Object> prms)
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
                    prms.put(prmName, filterValue);
                    if (filterValue.contains("%"))
                    {
                        res += String.format("(h.%s LIKE :%s) OR ", fieldName, prmName);
                    }
                    else
                    {
                        res += String.format("(h.%s = :%s) OR ", fieldName, prmName);
                    }
                }
                else
                {
                    res += String.format("(h.%s IS NULL OR h.%s = '') OR ", fieldName, fieldName);
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

    private String getIntPredicate(String fieldName, Integer filterValue, Map<String, Object> prms)
    {
        if (filterValue != null)
        {
            if (filterValue != -1)
            {
                String prmName = fieldName.split("\\.")[fieldName.split("\\.").length - 1];
                prms.put(prmName, filterValue);
                return String.format("AND (h.%s = :%s) ", fieldName, prmName);
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
            return String.format("AND (h.%s %s :%s) ", fieldName, comparison, prmName);
        }
        else
        {
            return "";
        }
    }

    private String getStatusPredicate(String fieldName, List<com.enioka.jqm.api.State> status, Map<String, Object> prms)
    {
        if (status == null || status.isEmpty())
        {
            return "";
        }

        String res = String.format("AND ( h.%s IN ( ", fieldName);

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
        if ((query.getFirstRow() != null || query.getPageSize() != null) && query.isQueryLiveInstances() && query.isQueryHistoryInstances())
        {
            throw new JqmInvalidRequestException("cannot use paging when querying both live and historical instances");
        }
        if (query.isQueryLiveInstances() && query.isQueryHistoryInstances() && query.getSorts().size() > 0)
        {
            throw new JqmInvalidRequestException("cannot use sorting when querying both live and historical instances");
        }

        EntityManager em = null;
        try
        {
            em = getEm();

            // Not using CriteriaBuilder - too much hassle for too little benefit
            String wh = "";
            Map<String, Object> prms = new HashMap<String, Object>();

            // String predicates
            wh += getStringPredicate("userName", query.getUser(), prms);
            wh += getStringPredicate("sessionId", query.getSessionId(), prms);
            wh += getStringPredicate("instanceKeyword1", query.getInstanceKeyword1(), prms);
            wh += getStringPredicate("instanceKeyword2", query.getInstanceKeyword2(), prms);
            wh += getStringPredicate("instanceKeyword3", query.getInstanceKeyword3(), prms);
            wh += getStringPredicate("instanceModule", query.getInstanceModule(), prms);
            wh += getStringPredicate("instanceApplication", query.getInstanceApplication(), prms);

            // Integer
            wh += getIntPredicate("parentId", query.getParentId(), prms);
            wh += getIntPredicate("id", query.getJobInstanceId(), prms);
            wh += getIntPredicate("queue.id", query.getQueueId() == null ? null : query.getQueueId(), prms);

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
                    sort += s.col.getJiField() == null ? ""
                            : ",h." + s.col.getJiField() + " " + (s.order == Query.SortOrder.ASCENDING ? "ASC" : "DESC");
                }
                if (sort.isEmpty())
                {
                    sort = " ORDER BY h.id";
                }
                else
                {
                    sort = " ORDER BY " + sort.substring(1);
                }

                // Finish query string
                String wh2 = "" + wh;
                Map<String, Object> prms2 = new HashMap<String, Object>();
                prms2.putAll(prms);
                wh2 += getStringPredicate("queue.name", query.getQueueName(), prms2);

                // tag fields should be looked for in linked object for active JI
                wh2 += getStringPredicate("jd.applicationName", query.getApplicationName(), prms2);
                wh2 += getStringPredicate("jd.keyword1", query.getJobDefKeyword1(), prms2);
                wh2 += getStringPredicate("jd.keyword2", query.getJobDefKeyword2(), prms2);
                wh2 += getStringPredicate("jd.keyword3", query.getJobDefKeyword3(), prms2);
                wh2 += getStringPredicate("jd.module", query.getJobDefModule(), prms2);
                wh2 += getStringPredicate("jd.application", query.getJobDefApplication(), prms2);
                wh2 += getStringPredicate("node.name", query.getNodeName(), prms2);

                // Calendar fields are specific (no common fields between History and JobInstance)
                wh2 += getCalendarPredicate("creationDate", query.getEnqueuedAfter(), ">=", prms2);
                wh2 += getCalendarPredicate("creationDate", query.getEnqueuedBefore(), "<=", prms2);
                wh2 += getCalendarPredicate("executionDate", query.getBeganRunningAfter(), ">=", prms2);
                wh2 += getCalendarPredicate("executionDate", query.getBeganRunningBefore(), "<=", prms2);
                wh2 += getStatusPredicate("state", query.getStatus(), prms2);
                if (wh2.length() >= 3)
                {
                    wh2 = " WHERE " + wh2.substring(3);
                }

                TypedQuery<JobInstance> q2 = em.createQuery(
                        "SELECT h FROM JobInstance h LEFT JOIN FETCH h.jd LEFT JOIN FETCH h.node " + wh2 + sort, JobInstance.class);
                for (Map.Entry<String, Object> entry : prms2.entrySet())
                {
                    q2.setParameter(entry.getKey(), entry.getValue());
                }

                // Set pagination parameters
                if (query.getFirstRow() != null)
                {
                    q2.setFirstResult(query.getFirstRow());
                }
                if (query.getPageSize() != null)
                {
                    q2.setMaxResults(query.getPageSize());
                }

                // Run the query
                for (JobInstance ji : q2.getResultList())
                {
                    res2.add(getJob(ji, em));
                }

                // If needed, fetch the total result count (without pagination). Note that without pagination, the Query object does not
                // need this indication.
                if (query.getFirstRow() != null || (query.getPageSize() != null && res2.size() >= query.getPageSize()))
                {
                    TypedQuery<Long> qCount = em.createQuery("SELECT COUNT(h) FROM JobInstance h " + wh2, Long.class);
                    for (Map.Entry<String, Object> entry : prms2.entrySet())
                    {
                        qCount.setParameter(entry.getKey(), entry.getValue());
                    }
                    query.setResultSize(new BigDecimal(qCount.getSingleResult()).intValueExact());
                }
            }

            // ////////////////////////////////////////
            // History query
            if (query.isQueryHistoryInstances())
            {
                wh += getStringPredicate("queueName", query.getQueueName(), prms);

                // tag fields should be looked directly in the denormalized fields for history.
                wh += getStringPredicate("applicationName", query.getApplicationName(), prms);
                wh += getStringPredicate("keyword1", query.getJobDefKeyword1(), prms);
                wh += getStringPredicate("keyword2", query.getJobDefKeyword2(), prms);
                wh += getStringPredicate("keyword3", query.getJobDefKeyword3(), prms);
                wh += getStringPredicate("module", query.getJobDefModule(), prms);
                wh += getStringPredicate("application", query.getJobDefApplication(), prms);
                wh += getStringPredicate("nodeName", query.getNodeName(), prms);

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

                TypedQuery<History> q1 = em.createQuery(
                        "SELECT h FROM History h LEFT JOIN FETCH h.jd LEFT JOIN FETCH h.node LEFT JOIN FETCH h.queue " + wh + sort,
                        History.class);
                for (Map.Entry<String, Object> entry : prms.entrySet())
                {
                    q1.setParameter(entry.getKey(), entry.getValue());
                }

                // Set pagination parameters
                if (query.getFirstRow() != null)
                {
                    q1.setFirstResult(query.getFirstRow());
                }
                if (query.getPageSize() != null)
                {
                    q1.setMaxResults(query.getPageSize());
                }

                // Actually run the query
                List<History> results = q1.getResultList();

                // If needed, fetch the total result count (without pagination). Note that without pagination, the Query object does not
                // need this indication.
                if (query.getFirstRow() != null || (query.getPageSize() != null && results.size() >= query.getPageSize()))
                {
                    TypedQuery<Long> qCount = em.createQuery("SELECT COUNT(h) FROM History h " + wh, Long.class);
                    for (Map.Entry<String, Object> entry : prms.entrySet())
                    {
                        qCount.setParameter(entry.getKey(), entry.getValue());
                    }
                    query.setResultSize(new BigDecimal(qCount.getSingleResult()).intValueExact());
                }

                // Optimization: fetch messages and parameters in batches of 50 (limit accepted by most databases for IN clauses).
                List<List<Integer>> ids = new ArrayList<List<Integer>>();
                List<Integer> currentList = null;
                int i = 0;
                for (History ji : results)
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
                        rps.addAll(em.createQuery("SELECT rp FROM RuntimeParameter rp WHERE rp.ji IN (:p)", RuntimeParameter.class)
                                .setParameter("p", idsBatch).getResultList());
                        msgs.addAll(em.createQuery("SELECT rp FROM Message rp WHERE rp.ji IN (:p)", Message.class)
                                .setParameter("p", idsBatch).getResultList());
                    }

                    for (History ji : results)
                    {
                        // This is the actual JPA -> DTO work.
                        res2.add(getJob(ji, em, rps, msgs));
                    }
                }
            }

            query.setResults(res2);
            return res2;
        }
        catch (Exception e)
        {
            throw new JqmClientException("an error occured during query execution", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    @Override
    public com.enioka.jqm.api.JobInstance getJob(int idJob)
    {
        EntityManager em = null;
        try
        {
            // Three steps: first, query History as:
            // * this is supposed to be the most frequent query.
            // * we try to avoid hitting the queues if possible
            // Second, query live queues
            // Third, query history again (because a JI may have ended between the first two queries, so we may miss a JI)
            // Outside this case, this third query will be very rare, as the method is always called with an ID that cannot be
            // guessed as its only parameter, so the existence of the JI is nearly always a given.
            em = getEm();
            History h = em.find(History.class, idJob);
            com.enioka.jqm.api.JobInstance res = null;
            if (h != null)
            {
                res = getJob(h, em);
            }
            else
            {
                JobInstance ji = em.find(JobInstance.class, idJob);
                if (ji != null)
                {
                    res = getJob(ji, em);
                }
                else
                {
                    h = em.find(History.class, idJob);
                    if (h != null)
                    {
                        res = getJob(h, em);
                    }
                    else
                    {
                        throw new JqmInvalidRequestException("No job instance of ID " + idJob);
                    }
                }
            }
            return res;
        }
        catch (JqmInvalidRequestException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JqmClientException("an error occured during query execution", e);
        }
        finally
        {
            closeQuietly(em);
        }
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getJobs()
    {
        ArrayList<com.enioka.jqm.api.JobInstance> jobs = new ArrayList<com.enioka.jqm.api.JobInstance>();
        EntityManager em = null;

        try
        {
            em = getEm();
            for (JobInstance h : em.createQuery("SELECT j FROM JobInstance j ORDER BY j.id", JobInstance.class).getResultList())
            {
                jobs.add(getJob(h, em));
            }
            for (History h : em.createQuery("SELECT j FROM History j ORDER BY j.id", History.class).getResultList())
            {
                jobs.add(getJob(h, em));
            }
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not query history and queues", e);
        }
        finally
        {
            closeQuietly(em);
        }
        return jobs;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getActiveJobs()
    {
        ArrayList<com.enioka.jqm.api.JobInstance> jobs = new ArrayList<com.enioka.jqm.api.JobInstance>();
        EntityManager em = null;

        try
        {
            em = getEm();
            for (JobInstance h : em.createQuery("SELECT j FROM JobInstance j ORDER BY j.id", JobInstance.class).getResultList())
            {
                jobs.add(getJob(h, em));
            }
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not query queues", e);
        }
        finally
        {
            closeQuietly(em);
        }
        return jobs;
    }

    @Override
    public List<com.enioka.jqm.api.JobInstance> getUserActiveJobs(String user)
    {
        if (user == null || user.isEmpty())
        {
            throw new JqmInvalidRequestException("user cannot be null or empty");
        }
        ArrayList<com.enioka.jqm.api.JobInstance> jobs = new ArrayList<com.enioka.jqm.api.JobInstance>();
        EntityManager em = null;

        try
        {
            em = getEm();
            for (JobInstance h : em.createQuery("SELECT j FROM JobInstance j WHERE j.userName = :u ORDER BY j.id", JobInstance.class)
                    .setParameter("u", user).getResultList())
            {
                jobs.add(getJob(h, em));
            }
            for (History h : em.createQuery("SELECT j FROM History j WHERE j.userName = :u ORDER BY j.id", History.class)
                    .setParameter("u", user).getResultList())
            {
                jobs.add(getJob(h, em));
            }
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not query both queues and history for job instances of user " + user, e);
        }
        finally
        {
            closeQuietly(em);
        }
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
        List<Deliverable> deliverables = null;
        EntityManager em = null;

        try
        {
            em = getEm();
            deliverables = em.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class)
                    .setParameter("idJob", idJob).getResultList();
        }
        catch (Exception e)
        {
            throw new JqmClientException("Deliverables cannot be found", e);
        }
        finally
        {
            closeQuietly(em);
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
        EntityManager em = null;
        ArrayList<InputStream> streams = new ArrayList<InputStream>();
        List<Deliverable> tmp = null;

        try
        {
            em = getEm();
            tmp = em.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class).setParameter("idJob", idJob)
                    .getResultList();

            for (Deliverable del : tmp)
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
            closeQuietly(em);
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
        EntityManager em = null;
        Deliverable deliverable = null;

        try
        {
            em = getEm();
            deliverable = em.find(Deliverable.class, delId);
        }
        catch (Exception e)
        {
            throw new JqmInvalidRequestException("Could not get find deliverable description inside DB - your ID may be wrong", e);
        }
        finally
        {
            closeQuietly(em);
        }

        return getDeliverableContent(deliverable);
    }

    InputStream getEngineLog(String nodeName, int latest)
    {
        EntityManager em = getEm();
        URL url = null;

        try
        {
            Node h = em.createQuery("SELECT n FROM Node n WHERE n.name = :n", Node.class).setParameter("n", nodeName).getSingleResult();
            url = new URL(getFileProtocol(em) + h.getDns() + ":" + h.getPort() + "/ws/simple/enginelog?latest=" + latest);
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
            closeQuietly(em);
        }

        return getFile(url.toString());
    }

    // Helper
    private InputStream getDeliverableContent(Deliverable deliverable)
    {
        EntityManager em = getEm();
        URL url = null;
        String dns, protocol;
        int port;

        try
        {
            History h = em.find(History.class, deliverable.getJobId());
            if (h == null)
            {
                JobInstance ji = em.find(JobInstance.class, deliverable.getJobId());
                if (ji == null)
                {
                    throw new JqmInvalidRequestException("No ended or running job instance found for this file");
                }
                dns = ji.getNode().getDns();
                port = ji.getNode().getPort();
            }
            else
            {
                if (h.getNode() == null)
                {
                    throw new JqmInvalidRequestException("cannot retrieve a file from a deleted node");
                }
                dns = h.getNode().getDns();
                port = h.getNode().getPort();
            }

            protocol = getFileProtocol(em);
        }
        catch (Exception e)
        {
            throw new JqmClientException("Could not process request", e);
        }
        finally
        {
            closeQuietly(em);
        }

        try
        {
            url = new URL(protocol + dns + ":" + port + "/ws/simple/file?id=" + deliverable.getRandomId());
            jqmlogger.trace("URL: " + url.toString());
        }
        catch (MalformedURLException e)
        {
            throw new JqmClientException("URL is not valid " + url, e);
        }

        return getFile(url.toString());
    }

    private String getFileProtocol(EntityManager em)
    {
        if (protocol == null)
        {
            protocol = "http://";
            try
            {
                GlobalParameter gp = em
                        .createQuery("SELECT gp from GlobalParameter gp WHERE gp.key = 'enableWsApiSsl'", GlobalParameter.class)
                        .getSingleResult();
                if (Boolean.parseBoolean(gp.getValue()))
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
        EntityManager em = getEm();
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
            if (SimpleApiSecurity.getId(em).usr != null)
            {
                credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(SimpleApiSecurity.getId(em).usr, SimpleApiSecurity.getId(em).pass));
            }
            SSLContext ctx = null;
            if (getFileProtocol(em).equals("https://"))
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
            closeQuietly(em);
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

    private InputStream getJobLog(int jobId, String extension, String param)
    {
        // 1: retrieve node to address
        EntityManager em = null;
        Node n = null;
        try
        {
            em = getEm();
            History h = em.find(History.class, jobId);
            if (h != null)
            {
                n = h.getNode();
            }
            else
            {
                JobInstance ji = em.find(JobInstance.class, jobId);
                if (ji != null)
                {
                    n = ji.getNode();
                }
                else
                {
                    throw new NoResultException("No history or running instance for this jobId.");
                }
            }
        }
        catch (Exception e)
        {
            closeQuietly(em);
            throw new JqmInvalidRequestException("No job found with the job ID " + jobId, e);
        }

        if (n == null)
        {
            throw new JqmInvalidRequestException("cannot retrieve a file from a deleted node");
        }

        // 2: build URL
        URL url = null;
        try
        {
            url = new URL(getFileProtocol(em) + n.getDns() + ":" + n.getPort() + "/ws/simple/" + param + "?id=" + jobId);
            jqmlogger.trace("URL: " + url.toString());
        }
        catch (MalformedURLException e)
        {
            throw new JqmClientException("URL is not valid " + url, e);
        }
        finally
        {
            em.close();
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
        EntityManager em = null;
        com.enioka.jqm.api.Queue tmp = null;

        try
        {
            em = getEm();
            for (Queue q : em.createQuery("SELECT q FROM Queue q ORDER BY q.name", Queue.class).getResultList())
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
            closeQuietly(em);
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

    private static RuntimeParameter createJobParameter(String key, String value, EntityManager em)
    {
        RuntimeParameter j = new RuntimeParameter();

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
        EntityManager em = null;
        List<JobDef> dbr = null;

        try
        {
            em = getEm();
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
            return res;
        }
        catch (Exception e)
        {
            throw new JqmClientException("could not query JobDef", e);
        }
        finally
        {
            closeQuietly(em);
        }
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

        for (JobDefParameter jdf : jd.getParameters())
        {
            res.addParameter(jdf.getKey(), jdf.getValue());
        }

        return res;
    }
}
