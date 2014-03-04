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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobHistoryParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.MessageJi;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.State;
import com.jcabi.aether.Aether;

class Loader implements Runnable, LoaderMBean
{
    private JobInstance job = null;
    private Node node = null;
    private EntityManager em = Helpers.getNewEm();
    private Map<String, URL[]> cache = null;
    private Logger jqmlogger = Logger.getLogger(this.getClass());
    private Polling p = null;
    private String res = null;
    private String lib = null;
    private File jarFile = null, jarDir = null;
    private File extractDir = null;
    private ClassLoader contextClassLoader = null;
    private ObjectName name = null;

    Loader(JobInstance job, Map<String, URL[]> cache, Polling p)
    {
        this.job = em.find(JobInstance.class, job.getId());
        this.node = em.find(Node.class, p.getDp().getNode().getId());
        this.cache = cache;
        this.p = p;

        // JMX
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try
        {
            name = new ObjectName("com.enioka.jqm:type=Node.Queue.JobInstance,Node=" + this.job.getNode().getName() + ",Queue="
                    + this.job.getQueue().getName() + ",name=" + this.job.getId());
            mbs.registerMBean(this, name);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not create JMX beans", e);
        }
    }

    // ExtractJar
    private void extractJar(String jarFile) throws IOException
    {
        jqmlogger.debug("jar: " + jarFile);
        JarFile jar = null;
        InputStream is = null;
        FileOutputStream fos = null;
        cleanUpExtractedZip();
        try
        {
            jar = new JarFile(jarFile);
            if (!extractDir.exists() && !extractDir.mkdir())
            {
                throw new IOException("could not create directory " + extractDir.getAbsolutePath());
            }
            Enumeration<JarEntry> enumm = jar.entries();
            while (enumm.hasMoreElements())
            {
                JarEntry file = enumm.nextElement();
                jqmlogger.debug("file: " + file.getName());
                File f = new File(extractDir + File.separator + file.getName());
                if (file.isDirectory())
                {
                    // if its a directory, create it
                    jqmlogger.debug("The file is actually a directory");
                    if (!f.exists() && !f.mkdir())
                    {
                        throw new IOException("could not create directory " + f.getAbsolutePath());
                    }
                    continue;
                }

                is = jar.getInputStream(file);
                fos = new FileOutputStream(f);
                while (is.available() > 0)
                {
                    // write contents of 'is' to 'fos'
                    fos.write(is.read());
                }

                IOUtils.closeQuietly(fos);
                IOUtils.closeQuietly(is);
            }

            // Done with the jar
            jar.close();
        }
        catch (IOException e)
        {
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(is);

            try
            {
                if (jar != null)
                {
                    jar.close();
                }
            }
            catch (IOException e1)
            {
                jqmlogger.debug(e);
            }
            throw e;
        }
    }

    // FindFile
    void findFile(String path, File f)
    {
        try
        {
            jqmlogger.debug("Trying to find the job in the directory: " + f);
            File[] list = f.listFiles();
            if (list != null)
            {
                for (File ff : list)
                {
                    if (ff.isDirectory() && path.equalsIgnoreCase(ff.getName()))
                    {
                        jqmlogger.debug("findFile lib " + ff.getPath());
                        lib = ff.getAbsolutePath();
                    }
                    if (ff.isDirectory())
                    {
                        findFile(path, ff);
                    }
                    else if (path.equalsIgnoreCase(ff.getName()))
                    {
                        jqmlogger.debug("findFile returning " + ff.getPath());
                        res = ff.getParentFile().getAbsolutePath();
                    }
                }
            }
        }
        catch (Exception e)
        {
            jqmlogger.debug(e);
        }
    }

    private void cleanUpExtractedZip()
    {
        if (!extractDir.canExecute())
        {
            return;
        }
        try
        {
            FileUtils.deleteDirectory(extractDir);
        }
        catch (IOException e)
        {
            jqmlogger.warn("Could not delete a temp file. It may result in filling up the file system", e);
        }
    }

