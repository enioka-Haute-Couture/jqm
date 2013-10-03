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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.persistence.EntityTransaction;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.temp.Polling;
import com.jcabi.aether.Aether;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Polling p = new Polling();
			Calendar executionDate = GregorianCalendar.getInstance(Locale
			        .getDefault());

			if (p.getJob() != null) {

				// Execution Date Updated

				History h = CreationTools.em
				        .createQuery(
				                "SELECT h FROM History h WHERE h.jobId = :j",
				                History.class)
				        .setParameter("j", p.getJob().get(0).getId())
				        .getSingleResult();

				System.out.println("history ID: " + h.getId());

				EntityTransaction transac = CreationTools.em.getTransaction();
				transac.begin();

				CreationTools.em
				        .createQuery(
				                "UPDATE History h SET h.executionDate = :date WHERE h.id = :h")
				        .setParameter("h", h.getId())
				        .setParameter("date", executionDate).executeUpdate();

				transac.commit();
				// Execution Date END

				File local = new File(System.getProperty("user.home")
				        + "/.m2/repository");
				Dependencies dependencies = new Dependencies(p.getJob().get(0)
				        .getJd().getFilePath()
				        + "pom.xml");
				File jar = new File(p.getJob().get(0).getJd().getFilePath()
				        + "target/DateTimeMaven-0.0.1-SNAPSHOT.jar");
				dependencies.print();
				URL jars = jar.toURI().toURL();

				Collection<RemoteRepository> remotes = Arrays
				        .asList(new RemoteRepository("maven-central",
				                "default", "http://repo1.maven.org/maven2/"),
				                new RemoteRepository("eclipselink", "default",
				                        "http://download.eclipse.org/rt/eclipselink/maven.repo/")

				        );

				ArrayList<URL> tmp = new ArrayList<URL>();
				Collection<Artifact> deps = null;

				// Update of the job status --> RUNNING
				p.executionStatus();

				// Clean the JobInstance list
				p.clean();

				for (int i = 0; i < dependencies.getList().size(); i++) {
					System.out.println("DEPENDENCIES" + i + ": "
					        + dependencies.getList().get(i));
					deps = new Aether(remotes, local).resolve(
					        new DefaultArtifact(dependencies.getList().get(i)),
					        "compile");
				}

				for (Artifact artifact : deps) {
					tmp.add(artifact.getFile().toURI().toURL());
					System.out.println("Artifact: "
					        + artifact.getFile().toURI().toURL());

				}
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
				jobClassLoader.invokeMain();
				System.out.println("+++++++++++++++++++++++++++++++++++++++");

				// Restore class loader
				Thread.currentThread()
				        .setContextClassLoader(contextClassLoader);
			}

		} catch (DependencyResolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
