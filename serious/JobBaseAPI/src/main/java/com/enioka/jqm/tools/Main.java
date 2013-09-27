package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.enioka.jqm.temp.Polling;
import com.jcabi.aether.Aether;

public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
	    try
		{
	    	Polling p = new Polling();
	    	File local = new File(System.getProperty("user.home") + "/.m2/repository");
	    	Dependencies dependencies = new Dependencies(p.getJob().getJd().getFilePath() + "pom.xml");
	    	File jar = new File(p.getJob().getJd().getFilePath() + "target/testJobQueueAPI-0.0.1-SNAPSHOT.jar");
	    	dependencies.print();
		    URL jars = jar.toURI().toURL();

		    Collection<RemoteRepository> remotes = Arrays.asList(
		      new RemoteRepository(
		        "maven-central",
		        "default",
		        "http://repo1.maven.org/maven2/"
		      ),
		      new RemoteRepository(
				        "eclipselink",
				        "default",
				        "http://download.eclipse.org/rt/eclipselink/maven.repo/"
				      )

		    );
		    ArrayList<URL> tmp = new ArrayList<URL>();
		    Collection<Artifact> deps = null;
		    for (int i = 0; i < dependencies.getList().size(); i++)
		    {
		    	System.out.println("DEPENDENCIES" + i +": " + dependencies.getList().get(i));
		    	deps = new Aether(remotes, local).resolve(
		    			new DefaultArtifact(dependencies.getList().get(i)),
		    			"compile"
		    			);
		    }

			for (Artifact artifact : deps)
			{
				tmp.add(artifact.getFile().toURI().toURL());
				System.out.println("Artifact: " + artifact.getFile().toURI().toURL());

			}
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

			URL[] urls = tmp.toArray(new URL[tmp.size()]);

			JarClassLoader jobClassLoader = new JarClassLoader(jars, urls);
			// Change active class loader
			Thread.currentThread().setContextClassLoader(jobClassLoader);

			// Go! (launches the main function in the startup class designated in
			// the manifest)
			jobClassLoader.invokeMain();

			// Restore class loader
			Thread.currentThread().setContextClassLoader(contextClassLoader);
//			urls.add(jars);
//
//
//		    URLClassLoader depClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
//		    //URLClassLoader urlClassLoader = new URLClassLoader(jars, null);
//		    Thread.currentThread().setContextClassLoader(depClassLoader);
//		    Class<?> clazz = depClassLoader.loadClass("Main");
//		    Method method = clazz.getDeclaredMethod("main", String[].class);
//		    String[] mParams = null;
//		    method.invoke(null, (Object) mParams);
//
//		    Thread.currentThread().setContextClassLoader(contextClassLoader);

		} catch (DependencyResolutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
