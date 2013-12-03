/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;
import com.jcabi.aether.Aether;

class Loader implements Runnable
{
	private JobInstance job = null;
	private History h = null;
	private Node node = null;
	private Object jobBase = null;
	private EntityManager em = Helpers.getNewEm();
	private Map<String, URL[]> cache = null;
	private Logger jqmlogger = Logger.getLogger(this.getClass());
	private Polling p = null;
	private String res = null;
	private String lib = null;
	private URL jarUrl = null;
	private File pomFile = null;
	private boolean pomCustom = false;
	private ClassLoader contextClassLoader = null;

	Loader(JobInstance job, Map<String, URL[]> cache, Polling p)
	{
		this.job = em.find(JobInstance.class, job.getId());
		this.node = em.find(Node.class, p.getDp().getNode().getId());
		this.cache = cache;
		this.p = p;
		this.h = em.createQuery("SELECT h FROM History h WHERE h.jobInstanceId = :j", History.class).setParameter("j", job.getId())
		        .getSingleResult();
	}

	// ExtractJar
	private void extractJar(String jarFile, String destDir) throws IOException
	{
		jqmlogger.debug("jar: " + jarFile);
		JarFile jar = null;
		InputStream is = null;
		FileOutputStream fos = null;
		try
		{
			jar = new JarFile(jarFile);
			File tmp = new File(destDir + "tmp" + job.getId() + "/");
			tmp.mkdir();
			destDir += "tmp" + job.getId() + "/";
			Enumeration<JarEntry> enumm = jar.entries();
			while (enumm.hasMoreElements())
			{
				JarEntry file = enumm.nextElement();
				jqmlogger.debug("file: " + file.getName());
				File f = new File(destDir + File.separator + file.getName());
				if (file.isDirectory())
				{ // if its a directory, create it
					jqmlogger.debug("The file is actually a directory");
					f.mkdir();
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
		} catch (IOException e)
		{
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(is);

			try
			{
				if (jar != null)
				{
					jar.close();
				}
			} catch (IOException e1)
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
		} catch (Exception e)
		{
			jqmlogger.debug(e);
		}
	}

	void classpathResolution(Node node) throws MalformedURLException, DependencyResolutionException, NoPomException
	{
		File local = new File(System.getProperty("user.home") + "/.m2/repository");
		File jar = new File(CheckFilePath.fixFilePath(node.getRepo()) + job.getJd().getJarPath());
		jarUrl = jar.toURI().toURL();
		jqmlogger.debug("Loader will try to launch jar " + jar.getAbsolutePath() + " - " + job.getJd().getJavaClassName());
		Collection<Artifact> deps = null;
		pomFile = new File(CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath())) + "pom.xml");
		jqmlogger.debug("Loader will try to load POM " + pomFile.getAbsolutePath());
		pomCustom = false;

		// 1st: if no pom, find a pom inside the JAR. (& copy it, we will read it in 2nd)
		if (!cache.containsKey(job.getJd().getApplicationName()) && !pomFile.exists())
		{
			InputStream is = null;
			OutputStream os = null;
			try
			{
				pomCustom = true;
				jqmlogger.debug("POM doesn't exist, engine will try to find in the META-INF/maven directory of the Jar file");
				String env = CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath()));

				extractJar(jar.getAbsolutePath(), env);
				findFile("pom.xml", new File(env + "tmp" + job.getId() + "/" + "META-INF/maven/"));

				jqmlogger.debug("pomdebug: " + res);

				if (res != null
				        && (CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath()))) != null)
				{
					is = new FileInputStream(res + "/pom.xml");
					os = new FileOutputStream(CheckFilePath.fixFilePath(node.getRepo()
					        + CheckFilePath.fixFilePath(job.getJd().getFilePath()))
					        + "pom.xml");

					int r = 0;
					byte[] bytes = new byte[1024];

					while ((r = is.read(bytes)) != -1)
					{
						os.write(bytes, 0, r);
					}

					IOUtils.closeQuietly(is);
					IOUtils.closeQuietly(os);

				}
			} catch (Exception e)
			{
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
			}
			pomFile = new File(CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath())) + "pom.xml");
		}

		// 2nd: if no pom, use lib.
		if (!cache.containsKey(job.getJd().getApplicationName()) && !pomFile.exists())
		{
			// If here: no pom.xml was given, no pom.xml inside the jar. Just use a "lib" directory
			jqmlogger.debug("POM doesn't exist, checking a lib directory in the Jar file");
			String env = CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath()));
			findFile("lib", new File(env + "tmp" + job.getId() + "/"));

			if (lib == null)
			{
				throw new NoPomException(
				        "No pom.xml in the current jar or in the job directory. No lib/ directory in the current jar file.");
			}

			File dir = new File(lib);

			if (!dir.exists())
			{
				throw new NoPomException(
				        "No pom.xml in the current jar or in the job directory. No lib/ directory in the current jar file.");
			}

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
		}

		// 3rd: if pom, use pom!
		if (!cache.containsKey(job.getJd().getApplicationName()) && pomFile.exists())
		{
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

			URL[] tmp = new URL[deps.size()];
			int i = 0;
			for (Artifact artifact : deps)
			{
				tmp[i] = artifact.getFile().toURI().toURL();
				jqmlogger.debug("Artifact: " + artifact.getFile().toURI().toURL());
				i++;
			}

			// Put in cache
			this.cache.put(job.getJd().getApplicationName(), tmp);
		}
	}

	// Run
	@Override
	public void run()
	{
		String resultStatus = "";
		jqmlogger.debug("LOADER HAS JUST STARTED UP");

		// Create the CLASSPATH for our classloader (if not already in cache)
		try
		{
			classpathResolution(node);
		} catch (Exception e1)
		{
			jqmlogger.warn("Could not resolve CLASSPATH for job " + job.getJd().getApplicationName(), e1);
			endOfRun("CRASHED");
			return;
		}

		// Update of the job status, dates & co
		try
		{
			em.getTransaction().begin();
			em.refresh(this.h, LockModeType.PESSIMISTIC_READ);
			em.refresh(job, LockModeType.PESSIMISTIC_READ);

			Date date = new Date();
			Calendar executionDate = GregorianCalendar.getInstance(Locale.getDefault());
			executionDate.setTime(date);
			h.setExecutionDate(executionDate);

			// Add a message
			Helpers.createMessage("Status updated: RUNNING", h, em);
			if (job.getState() != "KILLED")
			{
				job.setState("RUNNING");
			}
			h.setStatus("RUNNING");
			em.getTransaction().commit();
			jqmlogger.debug("JobInstance was updated: " + job.getState());
		} catch (Exception e)
		{
			jqmlogger.error("Could not update internal elements", e);
			em.getTransaction().rollback();
			endOfRun("CRASHED");
			return;
		}

		// Get the default connection
		String defaultconnection = em.createQuery("SELECT gp.value FROM GlobalParameter gp WHERE gp.key = 'defaultConnection'",
		        String.class).getSingleResult();

		// Class loader switch
		JarClassLoader jobClassLoader = null;
		try
		{
			// Save the current class loader
			contextClassLoader = Thread.currentThread().getContextClassLoader();

			// At this point, the CLASSPATH is always in cache, so just create the CL with it.
			jobClassLoader = new JarClassLoader(jarUrl, cache.get(job.getJd().getApplicationName()));

			// Switch
			jqmlogger.debug("Setting class loader");
			Thread.currentThread().setContextClassLoader(jobClassLoader);
			jqmlogger.debug("Class Loader was set correctly");
		} catch (Exception e)
		{
			jqmlogger.error("Could not switch classloaders", e);
			endOfRun("CRASHED");
			return;
		}

		// Go! (launches the main function in the startup class designated in the manifest)
		jqmlogger.debug("+++++++++++++++++++++++++++++++++++++++");
		jqmlogger.debug("Job is running in the thread: " + Thread.currentThread().getName());
		jqmlogger.debug("JOB MAIN FUNCTION WILL BE INVOKED NOW");
		try
		{
			jobBase = jobClassLoader.invokeMain(job, defaultconnection, contextClassLoader, em);
			resultStatus = "ENDED";
		} catch (JqmKillException e)
		{
			jqmlogger.info("Job instance " + job.getId() + " has been killed.");
			resultStatus = "KILLED";
		} catch (Exception e)
		{
			jqmlogger.info("Job instance " + job.getId() + " has crashed. Exception was:", e);
			resultStatus = "CRASHED";
		}
		jqmlogger.debug("+++++++++++++++++++++++++++++++++++++++");

		// Job instance has now ended its run
		endOfRun(resultStatus);

		jqmlogger.debug("End of loader. Thread will now end");
		jqmlogger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		jqmlogger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

		em.close();

	}

	private void endOfRun(String status)
	{
		// Restore class loader
		if (this.contextClassLoader != null)
		{
			Thread.currentThread().setContextClassLoader(contextClassLoader);
			jqmlogger.debug("Class Loader was correctly restored");
		}

		// Retrieve the object to update
		em.getTransaction().begin();
		em.refresh(job, LockModeType.PESSIMISTIC_WRITE);
		em.refresh(h);// , LockModeType.PESSIMISTIC_WRITE);

		jqmlogger.debug("Job status after execution: " + job.getState());
		jqmlogger.debug("Progression after execution: " + job.getProgress());
		p.decreaseNbThread();
		jqmlogger.debug("Post execution, the number of running threads for the current queue is " + p.getActualNbThread());

		// Update end date
		Date datee = new Date();
		Calendar endDate = GregorianCalendar.getInstance(Locale.getDefault());
		endDate.setTime(datee);
		h.setEndDate(endDate);

		// STATE UPDATED
		job.setState(status);
		jqmlogger.debug("In the Loader --> ENDED: HISTORY: " + h.getId());
		Helpers.createMessage("Status updated: " + status, h, em);
		h.setReturnedValue(0);
		h.setStatus(status);

		jqmlogger.debug("Final status: " + job.getState());
		jqmlogger.debug("Final progression: " + job.getProgress());

		// Deliverables
		try
		{
			processDeliverables();
		} catch (JqmEngineException e)
		{
			jqmlogger.warn("Could not retrieve deliverables from the result object. Won't impact the engine.", e);
		}

		// The temporary file containing the extracted jar must be deleted if it exists
		jqmlogger.debug("The tmp directory will be removed");
		try
		{
			FileUtils.deleteDirectory(new File(CheckFilePath.fixFilePath(node.getRepo()
			        + CheckFilePath.fixFilePath(job.getJd().getFilePath()))
			        + "tmp" + job.getId() + "/"));
		} catch (IOException e)
		{
			jqmlogger.warn("Could not delete a temp file. It may result in filling up the file system", e);
		}

		if (pomCustom)
		{
			pomFile.delete();
		}

		// Send e-mail
		if (job.getEmail() != null)
		{
			try
			{
				Mail mail = new Mail(job, em);
				mail.send();
			} catch (Exception e)
			{
				jqmlogger.warn("An e-mail could not be sent. No impact on the engine.", e);
			}
		}

		// Done
		if (job.getState() == "ENDED")
		{
			em.remove(job);
		}
		em.getTransaction().commit();
	}

	// This method must be called inside an active JPA transaction.
	@SuppressWarnings("unchecked")
	private void processDeliverables() throws JqmEngineException
	{
		if (this.jobBase == null)
		{
			return;
		}

		Method m;
		ArrayList<Object> dss;
		try
		{
			m = this.jobBase.getClass().getMethod("getSha1s");
			dss = (ArrayList<Object>) m.invoke(jobBase, null);
		} catch (Exception e1)
		{
			throw new JqmEngineException("The jobBase object is invalid: could not retrieve deliverables", e1);
		}

		for (Object ds : dss)
		{
			try
			{
				String filePath = (String) ds.getClass().getMethod("getFilePath", null).invoke(ds, null);
				String fileName = (String) ds.getClass().getMethod("getFileName", null).invoke(ds, null);
				String hashPath = (String) ds.getClass().getMethod("getHashPath", null).invoke(ds, null);
				String fileFamily = (String) ds.getClass().getMethod("getFileFamily", null).invoke(ds, null);

				jqmlogger.debug("Job " + job.getId() + " has created a file: " + fileName + " - " + hashPath + " - " + fileFamily);
				Helpers.createDeliverable(filePath, fileName, hashPath, fileFamily, this.job.getId(), em);
				jqmlogger.debug("Job " + job.getId() + " has finished registering file " + fileName);
			} catch (Exception e)
			{
				jqmlogger
				        .error("Could not analyse a deliverbale - it may be of an incorrect Java class. Job has run correctly - it's only missing its produce.",
				                e);
			}
		}
	}

	public String getLib()
	{
		return lib;
	}

	public void setLib(String lib)
	{
		this.lib = lib;
	}
}
