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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

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
public final class Dispatcher
{
    private static Logger jqmlogger = Logger.getLogger(Dispatcher.class);
    private static final String PERSISTENCE_UNIT = "jobqueue-api-pu";

    private Dispatcher()
    {

    }

    private static EntityManagerFactory emf = null;

    /**
     * For tests only. Never use in production code.
     */
    public static void resetEM()
    {
        if (emf != null)
        {
            emf.close();
        }
        emf = createFactory();
    }

    private static EntityManagerFactory createFactory()
    {
        Properties p = new Properties();
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream("conf/db.properties");
            p.load(fis);
            IOUtils.closeQuietly(fis);
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, p);
        }
        catch (FileNotFoundException e)
        {
            // No properties file means we use the test HSQLDB (file, not in-memory) as specified in the persistence.xml
            IOUtils.closeQuietly(fis);
            try
            {
                jqmlogger.info("New EMF will be created");
                return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            }
            catch (Exception ex)
            {
                jqmlogger.fatal("Could not create EMF", ex);
                return null;
            }
        }
        catch (IOException e)
        {
            jqmlogger.fatal("conf/db.properties file is invalid", e);
            IOUtils.closeQuietly(fis);
            throw new RuntimeException();
            // System.exit(1);
            // Stupid, just for Eclipse's parser and therefore avoid red lines...
            // return null;
        }
    }

    static EntityManager getEm()
    {
        if (emf == null)
        {
            emf = createFactory();
        }

        try
        {
            jqmlogger.debug("A new EM will be created");
            return emf.createEntityManager();
        }
        catch (Exception e)
        {
            jqmlogger.fatal("Could not create EM. Exiting.", e);
            throw new RuntimeException();
            // return null;
        }
    }

    private static com.enioka.jqm.api.JobDefinition jobDefToJobDefinition(JobDef jd)
    {

        com.enioka.jqm.api.JobDefinition job = new com.enioka.jqm.api.JobDefinition();
        Map<String, String> h = new HashMap<String, String>();

        if (jd.getParameters() != null)
        {
            for (JobDefParameter i : jd.getParameters())
            {
                h.put(i.getKey(), i.getValue());
            }
        }

        job.setParameters(h);
        job.setApplicationName(jd.getApplicationName());
        job.setApplication(jd.getApplication());
        job.setModule(jd.getModule());
        job.setKeyword1(jd.getKeyword1());
        job.setKeyword2(jd.getKeyword2());
        job.setKeyword3(jd.getKeyword3());

        return job;
    }

    private static com.enioka.jqm.api.JobInstance getJobInstance(int idJob)
    {
        EntityManager em = getEm();
        try
        {
            return getJobInstance(em.find(History.class, idJob));
        }
        catch (NoResultException e)
        {
            return getJobInstance(em.find(JobInstance.class, idJob));
        }
        finally
        {
            em.close();
        }
    }

    private static com.enioka.jqm.api.JobInstance getJobInstance(History h)
    {
        com.enioka.jqm.api.JobInstance ji = new com.enioka.jqm.api.JobInstance();
        ji.setId(h.getId());
        ji.setParameters(new HashMap<String, String>());
        ji.setParent(h.getParentJobId());
        ji.setQueue(getQueue(h.getQueue()));
        ji.setSessionID(h.getSessionId());
        ji.setState(h.getStatus());
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

        return ji;
    }

    private static com.enioka.jqm.api.JobInstance getJobInstance(JobInstance h)
    {
        com.enioka.jqm.api.JobInstance ji = new com.enioka.jqm.api.JobInstance();
        ji.setId(h.getId());
        ji.setParameters(new HashMap<String, String>());
        ji.setParent(h.getParentId());
        ji.setQueue(getQueue(h.getQueue()));
        ji.setSessionID(h.getSessionID());
        ji.setState(h.getState());
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

        return ji;
    }

    private static JobDefinition getJobDefinition(History h)
    {
        JobDefinition jd = new JobDefinition();
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

    private static com.enioka.jqm.api.Queue getQueue(Queue queue)
    {
        com.enioka.jqm.api.Queue q = new com.enioka.jqm.api.Queue();

        q.setDescription(queue.getDescription());
        q.setId(queue.getId());
        q.setName(queue.getName());

        return q;
    }

    private static List<JobParameter> overrideParameter(JobDef jdef, JobDefinition jdefinition, EntityManager em)
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

    // ----------------------------- ENQUEUE --------------------------------------

    /**
     * Ask for a new job instance, as described in the parameter object
     * 
     * @param jd
     *            the definition of the desired job instance
     * @return
     */
    public static int enQueue(JobDefinition jd)
    {
        jqmlogger.debug("BEGINING ENQUEUE");
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
            throw new JqmException("no such job definition");
        }

        jqmlogger.debug("Job to enqueue is from JobDef " + job.getId());
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
            jqmlogger.debug("JI won't actually be enqueued because a job in highlander mode is currently submitted: " + hl);
            em.getTransaction().rollback();
            em.close();
            return hl;
        }
        jqmlogger.debug("Not in highlander mode or no currently enqueued instance");

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
            jqmlogger.debug("Parameter: " + jp.getKey() + " - " + jp.getValue());
            em.persist(ji.addParameter(jp.getKey(), jp.getValue()));
        }
        jqmlogger.debug("JI just created: " + ji.getId());

        em.getTransaction().commit();
        em.close();
        return ji.getId();
    }

    // ----------------------------- HOLDED ------------------------------------------
    /**
     * Set the job status to HOLDED.
     * 
     * @param idJob
     * @return void
     */
    public static void pauseJob(int idJob)
    {
        jqmlogger.debug("Job status number " + idJob + " will be set to HOLDED");
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
            jqmlogger.debug(e);
        }

        em.getTransaction().commit();

        em.close();
    }

    // ----------------------------- RESUME ------------------------------------------
    /**
     * Resume the holded job.
     * 
     * @param idJob
     * @return void
     */
    public static void resumeJob(int idJob)
    {
        jqmlogger.debug("Job status number " + idJob + " will be resumed");
        EntityManager em = getEm();

        em.getTransaction().begin();

        try
        {
            @SuppressWarnings("unused")
            int q = em.createQuery("UPDATE JobInstance j SET j.state = 'SUBMITTED' WHERE j.id = :idJob").setParameter("idJob", idJob)
                    .executeUpdate();
            @SuppressWarnings("unused")
            int qq = em.createQuery("UPDATE History j SET j.status = 'SUBMITTED' WHERE j.jobInstanceId = :idJob")
                    .setParameter("idJob", idJob).executeUpdate();

        }
        catch (Exception e)
        {
            jqmlogger.debug(e);
        }

        em.getTransaction().commit();

        em.close();
    }

    // ----------------------------- HIGHLANDER --------------------------------------
    // Must be called within an active JPA transaction
    private static Integer highlanderMode(JobDef jd, EntityManager em)
    {
        // Synchronization is done through locking the JobDef
        em.lock(jd, LockModeType.PESSIMISTIC_WRITE);

        // Do the analysis
        Integer res = null;
        jqmlogger.debug("Highlander mode analysis is begining");
        ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) em
                .createQuery("SELECT j FROM JobInstance j WHERE j.jd = :j AND j.state = :s", JobInstance.class).setParameter("j", jd)
                .setParameter("s", State.SUBMITTED).getResultList();

        for (JobInstance j : jobs)
        {
            jqmlogger.debug("JI seen by highlander: " + j.getId() + j.getState());
            if (j.getState().equals(State.SUBMITTED))
            {
                // HIGHLANDER: only one enqueued job can survive!
                // current request must be cancelled and enqueue must return the id of the existing submitted JI
                res = j.getId();
                break;
            }
        }
        jqmlogger.debug("Highlander mode will return: " + res);
        return res;
    }

    // ----------------------------- GETJOB --------------------------------------

    /**
     * Retrieve a job instance. The returned object will contain all relevant data on that job instance such as its status.
     * 
     * @param idJob
     * @return
     */
    public static com.enioka.jqm.api.JobInstance getJob(int idJob)
    {
        try
        {
            return getJobInstance(getEm().find(History.class, idJob));
        }
        catch (NoResultException e)
        {
            return getJobInstance(getEm().find(JobInstance.class, idJob));
        }
    }

    // ----------------------------- DELJOBINQUEUE --------------------------------------

    /**
     * Remove an enqueued job from the queue. Should not be called if the job is already running, but will not throw any exceptions in that
     * case.
     * 
     * @param idJob
     */
    public static void delJobInQueue(int idJob)
    {
        jqmlogger.debug("Job status number " + idJob + " will be deleted");
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
            jqmlogger.info(e);
        }
        finally
        {
            em.close();
        }
    }

    // ----------------------------- CANCELJOBINQUEUE --------------------------------------

    /**
     * Cancel a job without removing it from the queue.
     * 
     * @param idJob
     */
    public static void cancelJobInQueue(int idJob)
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
            throw new JqmException("the job is already running, has already finished or never existed to begin with");
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

    // ----------------------------- STOPJOB --------------------------------------

    /**
     * Nicely stop a running job (asks the job to stop itself - this will usually not do anything as most jobs do not implement this
     * function)
     * 
     * @param idJob
     */
    public static void stopJob(int idJob)
    {

    }

    // ----------------------------- KILLJOB --------------------------------------

    /**
     * Kill a running job. Kill is not immediate, and is only possible when a job calls some JQM APIs. If none are called, the job cannot be
     * killed.
     * 
     * @param idJob
     */
    public static void killJob(int idJob)
    {
        EntityManager em = getEm();
        em.getTransaction().begin();
        JobInstance j = em.find(JobInstance.class, idJob, LockModeType.PESSIMISTIC_READ);
        jqmlogger.debug("The " + j.getState() + " job (ID: " + idJob + ")" + " will be marked for kill");

        j.setState(State.KILLED);

        MessageJi m = new MessageJi();
        m.setJobInstance(j);
        m.setTextMessage("Kill attempt on the job");
        em.persist(m);

        em.getTransaction().commit();
        em.close();
        jqmlogger.debug("Job status after killJob: " + j.getState());
    }

    // ----------------------------- RESTARTCRASHEDJOB --------------------------------------
    /**
     * Will restart a crashed job. This will eventually update the history and make the error disappear from the logs should the job end OK
     * this time.
     * 
     * @param idJob
     */
    public static void restartCrashedJob(int idJob)
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
            throw new JqmException("You cannot restart a job that is not done or which was purged from history");
        }

        if (!h.getState().equals(State.CRASHED))
        {
            em.close();
            throw new JqmException("You cannot restart a job that has not crashed");
        }

        if (!h.getJd().isCanBeRestarted())
        {
            em.close();
            throw new JqmException("This type of job was configured to prevent being restarded");
        }

        em.getTransaction().begin();
        em.remove(h);
        em.getTransaction().commit();

        enQueue(getJobDefinition(h));
        em.close();
    }

    // ----------------------------- RESTARTJOB --------------------------------------
    /**
     * Will enqueue (copy) a job once again with the same parameters and environment. This will not destroy the log of the original job
     * instance.
     * 
     * @param idJob
     * @return
     */
    public static int restartJob(int idJob)
    {
        EntityManager em = getEm();
        History h = null;
        try
        {
            h = em.find(History.class, idJob);
        }
        catch (NoResultException e)
        {
            em.close();
            throw new JqmException("You cannot restart a job that is not done or which was purged from history");
        }

        if (!h.getJd().isCanBeRestarted())
        {
            throw new JqmException("This type of job was configured to prevent being restarded");
        }

        int res = Dispatcher.enQueue(getJobDefinition(h));
        em.close();
        return res;
    }

    // ----------------------------- SETPOSITION --------------------------------------

    /**
     * Change the position of a job instance inside a queue.
     * 
     * @param idJob
     * @param position
     */
    public static void setPosition(int idJob, int position)
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
            throw new JqmException(
                    "Could not lock a job by the given ID. It may already have been executed or a timeout may have occurred.", e);
        }

        if (!ji.getState().equals(State.SUBMITTED))
        {
            em.getTransaction().rollback();
            em.close();
            throw new JqmException("Job is already set for execution. Too late to change its position in the queue");
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

    // ----------------------------- GETDELIVERABLES --------------------------------------

    /**
     * Retrieve a list of all deliverables produced by a job instance as OutputStreams.
     * 
     * @param idJob
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static List<InputStream> getDeliverables(int idJob) throws IOException
    {
        EntityManager em = getEm();
        ArrayList<InputStream> streams = new ArrayList<InputStream>();
        List<Deliverable> tmp = null;

        tmp = em.createQuery("SELECT d FROM Deliverable d WHERE d.jobId = :idJob", Deliverable.class).setParameter("idJob", idJob)
                .getResultList();

        jqmlogger.debug("idJob of the deliverable: " + idJob);
        jqmlogger.debug("size of the deliverable list: " + tmp.size());

        for (Deliverable del : tmp)
        {
            streams.add(getOneDeliverable(del));
        }

        em.close();
        return streams;
    }

    // ----------------------------- GETALLDELIVERABLES --------------------------------------

    /**
     * Get a list of deliverables description produced by a job instance.
     * 
     * @param idJob
     * @return
     */
    public static List<com.enioka.jqm.api.Deliverable> getAllDeliverables(int idJob)
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
            throw new JqmException("Deliverables cannot be found", e);
        }

        List<com.enioka.jqm.api.Deliverable> res = new ArrayList<com.enioka.jqm.api.Deliverable>();

        for (Deliverable d : deliverables)
        {
            res.add(new com.enioka.jqm.api.Deliverable(d.getFilePath(), d.getFileFamily(), d.getId(), d.getOriginalFileName()));
        }

        return res;
    }

    // ----------------------------- GETONEDELIVERABLE --------------------------------------

    /**
     * Retrieve one deliverable from JQM as a Stream.
     * 
     * @param d
     * @return
     * @throws JqmException
     */
    public static InputStream getOneDeliverable(com.enioka.jqm.api.Deliverable d) throws JqmException
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
            jqmlogger.info(e);
            em.close();
            throw new JqmException("Could not get find deliverable description inside DB - your ID may be wrong", e);
        }

        return getOneDeliverable(deliverable);
    }

    static InputStream getOneDeliverable(Deliverable deliverable) throws JqmException
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
            jqmlogger.info("GetOneDeliverable: No ended job found with this deliverable ID");
            em.close();
            throw new JqmException("No ended job found with the deliverable ID", e);
        }

        String destDir = System.getProperty("java.io.tmpdir") + "/" + h.getId();
        (new File(destDir)).mkdir();
        jqmlogger.debug("File will be copied into " + destDir);

        try
        {
            url = new URL("http://" + h.getNode().getDns() + ":" + h.getNode().getPort() + "/getfile?file=" + deliverable.getRandomId());
            jqmlogger.debug("URL: " + url.toString());
        }
        catch (MalformedURLException e)
        {
            throw new JqmException("URL is not valid " + url, e);
        }

        try
        {
            file = new File(destDir + "/" + h.getId() + deliverable.getOriginalFileName());
            FileUtils.copyURLToFile(url, file);
            jqmlogger.debug("File was downloaded to " + file.getAbsolutePath());
        }
        catch (IOException e)
        {
            throw new JqmException("Could not copy the downloaded file", e);
        }
        em.close();

        FileInputStream res = null;
        try
        {
            res = new FileInputStream(file);
        }
        catch (IOException e)
        {
            throw new JqmException("File seems not to be present where it should have been downloaded", e);
        }
        return res;
    }

    // ----------------------------- GETUSERDELIVERABLES --------------------------------------

    public static List<com.enioka.jqm.api.Deliverable> getUserDeliverables(String user)
    {
        EntityManager em = getEm();
        ArrayList<com.enioka.jqm.api.Deliverable> res = new ArrayList<com.enioka.jqm.api.Deliverable>();
        com.enioka.jqm.api.Deliverable tmp = null;

        for (Deliverable d : em
                .createQuery("SELECT d FROM Deliverable d, History h WHERE d.jobId = h.id AND h.userName = :u", Deliverable.class)
                .setParameter("u", user).getResultList())
        {
            tmp = new com.enioka.jqm.api.Deliverable(d.getFilePath(), d.getFileFamily(), d.getId(), d.getOriginalFileName());
            res.add(tmp);
        }

        em.close();
        return res;
    }

    // ----------------------------- GETMSG --------------------------------------

    public static List<String> getMsg(int idJob)
    {
        return getJobInstance(idJob).getMessages();
    }

    // ----------------------------- GETPROGRESS --------------------------------------
    public static Integer getProgress(int idJob)
    {
        return getJobInstance(idJob).getProgress();
    }

    // ----------------------------- GETUSERJOBS --------------------------------------

    /**
     * List all active job instances created by a given user
     * 
     * @param user
     * @return
     */
    public static List<com.enioka.jqm.api.JobInstance> getUserJobs(String user)
    {
        ArrayList<com.enioka.jqm.api.JobInstance> jobs = new ArrayList<com.enioka.jqm.api.JobInstance>();
        EntityManager em = getEm();

        try
        {
            for (JobInstance h : em.createQuery("SELECT j FROM JobInstance j WHERE j.userName = :u ORDER BY j.id", JobInstance.class)
                    .setParameter("u", user).getResultList())
            {
                jobs.add(getJobInstance(h));
            }
            for (History h : em.createQuery("SELECT j FROM History j WHERE j.userName = :u ORDER BY j.id", History.class)
                    .setParameter("u", user).getResultList())
            {
                jobs.add(getJobInstance(h));
            }
        }
        catch (Exception e)
        {
            throw new JqmException("The user cannot be found or an error occured getting his jobs", e);
        }

        em.close();
        return jobs;
    }

    // ----------------------------- GETJOBS --------------------------------------

    /**
     * List all active job instances
     * 
     * @return
     */
    public static List<com.enioka.jqm.api.JobInstance> getJobs()
    {
        ArrayList<com.enioka.jqm.api.JobInstance> res = new ArrayList<com.enioka.jqm.api.JobInstance>();
        ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) getEm().createQuery("SELECT j FROM JobInstance j", JobInstance.class)
                .getResultList();

        for (JobInstance j : jobs)
        {
            com.enioka.jqm.api.JobInstance tmp = new com.enioka.jqm.api.JobInstance();

            tmp.setId(j.getId());
            tmp.setJd(jobDefToJobDefinition(j.getJd()));
            if (j.getParentId() != null)
            {
                tmp.setParent(j.getParentId());
            }
            else
            {
                tmp.setParent(null);
            }

            res.add(tmp);
        }

        return res;
    }

    // ----------------------------- GETQUEUES --------------------------------------

    /**
     * List all the available queues.
     * 
     * @return
     */
    public static List<com.enioka.jqm.api.Queue> getQueues()
    {
        List<com.enioka.jqm.api.Queue> res = new ArrayList<com.enioka.jqm.api.Queue>();
        ArrayList<Queue> queues = (ArrayList<Queue>) getEm().createQuery("SELECT j FROM Queue j", Queue.class).getResultList();

        for (Queue queue : queues)
        {
            com.enioka.jqm.api.Queue q = getQueue(queue);
            res.add(q);
        }

        return res;
    }

    // ----------------------------- CHANGEQUEUE --------------------------------------

    /**
     * Move a job instance from a queue to another.
     * 
     * @param idJob
     * @param idQueue
     */
    public static void changeQueue(int idJob, int idQueue)
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
            throw new JqmException("Queue does not exist");
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
            throw new JqmException("Job instance does not exist or has already started");
        }
        finally
        {
            em.close();
        }
    }

    // ----------------------------- CHANGEQUEUE --------------------------------------

    /**
     * Move a job instance from a queue to another queue
     * 
     * @param idJob
     * @param queue
     */
    public static void changeQueue(int idJob, Queue queue)
    {
        changeQueue(idJob, queue.getId());
    }

    private static JobParameter createJobParameter(String key, String value, EntityManager em)
    {
        JobParameter j = new JobParameter();

        j.setKey(key);
        j.setValue(value);

        em.persist(j);
        return j;
    }

}
