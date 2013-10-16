
package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.enioka.jqm.api.JobBase;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.temp.DeliverableStruct;
import com.jcabi.aether.Aether;

public class Loader implements Runnable {

	JobInstance job = null;
	JobBase jobBase = new JobBase();
	ArrayList<DeliverableStruct> s1s = new ArrayList<DeliverableStruct>();
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
	EntityManager em = emf.createEntityManager();
	Map<String, ClassLoader> cache = null;
	boolean isInCache = true;
	Logger jqmlogger = Logger.getLogger(this.getClass());

	public Loader(JobInstance job, Map<String, ClassLoader> cache) {

		this.job = job;
		this.cache = cache;
	}

	public void crashedStatus() {

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		// STATE UPDATED

		em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j").setParameter("j", job.getId()).setParameter("msg", "CRASHED")
		        .executeUpdate();

		// MESSAGE HISTORY UPDATED

		History h = em.createQuery("SELECT h FROM History h WHERE h.id = :j", History.class).setParameter("j", job.getId()).getSingleResult();

		CreationTools.createMessage("Status updated: ATTRIBUTED", h, em);

		transac.commit();
	}

	@Override
	public void run() {

		try {

			System.out.println("TOUT DEBUT LOADER");
			// Main.p.updateExecutionDate();

			// ---------------- BEGIN: MAVEN DEPENDENCIES ------------------

			File local = new File(System.getProperty("user.home") + "/.m2/repository");
			File jar = new File(job.getJd().getJarPath());
			URL jars = jar.toURI().toURL();
			ArrayList<URL> tmp = new ArrayList<URL>();
			Collection<Artifact> deps = null;

			// Update of the job status
			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance = :j", History.class).setParameter("j", job).getSingleResult();

			CreationTools.createMessage("Status updated: RUNNING", h, em);

			EntityTransaction transac = em.getTransaction();
			transac.begin();

			em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j)").setParameter("j", job.getId()).setParameter("msg", "RUNNING")
			        .executeUpdate();
			transac.commit();

			if (!cache.containsKey(job.getJd().getApplicationName())) {

				Dependencies dependencies = new Dependencies(job.getJd().getFilePath() + "pom.xml");

				isInCache = false;
				Collection<RemoteRepository> remotes = Arrays.asList(new RemoteRepository("maven-central", "default",
				        "http://repo1.maven.org/maven2/"), new RemoteRepository("eclipselink", "default",
				        "http://download.eclipse.org/rt/eclipselink/maven.repo/")

				);

				for (int i = 0; i < dependencies.getList().size(); i++) {
					deps = new Aether(remotes, local).resolve(new DefaultArtifact(dependencies.getList().get(i)), "compile");
				}

				if (deps != null) {
					for (Artifact artifact : deps) {
						tmp.add(artifact.getFile().toURI().toURL());
						System.out.println("Artifact: " + artifact.getFile().toURI().toURL());
					}
				}
			}
			// ------------------- END: MAVEN DEPENDENCIES ---------------

			// We save the actual classloader
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			JarClassLoader jobClassLoader = null;
			URL[] urls = tmp.toArray(new URL[tmp.size()]);

			if (!isInCache) {
				jobClassLoader = new JarClassLoader(jars, urls);
				cache.put(job.getJd().getApplicationName(), jobClassLoader);
			} else
				jobClassLoader = (JarClassLoader) cache.get(job.getJd().getApplicationName());

			// Change active class loader
			Thread.currentThread().setContextClassLoader(jobClassLoader);

			// Go! (launches the main function in the startup class
			// designated
			// in
			// the manifest)
			System.out.println("+++++++++++++++++++++++++++++++++++++++");
			System.out.println("Je suis dans le thread " + Thread.currentThread().getName());
			System.out.println("AVANT INVOKE MAIN");
			jobBase = jobClassLoader.invokeMain(job);

			System.out.println("+++++++++++++++++++++++++++++++++++++++");

			// Restore class loader
			Thread.currentThread().setContextClassLoader(contextClassLoader);

			if (this.jobBase.getSha1s().size() != 0) {
				for (int j = 0; j < this.jobBase.getSha1s().size(); j++) {

					System.out.println("SHA1: " + this.jobBase.getSha1s().get(j).getFilePath());

					CreationTools.createDeliverable(this.jobBase.getSha1s().get(j).getFilePath(), this.jobBase.getSha1s().get(j).getHashPath(),
					        this.jobBase.getSha1s().get(j).getFileFamily(), this.job.getId(), em);
				}
			}
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

			// STATE UPDATED

			em.getTransaction().begin();

			em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j").setParameter("j", job.getId()).setParameter("msg", "ENDED")
			        .executeUpdate();

			// MESSAGE HISTORY UPDATED
			em.getTransaction().commit();
			CreationTools.createMessage("Status updated: ENDED", h, em);

		} catch (DependencyResolutionException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (MalformedURLException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (ClassNotFoundException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (SecurityException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (NoSuchMethodException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (IllegalArgumentException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (InvocationTargetException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (IOException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (InstantiationException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (IllegalAccessException e) {
			crashedStatus();
			jqmlogger.info(e);
		} catch (Exception e) {
		} finally {
			try {
				em.close();
				emf.close();
			} catch (Exception e) {
			}
		}

	}

	public JobInstance getJob() {

		return job;
	}

	public void setJob(JobInstance job) {

		this.job = job;
	}

	public JobBase getJobBase() {

		return jobBase;
	}

	public void setJobBase(JobBase jobBase) {

		this.jobBase = jobBase;
	}
}
