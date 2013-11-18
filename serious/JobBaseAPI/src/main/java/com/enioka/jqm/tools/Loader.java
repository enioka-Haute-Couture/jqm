/**
 * Copyright �� 2013 enioka. All rights reserved
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
import java.lang.reflect.InvocationTargetException;
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
import javax.persistence.EntityTransaction;

import org.apache.commons.io.FileUtils;
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
	private Object jobBase = null;
	private EntityManager em = Helpers.getNewEm();
	private Map<String, URL[]> cache = null;
	private Logger jqmlogger = Logger.getLogger(this.getClass());
	private Polling p = null;
	String res = null;
	String lib = null;
	URL[] libUrls = null;

	Loader(JobInstance job, Map<String, URL[]> cache, Polling p)
	{

		this.job = job;
		this.cache = cache;
		this.p = p;
	}

	// CrashedStatus
	protected void crashedStatus()
	{

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		// STATE UPDATED

		em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j").setParameter("j", job.getId())
				.setParameter("msg", "CRASHED").executeUpdate();

		// MESSAGE HISTORY UPDATED

		History h = em.createQuery("SELECT h FROM History h WHERE h.id = :j", History.class).setParameter("j", job.getId())
				.getSingleResult();

		Helpers.createMessage("Status updated: ATTRIBUTED", h, em);

		h.setReturnedValue(1);

		transac.commit();
	}

	// ExtractJar
	void extractJar(String jarFile, String destDir)
	{
		try
		{
			JarFile jar = new JarFile(jarFile);
			File tmp = new File(destDir + "tmp/");
			tmp.mkdir();
			destDir += "tmp/";
			Enumeration<JarEntry> enumm = jar.entries();
			while (enumm.hasMoreElements())
			{
				JarEntry file = enumm.nextElement();
				File f = new File(destDir + File.separator + file.getName());
				if (file.isDirectory())
				{ // if its a directory, create it
					f.mkdir();
					continue;
				}
				InputStream is = jar.getInputStream(file); // get the input stream
				FileOutputStream fos = new FileOutputStream(f);
				while (is.available() > 0)
				{ // write contents of 'is' to 'fos'
					fos.write(is.read());
				}
				fos.close();
				is.close();
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// FindFile
	void findFile(String path, File f)
	{
		File[] list = f.listFiles();
		if (list != null)
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

	// Run
	@Override
	public void run()
	{
		try
		{
			jqmlogger.debug("TOUT DEBUT LOADER");
			Node node = this.p.dp.getNode();

			// ---------------- BEGIN: MAVEN DEPENDENCIES ------------------
			File local = new File(System.getProperty("user.home") + "/.m2/repository");
			File jar = new File(CheckFilePath.fixFilePath(node.getRepo()) + job.getJd().getJarPath());
			URL jars = jar.toURI().toURL();
			jqmlogger.debug("Loader will try to launch jar " + jar.getAbsolutePath() + " - " + job.getJd().getJavaClassName());
			ArrayList<URL> tmp = new ArrayList<URL>();
			Collection<Artifact> deps = null;
			File pomFile = new File(CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath()))
					+ "pom.xml");
			jqmlogger.debug("Loader will try to load POM " + pomFile.getAbsolutePath());
			boolean pomCustom = false;

			jqmlogger.debug("Loader actualNbThread: " + p.getActualNbThread());

			if (!pomFile.exists())
			{
				pomCustom = true;
				jqmlogger.debug("POM doesn't exist, it will be get in the META-INF/pom.xml of the Jar file");
				String env = CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath()));

				extractJar(jar.getAbsolutePath(), env);
				findFile("pom.xml", new File(env + "tmp/META-INF/maven/"));

				jqmlogger.debug("pomdebug: " + res);

				InputStream is = new FileInputStream(res + "/pom.xml");
				OutputStream os = new FileOutputStream(CheckFilePath.fixFilePath(node.getRepo()
						+ CheckFilePath.fixFilePath(job.getJd().getFilePath()))
						+ "pom.xml");

				int r = 0;
				byte[] bytes = new byte[1024];

				while ((r = is.read(bytes)) != -1)
				{
					os.write(bytes, 0, r);
				}

				is.close();
				os.close();

				pomFile = new File(CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath()))
						+ "pom.xml");
			}
			if (!pomFile.exists())
			{
				String env = CheckFilePath.fixFilePath(node.getRepo() + CheckFilePath.fixFilePath(job.getJd().getFilePath()));
				findFile("lib", new File(env + "tmp/META-INF/maven/"));

				File dir = new File(res);

				if (!dir.exists())
				{
					throw new IOException();
				}

				FileFilter fileFilter = new WildcardFileFilter("*.jar");
				File[] files = dir.listFiles(fileFilter);
				libUrls = new URL[files.length];
				for (int i = 0; i < files.length; i++)
				{
					libUrls[i] = files[i].toURI().toURL();
				}
			}

			// The temporary file containing the extract jar must be deleted
			FileUtils.deleteDirectory(new File(CheckFilePath.fixFilePath(node.getRepo()
					+ CheckFilePath.fixFilePath(job.getJd().getFilePath()))
					+ "tmp"));

			// Update of the job status
			em.getTransaction().begin();
			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance = :j", History.class).setParameter("j", job)
					.getSingleResult();

			// Update of the execution date
			Date date = new Date();
			Calendar executionDate = GregorianCalendar.getInstance(Locale.getDefault());
			executionDate.setTime(date);

			h.setExecutionDate(executionDate);

			// End of the execution date

			Helpers.createMessage("Status updated: RUNNING", h, em);
			em.getTransaction().commit();

			EntityTransaction transac = em.getTransaction();
			transac.begin();

			job.setState("RUNNING");
			transac.commit();
			jqmlogger.debug("JobInstance was updated: " + job.getState());

			boolean isInCache = true;
			if (!cache.containsKey(job.getJd().getApplicationName()))
			{
				if (libUrls == null)
				{
					Dependencies dependencies = new Dependencies(pomFile.getAbsolutePath());

					if (pomCustom)
						pomFile.delete();

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

					isInCache = false;
					Collection<RemoteRepository> remotes = Arrays.asList(rr);

					deps = new ArrayList<Artifact>();
					for (int i = 0; i < dependencies.getList().size(); i++)
					{
						jqmlogger.info("Resolving Maven dep " + dependencies.getList().get(i));
						deps.addAll(new Aether(remotes, local).resolve(new DefaultArtifact(dependencies.getList().get(i)), "compile"));
					}

					for (Artifact artifact : deps)
					{
						tmp.add(artifact.getFile().toURI().toURL());
						jqmlogger.info("Artifact: " + artifact.getFile().toURI().toURL());
					}
				}
			}
			// ------------------- END: MAVEN DEPENDENCIES ---------------

			// We save the actual classloader
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			JarClassLoader jobClassLoader = null;
			URL[] urls = null;

			if (libUrls == null)
				urls = tmp.toArray(new URL[tmp.size()]);
			else
				urls = libUrls;

			if (!isInCache)
			{
				jobClassLoader = new JarClassLoader(jars, urls);
				cache.put(job.getJd().getApplicationName(), urls);
			}
			else
			{
				jobClassLoader = new JarClassLoader(jars, cache.get(job.getJd().getApplicationName()));
			}

			// Get the default connection

			String defaultconnection = em.createQuery("SELECT gp.value FROM GlobalParameter gp WHERE gp.key = 'defaultConnection'",
					String.class).getSingleResult();

			// Change active class loader
			jqmlogger.debug("Setting class loader");
			Thread.currentThread().setContextClassLoader(jobClassLoader);
			jqmlogger.info("Class Loader was set correctly");

			// Go! (launches the main function in the startup class designated in the manifest)
			jqmlogger.debug("+++++++++++++++++++++++++++++++++++++++");
			jqmlogger.debug("Job is running in the thread: " + Thread.currentThread().getName());
			jqmlogger.debug("AVANT INVOKE MAIN");
			jobBase = jobClassLoader.invokeMain(job, defaultconnection, contextClassLoader, em);
			jqmlogger.debug("+++++++++++++++++++++++++++++++++++++++");

			jqmlogger.debug("Job status after execution before contextClassLoader: " + job.getState());
			jqmlogger.debug("Progression after execution: " + job.getProgress());

			// Restore class loader
			Thread.currentThread().setContextClassLoader(contextClassLoader);

			jqmlogger.debug("ActualNbThread after execution: " + p.getActualNbThread());
			p.setActualNbThread(p.getActualNbThread() - 1);

			// em.getEntityManagerFactory().getCache().evictAll();
			// em.refresh(em.merge(job));

			// Update end date

			Date datee = new Date();
			Calendar endDate = GregorianCalendar.getInstance(Locale.getDefault());
			endDate.setTime(datee);

			em.getTransaction().begin();
			h.setEndDate(endDate);
			em.getTransaction().commit();

			jqmlogger.debug("Job status after execution: " + job.getState());

			// End end date

			// STATE UPDATED
			if (job.getState().equals("RUNNING"))
			{
				em.getTransaction().begin();
				// em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j").setParameter("j", job.getId())
				// .setParameter("msg", "KILLED").executeUpdate();
				job.setState("ENDED");
				em.merge(job);
				em.getTransaction().commit();
				jqmlogger.debug("In the Loader --> ENDED: HISTORY: " + h.getId());

				em.getTransaction().begin();
				Helpers.createMessage("Status updated: ENDED", h, em);
				em.getTransaction().commit();

				em.getTransaction().begin();
				h.setReturnedValue(0);
				em.persist(h);
				em.getTransaction().commit();
			}

			em.refresh(em.merge(job));

			jqmlogger.debug("Final status: " + job.getState());
			jqmlogger.debug("Final progression: " + job.getProgress());

			// Retrieve files created by the job
			Method m = this.jobBase.getClass().getMethod("getSha1s");
			ArrayList<Object> dss = (ArrayList<Object>) m.invoke(jobBase, null);

			for (Object ds : dss)
			{
				try
				{
					em.getTransaction().begin();
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
				} finally
				{
					em.getTransaction().commit();
				}
			}

			jqmlogger.debug("End of loader. Thread will now end");
			jqmlogger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			jqmlogger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

			// SEND EMAIL

			if (job.getEmail() != null)
			{
				Mail mail = new Mail(node, job, em);
				mail.send();
			}

			// END SEND EMAIL

		} catch (DependencyResolutionException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (MalformedURLException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (ClassNotFoundException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (SecurityException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (NoSuchMethodException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (IllegalArgumentException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (InvocationTargetException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (IOException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (InstantiationException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (IllegalAccessException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (JqmKillException e)
		{
			em.getTransaction().begin();
			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance = :j", History.class).setParameter("j", job)
					.getSingleResult();
			em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j").setParameter("j", job.getId())
					.setParameter("msg", "KILLED").executeUpdate();
			em.getTransaction().commit();
			jqmlogger.debug("In the Loader --> KILLED: HISTORY: " + h.getId());

			em.getTransaction().begin();
			Helpers.createMessage("Status updated: KILLED", h, em);
			em.getTransaction().commit();

			em.getTransaction().begin();
			h.setReturnedValue(0);
			em.getTransaction().commit();
		} catch (Exception e)
		{
			jqmlogger.error("An error occured during job execution or preparation: " + e.getMessage(), e);
		} finally
		{
			try
			{
				em.close();
			} catch (Exception e)
			{
			}
		}

	}
}
