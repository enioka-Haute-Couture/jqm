package com.enioka.jqm.tools;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;

public class JarClassLoader extends URLClassLoader {

	URL jarUrl;

	public JarClassLoader(URL url) {
		super(new URL[] { url });
		this.jarUrl = url;
	}

	private static URL[] addUrls(URL url, URL[] libs)
	{
		URL[] urls = new URL[libs.length + 1];
		urls[0] = url;
		for (int i = 0; i< libs.length; i++)
			urls[i+1] = libs[i];
		return urls;
	}

	public JarClassLoader(URL url, URL[] libs) {
		super(addUrls(url, libs));
		this.jarUrl = url;
	}

	public JarClassLoader(URL url, URL[] libs, ClassLoader parent) {
		super(addUrls(url, libs), parent);
		this.jarUrl = url;
	}

	public String getMainClassName() throws IOException {
		URL u = new URL("jar", "", jarUrl + "!/");
		JarURLConnection uc = (JarURLConnection) u.openConnection();
		Attributes attr = uc.getMainAttributes();
		return attr != null ? attr.getValue(Attributes.Name.MAIN_CLASS) : null;
	}

	public void invokeClass(String name, String[] args)
			throws ClassNotFoundException, NoSuchMethodException,
			InvocationTargetException {
		@SuppressWarnings("rawtypes")
		Class c = loadClass(name);
		@SuppressWarnings("unchecked")
		Method m = c.getMethod("main", new Class[] { args.getClass() });
		m.setAccessible(true);
		int mods = m.getModifiers();
		if (m.getReturnType() != void.class || !Modifier.isStatic(mods)
				|| !Modifier.isPublic(mods)) {
			throw new NoSuchMethodException("main");
		}
		try {
			m.invoke(null, new Object[] { args });
		} catch (IllegalAccessException e) {
			// This should not happen, as we have disabled access checks
		}
	}

	public void invokeMain() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IOException
	{
		this.invokeClass("Main", new String[] {});
	}
}
