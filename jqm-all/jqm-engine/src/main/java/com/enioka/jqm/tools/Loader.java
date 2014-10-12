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
import org.apache.commons.lang.exception.ExceptionUtils;
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
    private Logger jqmlogger = Logger.getLogger(this.getClass());

    private JobInstance job = null;
    private Node node = null;

    private QueuePoller p = null;
    private LibraryCache cache = null;

    private ObjectName name = null;
    private ClassLoader contextClassLoader = null;

    Loader(JobInstance job, LibraryCache cache, QueuePoller p)
    {
        this.cache = cache;
        this.p = p;
        this.job = job;

        // JMX
        if (p.engine.loadJmxBeans)
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try
            {
                name = new ObjectName("com.enioka.jqm:type=Node.Queue.JobInstance,Node=" + this.job.getNode().getName() + ",Queue="
                        + this.job.getQueue().getName() + ",name=" + this.job.getId());
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
        if (System.out instanceof MulticastPrintStream)
        {
            String fileName = StringUtils.leftPad("" + this.job.getId(), 10, "0");
            MulticastPrintStream mps = (MulticastPrintStream) System.out;
            mps.registerThread(String.valueOf(fileName + ".stdout.log"));
            mps = (MulticastPrintStream) System.err;
            mps.registerThread(String.valueOf(fileName + ".stderr.log"));
        }

        // Var init
        Thread.currentThread().setName(this.job.getJd().getApplicationName() + ";payload;" + this.job.getId());
        EntityManager em = Helpers.getNewEm();
        this.job = em.find(JobInstance.class, job.getId());
        this.node = em.find(Node.class, p.getDp().getNode().getId());

        // Log
        State resultStatus = State.SUBMITTED;
        String endMessage = null;
        jqmlogger.debug("A loader/runner thread has just started for Job Instance " + job.getId() + ". Jar is: " + job.getJd().getJarPath()
                + " - class is: " + job.getJd().getJavaClassName());

        // Check file paths
        File jarFile = new File(FilenameUtils.concat(new File(node.getRepo()).getAbsolutePath(), job.getJd().getJarPath()));
        if (!jarFile.canRead())
        {
            jqmlogger.warn("Cannot read file at " + jarFile.getAbsolutePath()
                    + ". Job instance will crash. Check job definition or permissions on file.");
            endOfRun(State.CRASHED);
            em.close();
            return;
        }
        final URL jarUrl;
        try
        {
            jarUrl = jarFile.toURI().toURL();
        }
        catch (MalformedURLException ex)
        {
            jqmlogger.warn("The JAR file path specified in Job Definition is incorrect " + job.getJd().getApplicationName(), ex);
            endOfRun(State.CRASHED);
            em.close();
            return;
        }

        // Create the CLASSPATH for our classloader (if not already in cache)
        final URL[] classpath;
        try
        {
            classpath = cache.getLibraries(node, job.getJd(), em);
        }
        catch (Exception e1)
        {
            jqmlogger.warn("Could not resolve CLASSPATH for job " + job.getJd().getApplicationName(), e1);
            endOfRun(State.CRASHED);
            em.close();
            return;
        }

        // Update of the job status, dates & co
        try
        {
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
        catch (Exception e)
        {
            jqmlogger.error("Could not update internal elements", e);
            em.getTransaction().rollback();
            em.close();
            endOfRun(State.CRASHED);
            return;
        }

        // Parameters
        Map<String, String> params = new HashMap<String, String>();
        for (RuntimeParameter jp : em.createQuery("SELECT p FROM RuntimeParameter p WHERE p.ji = :i", RuntimeParameter.class)
                .setParameter("i", job.getId()).getResultList())
        {
            jqmlogger.trace("Parameter " + jp.getKey() + " - " + jp.getValue());
            params.put(jp.getKey(), jp.getValue());
        }

        // No need anymore for the EM.
        em.close();

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
                    return new JarClassLoader(jarUrl, classpath, extLoader);
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
            endOfRun(State.CRASHED);
            return;
        }

        // Go! (launches the main function in the startup class designated in the manifest)
        try
        {
            jobClassLoader.launchJar(job, params);
            resultStatus = State.ENDED;
        }
        catch (JqmKillException e)
        {
            jqmlogger.info("Job instance  " + job.getId() + " has been killed.");
            resultStatus = State.KILLED;
        }
        catch (Exception e)
        {
            jqmlogger.info("Job instance " + job.getId() + " has crashed. Exception was:", e);
            resultStatus = State.CRASHED;
            endMessage = "Status updated: " + resultStatus + ". Exception was (give this to support): " + e.getMessage() + "\n";
            endMessage = endMessage
                    + ExceptionUtils.getStackTrace(e).substring(0,
                            Math.min(ExceptionUtils.getStackTrace(e).length() - 1, 999 - endMessage.length()));
        }

        // Job instance has now ended its run
        try
        {
            endOfRun(resultStatus);
        }
        catch (Exception e)
        {
            jqmlogger.error("An error occurred while finalizing the job instance.", e);
        }

        jqmlogger.debug("End of loader for JobInstance " + this.job.getId() + ". Thread will now end");
    }

    private void endOfRun(State status)
    {
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

        // Release the slot
        p.decreaseNbThread();
        EntityManager em = Helpers.getNewEm();

        // Clean class loader
        ClassLoaderLeakCleaner.clean(Thread.currentThread().getContextClassLoader());

        // Restore class loader
        if (this.contextClassLoader != null)
        {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            jqmlogger.trace("Class Loader was correctly restored");
        }

        // Retrieve the object to update
        job = em.find(JobInstance.class, this.job.getId());

        // Update end date
        Calendar endDate = GregorianCalendar.getInstance(Locale.getDefault());

        // Done: put inside history & remove instance from queue.
        em.getTransaction().begin();
        History h = Helpers.createHistory(job, em, status, endDate);
        jqmlogger.trace("An History was just created for job instance " + h.getId());

        // Other transaction for purging the JI (deadlock power - beware of Message)
        em.getTransaction().commit();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM JobInstance WHERE id = :i").setParameter("i", job.getId()).executeUpdate();
        em.getTransaction().commit();

        // Clean temp dir (if it exists)
        File tmpDir = new File(FilenameUtils.concat(node.getTmpDirectory(), "" + job.getId()));
        if (tmpDir.isDirectory())
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

        // Unregister MBean
        if (this.p.engine.loadJmxBeans)
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
        if (System.out instanceof MulticastPrintStream)
        {
            MulticastPrintStream mps = (MulticastPrintStream) System.out;
            mps.unregisterThread();
            mps = (MulticastPrintStream) System.err;
            mps.unregisterThread();
        }

        // Done
        em.close();
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
        return (Calendar.getInstance().getTimeInMillis() - this.job.getExecutionDate().getTimeInMillis()) / 1000;
    }
}