    private void classpathResolution(Node node) throws MalformedURLException, DependencyResolutionException, NoPomException
    {
        if (cache.containsKey(job.getJd().getApplicationName()))
        {
            return;
        }
        File local = new File(System.getProperty("user.home") + "/.m2/repository");
        Collection<Artifact> deps = null;
        File pomFile = new File(FilenameUtils.concat(jarDir.getAbsolutePath(), "pom.xml"));
        File libDir = new File(FilenameUtils.concat(jarDir.getAbsolutePath(), "lib"));
        jqmlogger.debug("Loader will try to load POM " + pomFile.getAbsolutePath());
        boolean pomFromJar = false;

        // 1st: if no pom, no lib dir => find a pom inside the JAR. (& copy it, we will read later)
        if (!pomFile.exists() && !libDir.exists())
        {
            jqmlogger.debug("No pom inside jar directory. Checking for a pom inside the jar file");
            InputStream is = null;
            OutputStream os = null;
            try
            {
                pomFromJar = true;
                jqmlogger.debug("POM doesn't exist, engine will try to find in the META-INF/maven directory of the Jar file");

                extractJar(jarFile.getAbsolutePath());
                findFile("pom.xml", new File(extractDir + "/META-INF/maven/"));
                jqmlogger.debug("pomdebug: " + res);

                if (res != null)
                {
                    is = new FileInputStream(res + "/pom.xml");
                    os = new FileOutputStream(pomFile);
                    IOUtils.copy(is, os);
                }
            }
            catch (IOException e)
            {
                throw new NoPomException("Could not handle pom", e);
            }
            finally
            {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
            }

            // Cleanup extract dir - it is useless now as a pom was found and copied at its usual place
            if (res != null)
            {
                cleanUpExtractedZip();
            }
        }

        // 2nd: if no pom, no pom inside jar, no lib dir => find a lib dir inside the jar
        if (!pomFile.exists() && !libDir.exists())
        {
            jqmlogger.debug("Trying lib inside jar");
            findFile("lib", extractDir);

            if (lib != null)
            {
                File dir = new File(lib);

                FileFilter fileFilter = new WildcardFileFilter("*.jar");
                File[] files = dir.listFiles(fileFilter);
                URL[] libUrls = new URL[files.length];
                for (int i = 0; i < files.length; i++)
                {
                    jqmlogger.debug("Jars in lib/ : " + files[i].toURI().toURL());
                    libUrls[i] = files[i].toURI().toURL();
                }

                // Put in cache
                this.cache.put(job.getJd().getApplicationName(), libUrls);
                return;
            }
            else
            {
                jqmlogger.debug("no lib directory inside jar");
                cleanUpExtractedZip();
            }
        }

        // 3rd: if pom, use pom!
        if (pomFile.exists() && !libDir.exists())
        {
            jqmlogger.debug("Reading a pom file");
            Dependencies dependencies = new Dependencies(pomFile.getAbsolutePath());

            List<GlobalParameter> repolist = em
                    .createQuery("SELECT gp FROM GlobalParameter gp WHERE gp.key = :repo", GlobalParameter.class)
                    .setParameter("repo", "mavenRepo").getResultList();

            RemoteRepository[] rr = new RemoteRepository[repolist.size()];
            int ii = 0;
            for (GlobalParameter g : repolist)
            {
                rr[ii] = new RemoteRepository(g.getKey(), "default", g.getValue());
                ii++;
            }

            Collection<RemoteRepository> remotes = Arrays.asList(rr);

            deps = new ArrayList<Artifact>();
            for (int i = 0; i < dependencies.getList().size(); i++)
            {
                jqmlogger.debug("Resolving Maven dep " + dependencies.getList().get(i));
                deps.addAll(new Aether(remotes, local).resolve(new DefaultArtifact(dependencies.getList().get(i)), "compile"));
            }

            int size = 0;
            for (Artifact artifact : deps)
            {
                if (!"pom".equals(artifact.getExtension()))
                {
                    size++;
                }
            }
            URL[] tmp = new URL[size];
            int i = 0;
            for (Artifact artifact : deps)
            {
                if ("pom".equals(artifact.getExtension()))
                {
                    continue;
                }
                tmp[i] = artifact.getFile().toURI().toURL();
                jqmlogger.debug("Artifact from pom: " + artifact.getFile().toURI().toURL());
                i++;
            }

            // Put in cache
            this.cache.put(job.getJd().getApplicationName(), tmp);

            // Cleanup
            if (pomFromJar && !pomFile.delete())
            {
                jqmlogger.warn("Could not delete the temp pom file extracted from the jar.");
            }
            return;
        }

        // 4: if lib, use lib... (lib has priority over pom)
        if (libDir.exists())
        {
            jqmlogger.debug("Using the lib directory " + libDir.getAbsolutePath() + " as the source for dependencies");
            FileFilter fileFilter = new WildcardFileFilter("*.jar");
            File[] files = libDir.listFiles(fileFilter);
            URL[] tmp = new URL[files.length];
            for (int i = 0; i < files.length; i++)
            {
                tmp[i] = files[i].toURI().toURL();
                jqmlogger.debug("Artifact from lib dir: " + tmp[i]);
            }

            // Put in cache
            this.cache.put(job.getJd().getApplicationName(), tmp);
            return;
        }

        throw new NoPomException(
                "There is no lib dir or no pom.xml inside the directory containing the jar or inside the jar. The jar cannot be launched.");
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
    };

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

