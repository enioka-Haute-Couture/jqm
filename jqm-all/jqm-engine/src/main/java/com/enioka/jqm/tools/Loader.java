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

package com.enioka.jqm.tools;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.RuntimeParameter;
import com.enioka.jqm.jpamodel.State;

/**
 * The loader is the tracker object for a payload execution. The job thread starts here and ends here. This class handles logging (creation
 * of {@link History}),starting the payload, etc.
 */
class Loader implements Runnable, LoaderMBean
{
    private Logger jqmlogger = Logger.getLogger(Loader.class);

    private JobInstance job = null;
    private Node node = null;

    private QueuePoller p = null;
    private LibraryCache cache = null;

    private ObjectName name = null;
    private ClassLoader contextClassLoader = null;
    Boolean isDone = false;
    private String threadName;

    // These two fields are instance-level in order to allow an easy endOfRunDb external call
    private Calendar endDate = null;
    private State resultStatus = State.ATTRIBUTED;

    Loader(JobInstance job, LibraryCache cache, QueuePoller p)
    {
        this.cache = cache;
        this.p = p;
        this.job = job;
        this.threadName = this.job.getJd().getApplicationName() + ";payload;" + this.job.getId();

        // JMX
        if (p != null && this.p.getEngine().loadJmxBeans)
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try
            {
                name = new ObjectName("com.enioka.jqm:type=Node.Queue.JobInstance,Node=" + this.p.getEngine().getNode().getName()
                        + ",Queue=" + this.p.getQueue().getName() + ",name=" + this.job.getId());
                mbs.registerMBean(this, name);
            }
            catch (Exception e)
            {
                throw new JqmInitError("Could not create JMX bean for running job instance", e);
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            runPayload();
        }
        catch (Throwable t)
        {
            jqmlogger.error("An unexpected error has occurred - the engine may have become unstable", t);
        }
    }

