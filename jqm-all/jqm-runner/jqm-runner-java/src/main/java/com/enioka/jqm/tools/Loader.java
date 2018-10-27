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

import java.lang.management.ManagementFactory;
import java.util.Calendar;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.enioka.jqm.api.JobInstanceTracker;
import com.enioka.jqm.api.JobManager;
import com.enioka.jqm.api.JobRunnerCallback;
import com.enioka.jqm.api.JobRunnerException;
import com.enioka.jqm.api.JqmKillException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.History;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The loader is the tracker object for a payload execution. The job thread starts here and ends here. This class handles logging (creation
 * of {@link History}),starting the payload, etc.
 */
class Loader implements JobInstanceTracker, LoaderMBean
{
    private Logger jqmlogger = LoggerFactory.getLogger(Loader.class);

    private JobInstance job = null;
    private JobManager engineApi = null;

    private final JobRunnerCallback engineCallback;
    private final ClassloaderManager clm;

    private ObjectName name = null;
    private ClassLoader classLoaderToRestoreAtEnd = null;
    private JarClassLoader jobClassLoader = null;
    private EngineApiProxy handler = null;

    Loader(JobInstance job, JobRunnerCallback cb, ClassloaderManager clm, JobManager engineApi)
    {
        this.engineCallback = cb;
        this.clm = clm;
        this.job = job;
        this.engineApi = engineApi;

        // JMX
        if (cb != null && cb.isJmxEnabled())
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try
            {
                name = new ObjectName(cb.getJmxBeanName());
                mbs.registerMBean(this, name);
            }
            catch (Exception e)
            {
                throw new JobRunnerException("Could not create JMX bean for running job instance", e);
            }
        }
    }

    @Override
    public void initialize(DbConn cnx)
    {
        // Set CL cache inside JD
        this.job.getJD().getClassLoader(cnx);

        // Create a proxy able to cross CL boundaries for the Engine API.
        handler = new EngineApiProxy(engineApi);

        // Class loader creation (or retrieval from cache)
        try
        {
            this.jobClassLoader = this.clm.getClassloader(this.job, this.engineCallback);
        }
        catch (Exception e)
        {
            throw new JobRunnerException(e);
        }
    }

    @Override
    public State run()
    {
        // Class loader switch
        classLoaderToRestoreAtEnd = Thread.currentThread().getContextClassLoader();
        try
        {
            // Switch
            jqmlogger.trace("Setting class loader");
            Thread.currentThread().setContextClassLoader(jobClassLoader);
            jqmlogger.trace("Class Loader was set correctly");
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not switch classloaders", e);
            return State.CRASHED;
        }

        // Go! (launches the main function in the startup class designated in the manifest)
        try
        {
            jobClassLoader.launchJar(job, job.getPrms(), clm, handler);
            return State.ENDED;
        }
        catch (JqmKillException e)
        {
            Thread.interrupted(); // Clear interrupted status. (sad: only useful for Oracle driver)
            jqmlogger.info("Job instance  " + job.getId() + " has been killed.");
            return State.CRASHED;
        }
        catch (Exception e)
        {
            jqmlogger.info("Job instance " + job.getId() + " has crashed. Exception was:", e);
            return State.CRASHED;
        }
    }

    @Override
    public void wrap()
    {
        // Restore and clean class loaders (if needed, as CLs may be persistent)
        if (this.classLoaderToRestoreAtEnd != null)
        {
            if (Thread.currentThread().getContextClassLoader() instanceof JarClassLoader)
            {
                ((JarClassLoader) Thread.currentThread().getContextClassLoader()).tryClose();
            }
            Thread.currentThread().setContextClassLoader(classLoaderToRestoreAtEnd);
            jqmlogger.trace("Class Loader was correctly restored");
        }

        // Unregister MBean
        if (engineCallback != null && engineCallback.isJmxEnabled())
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
    }

    ///////////////////////////////////////////////////////////////////////////
    // JMX methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void kill()
    {
        this.engineCallback.killThroughClientApi();
    }

    @Override
    public String getApplicationName()
    {
        return this.job.getJD().getApplicationName();
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
        return this.engineCallback.getRunTimeSeconds();
    }
}