        State resultStatus = State.SUBMITTED;
        jqmlogger.debug("LOADER HAS JUST STARTED UP FOR JOB INSTANCE " + job.getId());
        jqmlogger.debug("Job instance " + job.getId() + " has " + job.getParameters().size() + " parameters");
        jarFile = new File(FilenameUtils.concat(new File(node.getRepo()).getAbsolutePath(), job.getJd().getJarPath()));
        jarDir = jarFile.getParentFile();

        if (!jarFile.canRead())
        {
            jqmlogger.warn("Cannot read file at " + jarFile.getAbsolutePath()
                    + ". Job instance will crash. Check job definition or permissions on file.");
            endOfRun(State.CRASHED);
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
            return;
        }
        extractDir = new File(FilenameUtils.concat(jarDir.getAbsolutePath(), "tmp/"));
        jqmlogger.debug("Loader will try to launch jar " + jarFile.getAbsolutePath() + " - " + job.getJd().getJavaClassName());

        // Create the CLASSPATH for our classloader (if not already in cache)
        try
        {
            synchronized (cache)
            {
                if (!cache.containsKey(job.getJd().getApplicationName()))
                {
                    jqmlogger.info("Application " + job.getJd().getApplicationName() + " dependencies are not yet in cache");
                    classpathResolution(node);
                }
                else
                {
                    jqmlogger.debug("Application  " + job.getJd().getApplicationName() + " dependencies are already in cache - "
                            + cache.get(job.getJd().getApplicationName()).length);
                    for (URL s : cache.get(job.getJd().getApplicationName()))
                    {
                        jqmlogger.trace("JI " + job.getId() + " - " + s.getPath());
                    }
                }
            }
        }
        catch (Exception e1)
        {
            jqmlogger.warn("Could not resolve CLASSPATH for job " + job.getJd().getApplicationName(), e1);
            endOfRun(State.CRASHED);
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

            // Add a message
            Helpers.createMessage("Status updated: RUNNING", job, em);

            em.getTransaction().commit();
            jqmlogger.debug("JobInstance was updated: " + job.getState());
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not update internal elements", e);
            em.getTransaction().rollback();
            endOfRun(State.CRASHED);
            return;
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
                public JarClassLoader run()
                {
                    return new JarClassLoader(jarUrl, cache.get(job.getJd().getApplicationName()));
                }
            });

            // Switch
            jqmlogger.debug("Setting class loader");
            Thread.currentThread().setContextClassLoader(jobClassLoader);
            jqmlogger.debug("Class Loader was set correctly");
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not switch classloaders", e);
            endOfRun(State.CRASHED);
            return;
        }

        // Go! (launches the main function in the startup class designated in the manifest)
        jqmlogger.debug("+++++++++++++++++++++++++++++++++++++++");
        jqmlogger.debug("Job is running in the thread: " + Thread.currentThread().getName());
        jqmlogger.debug("JOB MAIN FUNCTION WILL BE INVOKED NOW");
        try
        {
            jobClassLoader.launchJar(job, contextClassLoader, em);
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
        }
        jqmlogger.debug("++++++++++++++++++++++++++++++++++++++++");

        // Job instance has now ended its run
        try
        {
            endOfRun(resultStatus);
        }
        catch (Exception e)
        {
            jqmlogger.error("An error occurred while finalizing the job instance.", e);
        }

        jqmlogger.debug("End of loader. Thread will now end");
        jqmlogger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        jqmlogger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

        em.close();
    }

    private void endOfRun(State status)
    {
        p.decreaseNbThread();

        // Restore class loader
        if (this.contextClassLoader != null)
        {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            jqmlogger.debug("Class Loader was correctly restored");
        }

        // Retrieve the object to update
        em.getTransaction().begin();
        em.refresh(job);
        jqmlogger.debug("Job status after execution: " + job.getState());
        jqmlogger.debug("Progression after execution: " + job.getProgress());
        jqmlogger.debug("Post execution, the number of running threads for the current queue is " + p.getCurrentActiveThreadCount());

        // Update end date
        Calendar endDate = GregorianCalendar.getInstance(Locale.getDefault());

        // STATE UPDATED
        // job.setState(status);
        jqmlogger.debug("In the Loader --> ENDED");

        // Done: put inside history & remove instance from queue.
        History h = new History();
        h.setId(job.getId());
        h.setJd(job.getJd());
        h.setSessionId(job.getSessionID());
        h.setQueue(job.getQueue());
        h.setMessages(new ArrayList<Message>());
        h.setEnqueueDate(job.getCreationDate());
        h.setEndDate(endDate);
        h.setAttributionDate(job.getAttributionDate());
        h.setExecutionDate(job.getExecutionDate());
        h.setUserName(job.getUserName());
        h.setEmail(job.getEmail());
        h.setParentJobId(job.getParentId());
        h.setApplication(job.getApplication());
        h.setModule(job.getModule());
        h.setKeyword1(job.getKeyword1());
        h.setKeyword2(job.getKeyword2());
        h.setKeyword3(job.getKeyword3());
        h.setProgress(job.getProgress());
        h.setParameters(new ArrayList<JobHistoryParameter>());
        h.setStatus(status);
        h.setNode(job.getNode());

        em.persist(h);
        jqmlogger.debug("An History was just created: " + h.getId());

        for (JobParameter j : job.getParameters())
        {
            JobHistoryParameter jp = new JobHistoryParameter();
            jp.setKey(j.getKey());
            jp.setValue(j.getValue());
            em.persist(jp);
            h.getParameters().add(jp);
        }
        for (MessageJi p : job.getMessages())
        {
            Message m = new Message();
            m.setHistory(h);
            m.setTextMessage(p.getTextMessage());
            em.persist(m);
        }

        // A last message (directly created on History, not JI)
        Message m = new Message();
        m.setHistory(h);
        m.setTextMessage("Status updated: " + status);
        em.persist(m);

        // Purge the JI (using query, not em - more efficient + avoid cache issues on MessageJI which are not up to date here)
        em.getTransaction().commit();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM MessageJi WHERE jobInstance = :i").setParameter("i", job).executeUpdate();
        em.getTransaction().commit();
        em.getTransaction().begin();
        em.createQuery("DELETE FROM JobParameter WHERE jobInstance = :i").setParameter("i", job).executeUpdate();
        em.getTransaction().commit();

        // Other transaction for purging the JI (deadlock power - beware of MessageJi)
        em.getTransaction().begin();
        em.createQuery("DELETE FROM JobInstance WHERE id = :i").setParameter("i", job.getId()).executeUpdate();
        em.getTransaction().commit();

        // Send e-mail
        if (job.getEmail() != null)
        {
            try
            {
                Mail.send(job, em);
            }
            catch (Exception e)
            {
                jqmlogger.warn("An e-mail could not be sent. No impact on the engine.", e);
            }
        }

        // Unregister MBean
        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.unregisterMBean(name);
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not unregister JobInstance JMX bean", e);
        }

        // Unregister logger
        if (System.out instanceof MulticastPrintStream)
        {
            MulticastPrintStream mps = (MulticastPrintStream) System.out;
            mps.unregisterThread();
            mps = (MulticastPrintStream) System.err;
            mps.unregisterThread();
        }
    }

    // ////////////////////////////////////////////////////////////
    // JMX methods
    // ////////////////////////////////////////////////////////////

    @Override
    public void kill()
    {
        Properties p = new Properties();
        p.put("emf", this.em.getEntityManagerFactory());
        JqmClientFactory.getClient("uncached", p, false).killJob(this.job.getId());
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