    private void runPayload()
    {
        // Set thread name
        Thread.currentThread().setName(threadName);

        // One log per launch?
        if (System.out instanceof MulticastPrintStream)
        {
            String fileName = StringUtils.leftPad("" + this.job.getId(), 10, "0");
            MulticastPrintStream mps = (MulticastPrintStream) System.out;
            mps.registerThread(String.valueOf(fileName + ".stdout.log"));
            mps = (MulticastPrintStream) System.err;
            mps.registerThread(String.valueOf(fileName + ".stderr.log"));
        }

        EntityManager em = null;
        final boolean noLibLoading;
        final URL jarUrl;
        final URL[] classpath;
        final Map<String, String> params;

        // Block needing the database
        try
        {
            em = Helpers.getNewEm();

            // Refresh entities from the current EM
            this.job = em.find(JobInstance.class, job.getId());
            this.node = em.find(Node.class, job.getNode().getId());

            // Log
            this.resultStatus = State.SUBMITTED;
            jqmlogger.debug("A loader/runner thread has just started for Job Instance " + job.getId() + ". Jar is: "
                    + job.getJd().getJarPath() + " - class is: " + job.getJd().getJavaClassName());

            // Disabled
            if (!this.job.getJd().isEnabled())
            {
                jqmlogger.info("Job Instance " + job.getId() + " will actually not truly run as its Job Definition is disabled");
                em.getTransaction().begin();
                this.job.setProgress(-1);
                em.getTransaction().commit();
                resultStatus = State.ENDED;
                endOfRun();
                return;
            }

            // Check file paths (unless it is /dev/null, which means no jar file)
            noLibLoading = "/dev/null".equals(job.getJd().getJarPath());
            File jarFile = new File(FilenameUtils.concat(new File(node.getRepo()).getAbsolutePath(), job.getJd().getJarPath()));
            if (!noLibLoading && !jarFile.canRead())
            {
                jqmlogger.warn("Cannot read file at " + jarFile.getAbsolutePath()
                        + ". Job instance will crash. Check job definition or permissions on file.");
                resultStatus = State.CRASHED;
                endOfRun();
                return;
            }

            try
            {
                jarUrl = jarFile.toURI().toURL();
            }
            catch (MalformedURLException ex)
            {
                jqmlogger.warn("The JAR file path specified in Job Definition is incorrect " + job.getJd().getApplicationName(), ex);
                resultStatus = State.CRASHED;
                endOfRun();
                return;
            }

            // Create the CLASSPATH for our classloader (if not already in cache)
            if (!noLibLoading)
            {
                classpath = cache.getLibraries(node, job.getJd(), em);
            }
            else
            {
                classpath = null;
            }

            // Parameters
            params = new HashMap<String, String>();
            for (RuntimeParameter jp : em.createQuery("SELECT p FROM RuntimeParameter p WHERE p.ji = :i", RuntimeParameter.class)
                    .setParameter("i", job.getId()).getResultList())
            {
                jqmlogger.trace("Parameter " + jp.getKey() + " - " + jp.getValue());
                params.put(jp.getKey(), jp.getValue());
            }

            // Update of the job status, dates & co
            em.getTransaction().begin();
            em.refresh(job, LockModeType.PESSIMISTIC_WRITE);
            if (!job.getState().equals(State.KILLED))
            {
                // Use a query to avoid locks on FK checks (with setters, every field is updated!)
                em.createQuery("UPDATE JobInstance j SET j.executionDate = current_timestamp(), state = 'RUNNING' WHERE j.id = :i")
                        .setParameter("i", job.getId()).executeUpdate();
            }
            em.getTransaction().commit();
        }
        catch (RuntimeException e)
        {
            firstBlockDbFailureAnalysis(e);
            return;
        }
        catch (JqmPayloadException e)
        {
            jqmlogger.warn("Could not resolve CLASSPATH for job " + job.getJd().getApplicationName(), e);
            resultStatus = State.CRASHED;
            endOfRun();
            return;
        }
        finally
        {
            Helpers.closeQuietly(em);
        }

        // Class loader switch
        JarClassLoader jobClassLoader = null;
        try
        {
            // Save the current class loader
            contextClassLoader = Thread.currentThread().getContextClassLoader();

            // At this point, the CLASSPATH is always in cache, so just create the CL with it.
            jobClassLoader = AccessController.doPrivileged(new PrivilegedAction<JarClassLoader>()
            {
                @Override
                public JarClassLoader run()
                {
                    ClassLoader extLoader = null;
                    try
                    {
                        extLoader = ((JndiContext) NamingManager.getInitialContext(null)).getExtCl();
                    }
                    catch (NamingException e)
                    {
                        jqmlogger.warn("could not find ext directory class loader. No parent classloader will be used", e);
                    }
                    if (!noLibLoading)
                    {
                        return new JarClassLoader(jarUrl, classpath, extLoader);
                    }
                    else
                    {
                        return new JarClassLoader(Thread.currentThread().getContextClassLoader());
                    }
                }
            });

            // Switch
            jqmlogger.trace("Setting class loader");
            Thread.currentThread().setContextClassLoader(jobClassLoader);
            jqmlogger.trace("Class Loader was set correctly");
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not switch classloaders", e);
            this.resultStatus = State.CRASHED;
            endOfRun();
            return;
        }

        // Go! (launches the main function in the startup class designated in the manifest)
        try
        {
            jobClassLoader.launchJar(job, params);
            this.resultStatus = State.ENDED;
        }
        catch (JqmKillException e)
        {
            jqmlogger.info("Job instance  " + job.getId() + " has been killed.");
            this.resultStatus = State.KILLED;
        }
        catch (Exception e)
        {
            jqmlogger.info("Job instance " + job.getId() + " has crashed. Exception was:", e);
            this.resultStatus = State.CRASHED;
        }

        // Job instance has now ended its run
        try
        {
            endOfRun();
        }
        catch (Exception e)
        {
            jqmlogger.error("An error occurred while finalizing the job instance.", e);
        }

        jqmlogger.debug("End of loader for JobInstance " + this.job.getId() + ". Thread will now end");
    }

    void endOfRun(State s)
    {
        this.resultStatus = s;
        endOfRun();
    }

    private void endOfRun()
    {
        // Register end date as soon as possible to be as exact as possible (sending mails may take time for example)
        endDate = GregorianCalendar.getInstance(Locale.getDefault());

        // This block is needed for external payloads, as the single runner may forcefully call endOfRun.
        synchronized (this)
        {
            if (!isDone)
            {
                isDone = true;
            }
            else
            {
                return;
            }
        }

        // Release the slot so as to allow other job instances to run (first op!)
        if (p != null)
        {
            p.decreaseNbThread();
        }

        // Send e-mail before releasing the slot - it may be long
        if (job.getEmail() != null)
        {
            try
            {
                Helpers.sendEndMessage(job);
            }
            catch (Exception e)
            {
                jqmlogger.warn("An e-mail could not be sent. No impact on the engine.", e);
            }
        }

        // Clean class loader
        ClassLoaderLeakCleaner.clean(Thread.currentThread().getContextClassLoader());

        // Clean JDBC connections
        ClassLoaderLeakCleaner.cleanJdbc(Thread.currentThread());

        // Restore class loader
        if (this.contextClassLoader != null)
        {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            jqmlogger.trace("Class Loader was correctly restored");
        }

        // Clean temp dir (if it exists)
        File tmpDir = new File(FilenameUtils.concat(node.getTmpDirectory(), "" + job.getId()));
        if (tmpDir.isDirectory())
        {
            try
            {
                if (FileUtils.deleteQuietly(tmpDir))
                {
                    jqmlogger.trace("temp directory was removed");
                }
                else
                {
                    jqmlogger.warn("Could not remove temp directory " + tmpDir.getAbsolutePath()
                            + "for this job instance. There may be open handlers remaining open.");
                }
            }
            catch (Exception e)
            {
                jqmlogger.warn("Could not remove temp directory for unusual reasons", e);
            }
        }

        // Unregister MBean
        if (p != null && this.p.getEngine().loadJmxBeans)
        {
            try
            {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.unregisterMBean(name);
            }
            catch (Exception e)
            {
                jqmlogger.error("Could not unregister JobInstance JMX bean", e);
            }
        }

        // Unregister logger
        unregisterLogger();

        // Part needing DB connection with specific failure handling code.
        endOfRunDb();
    }

