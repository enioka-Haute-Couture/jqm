
package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.enioka.jqm.jpamodel.JobInstance;
import com.jcabi.aether.Aether;

public class Loader implements Runnable {

	JobInstance job = null;

	public Loader(JobInstance job) {

		this.job = job;
	}

	public void crashedStatus() {

		EntityManagerFactory emf = Persistence
		        .createEntityManagerFactory("jobqueue-api-pu");
		EntityManager em = emf.createEntityManager();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		// STATE UPDATED

		em.createQuery(
		        "UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j")
		        .setParameter("j", job.getId()).setParameter("msg", "CRASHED")
		        .executeUpdate();

		// MESSAGE HISTORY UPDATED

		em.createQuery(
		        "UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
		                + "(SELECT h.id FROM History h WHERE h.jobId = :j)")
		        .setParameter("j", job.getId())
		        .setParameter("msg", "Status updated: CRASHED").executeUpdate();

		transac.commit();
		em.close();
		emf.close();
	}

	public void run() {

		try {

			Main.p.updateExecutionDate();

			System.out
			        .println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			System.out
			        .println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			System.out
			        .println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			System.out
			        .println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

			// ---------------- BEGIN: MAVEN DEPENDENCIES ------------------

			File local = new File(System.getProperty("user.home")
			        + "/.m2/repository");
			Dependencies dependencies = new Dependencies(job.getJd()
			        .getFilePath() + "pom.xml");
			File jar = new File(job.getJd().getFilePath()
			        + "target/DateTimeMaven-0.0.1-SNAPSHOT.jar");
			dependencies.print();
			URL jars = jar.toURI().toURL();

			Collection<RemoteRepository> remotes = Arrays
			        .asList(new RemoteRepository("maven-central", "default",
			                "http://repo1.maven.org/maven2/"),
			                new RemoteRepository("eclipselink", "default",
			                        "http://download.eclipse.org/rt/eclipselink/maven.repo/")

			        );

			ArrayList<URL> tmp = new ArrayList<URL>();
			Collection<Artifact> deps = null;

			// Update of the job status
			EntityManagerFactory emf = Persistence
			        .createEntityManagerFactory("jobqueue-api-pu");
			EntityManager em = emf.createEntityManager();
			EntityTransaction transac = em.getTransaction();
			transac.begin();

			em.createQuery(
			        "UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
			                + "(SELECT h.id FROM History h WHERE h.jobId = :j)")
			        .setParameter("j", job.getId())
			        .setParameter("msg", "Status updated: RUNNING")
			        .executeUpdate();

			em.createQuery(
			        "UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j)")
			        .setParameter("j", job.getId())
			        .setParameter("msg", "RUNNING").executeUpdate();
			// transac.commit();

			// Clean the JobInstance list
			// Main.p.clean();

			for (int i = 0; i < dependencies.getList().size(); i++) {
				deps = new Aether(remotes, local).resolve(new DefaultArtifact(
				        dependencies.getList().get(i)), "compile");
			}

			for (Artifact artifact : deps) {
				tmp.add(artifact.getFile().toURI().toURL());
				System.out.println("Artifact: "
				        + artifact.getFile().toURI().toURL());

			}
			// ------------------- END: MAVEN DEPENDENCIES ---------------

			// We save the actual classloader
			ClassLoader contextClassLoader = Thread.currentThread()
			        .getContextClassLoader();

			URL[] urls = tmp.toArray(new URL[tmp.size()]);

			JarClassLoader jobClassLoader = new JarClassLoader(jars, urls);

			// Change active class loader
			Thread.currentThread().setContextClassLoader(jobClassLoader);

			// Go! (launches the main function in the startup class
			// designated
			// in
			// the manifest)
			System.out.println("+++++++++++++++++++++++++++++++++++++++");
			System.out.println("Je suis dans le thread "
			        + Thread.currentThread().getName());
			jobClassLoader.invokeMain();
			System.out.println("+++++++++++++++++++++++++++++++++++++++");

			// Restore class loader
			Thread.currentThread().setContextClassLoader(contextClassLoader);

			// EntityManagerFactory emf = Persistence
			// .createEntityManagerFactory("jobqueue-api-pu");
			// EntityManager em = emf.createEntityManager();
			// transac = em.getTransaction();
			// transac.begin();

			// STATE UPDATED

			em.createQuery(
			        "UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j")
			        .setParameter("j", job.getId())
			        .setParameter("msg", "ENDED").executeUpdate();

			// MESSAGE HISTORY UPDATED

			em.createQuery(
			        "UPDATE Message m SET m.textMessage = :msg WHERE m.history.id = "
			                + "(SELECT h.id FROM History h WHERE h.jobId = :j)")
			        .setParameter("j", job.getId())
			        .setParameter("msg", "Status updated: ENDED")
			        .executeUpdate();

			transac.commit();
			em.close();
			emf.close();

		} catch (DependencyResolutionException e) {

			crashedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			crashedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			crashedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			crashedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			crashedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			crashedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			crashedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			crashedStatus();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
