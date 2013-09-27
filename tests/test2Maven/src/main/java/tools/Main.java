/**
 *
 */
package tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.eclipse.aether.resolution.ArtifactResult;

import dependencies.Dependencies;
import dependencies.DependencyResolver;


/**
 * @author Pierre COPPEE <pierre.coppee@enioka.com>
 *
 */
public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		// ------------- NORMAL CLASSLOADER -----------------
//		File file = new File("/Users/pico/Dropbox/projets/enioka/DemoMaven/DemoMaven-1.0-SNAPSHOT");
//		JarLoader j = new JarLoader();
//
//		j.loadJar(file);

		// ------------- URL CLASSLOADER --------------------
//        URL url;
//		try
//		{
//			url = file.toURI().toURL();
//			URL[] urls = {url};
//	        ClassLoader loader = new URLClassLoader(urls);
//	        Class<?> myClass = loader.loadClass("com.enioka.demomaven.Main");
//	        Method m = null;
//	        m = myClass.getDeclaredMethod("main", String[].class);
//	        String[] mParams = null;
//	        m.invoke(null, (Object) mParams);
//
//		} catch (MalformedURLException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SecurityException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// ----------------- TESTING CLASSLOADER -----------------
		DependencyResolver resolver;
		try
		{
			//ClassLoader original = ClassLoader.getSystemClassLoader();
			// ---------- Lecture des dependences ----------------
			// NEED: pom.xml file path
			Dependencies plugMeIn = new Dependencies("/Users/pico/Dropbox/projets/enioka/jqm/JobQueueAPI/pom.xml");
			plugMeIn.print();
			// ---------- Fin de la lecture des dependences ------
			// NEED: Maven file path
			resolver = new DependencyResolver(new File(System.getProperty("user.home") + "/.m2/repository"), "http://repo1.maven.org/maven2/");
			// DOIT PRENDRE UNE LISTE EN PARAMETRE POUR PRENDRE TOUTES LES DEPENDENCES
			ArrayList<DependencyResolver.ResolveResult> result = resolver.resolve(plugMeIn.getList());

			ArrayList<URL> artifactUrls = new ArrayList<URL>();
			for (int i = 0; i < result.size(); i++) {
				//System.out.println("ARTIFACT: " + result.get(i).toString());
				for (ArtifactResult artRes : result.get(i).artifactResults) {
					artifactUrls.add(artRes.getArtifact().getFile().toURI().toURL());
				}
			}
			//final URLClassLoader urlClassLoader = new URLClassLoader(artifactUrls.toArray(new URL[artifactUrls.size()]));
			ClassLoader old = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(new URLClassLoader(artifactUrls.toArray(new URL[artifactUrls.size()])));
			File file = new File("/Users/pico/Dropbox/projets/enioka/jqm/JobQueueAPI/target/testJobQueueAPI-0.0.1-SNAPSHOT.jar");
			URL[] url = {file.toURI().toURL()};
			ClassLoader cl = Thread.currentThread().getContextClassLoader();

			URL[] tmp = ((URLClassLoader)old).getURLs();

			for (URL u:tmp)
			{
				System.out.println("ClassPath:" + u.getFile());
			}
			System.out.println();
			URLClassLoader urlClassLoader = new URLClassLoader(url, cl);

			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");
			System.getProperty("java.class.path");
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++");

			Class<?> clazz = urlClassLoader.loadClass("Main");

			Method method = clazz.getDeclaredMethod("main", String[].class);
			String[] mParams = null;
			System.out.println("---------------------------------------------------");
			System.out.println(cl.getResource("META-INF/persistence.xml"));
			System.out.println("---------------------------------------------------");
			System.out.println("Result: " + method.invoke(null, (Object) mParams));
			Thread.currentThread().setContextClassLoader(old);
		} catch (IOException e)
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
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

}