    private void unregisterLogger()
    {
        if (System.out instanceof MulticastPrintStream)
        {
            MulticastPrintStream mps = (MulticastPrintStream) System.out;
            mps.unregisterThread();
            mps = (MulticastPrintStream) System.err;
            mps.unregisterThread();
        }
    }

    /**
     * Part of the endOfRun process that needs the database. May be deferred if the database is not available.
     */
    void endOfRunDb()
    {
        EntityManager em = Helpers.getNewEm();

        try
        {
            // Retrieve the object to update
            job = em.find(JobInstance.class, this.job.getId());

            // Done: put inside history & remove instance from queue.
            em.getTransaction().begin();
            History h = Helpers.createHistory(job, em, this.resultStatus, endDate);
            jqmlogger.trace("An History was just created for job instance " + h.getId());
            em.createQuery("DELETE FROM JobInstance WHERE id = :i").setParameter("i", job.getId()).executeUpdate();
            em.getTransaction().commit();
        }
        catch (RuntimeException e)
        {
            endBlockDbFailureAnalysis(e);
        }
        finally
        {
            Helpers.closeQuietly(em);
        }
    }

    private void firstBlockDbFailureAnalysis(Exception e)
    {
        if (Helpers.testDbFailure(e))
        {
            jqmlogger.error("connection to database lost - loader " + this.getId() + " will be restarted later");
            jqmlogger.trace("connection error was:", e);
            this.p.getEngine().loaderRestartNeeded(this);
            unregisterLogger();
            return;
        }
        else
        {
            jqmlogger.error("a database related operation has failed and cannot be recovered", e);
            resultStatus = State.CRASHED;
            endOfRun();
            return;
        }
    }

    private void endBlockDbFailureAnalysis(RuntimeException e)
    {
        if (Helpers.testDbFailure(e))
        {
            jqmlogger.error("connection to database lost - loader " + this.getId() + " will need delayed finalization");
            jqmlogger.trace("connection error was:", e.getCause());
            this.p.getEngine().loaderFinalizationNeeded(this);
        }
        else
        {
            jqmlogger.error("a database related operation has failed and cannot be recovered");
            throw e;
        }
    }

    // ////////////////////////////////////////////////////////////
    // JMX methods
    // ////////////////////////////////////////////////////////////

    @Override
    public void kill()
    {
        Properties props = new Properties();
        props.put("emf", Helpers.getEmf());
        JqmClientFactory.getClient("uncached", props, false).killJob(this.job.getId());
    }

    @Override
    public String getApplicationName()
    {
        return this.job.getJd().getApplicationName();
    }

    @Override
    public Calendar getEnqueueDate()
    {
        return this.job.getCreationDate();
    }

    @Override
    public String getKeyword1()
    {
        return this.job.getKeyword1();
    }

    @Override
    public String getKeyword2()
    {
        return this.job.getKeyword2();
    }

    @Override
    public String getKeyword3()
    {
        return this.job.getKeyword3();
    }

    @Override
    public String getModule()
    {
        return this.job.getModule();
    }

    @Override
    public String getUser()
    {
        return this.job.getUserName();
    }

    @Override
    public String getSessionId()
    {
        return this.job.getSessionID();
    }

    @Override
    public Integer getId()
    {
        return this.job.getId();
    }

    @Override
    public Long getRunTimeSeconds()
    {
        if (this.job.getExecutionDate() == null)
        {
            EntityManager em2 = Helpers.getNewEm();
            this.job.setExecutionDate(em2.find(JobInstance.class, this.job.getId()).getExecutionDate());
            em2.close();
        }
        if (this.job.getExecutionDate() == null)
        {
            return 0L;
        }
        return (Calendar.getInstance().getTimeInMillis() - this.job.getExecutionDate().getTimeInMillis()) / 1000;
    }
}
