/**
 *
 */
package jarloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Pierre COPPEE <pierre.coppee@enioka.com>
 *
 */
public class JarLoader extends ClassLoader{

	private ArrayList<Class<?>> jars = new ArrayList<Class<?>>();

	public JarLoader() {

	}

	public void loadJar(File file) {

		JarFile jarFile;

		try {
			// -----------------------------------------------------------
			jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();

			while (entries.hasMoreElements()) {
				JarEntry j = entries.nextElement();
				if (!j.getName().endsWith(".class")) {
					continue;
				}

				InputStream is = jarFile.getInputStream(j);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				int c;

				while ((c = is.read()) != -1) {
					output.write(c);
				}

				byte[] stock_data = output.toByteArray();
				Class<?> cl = defineClass(null, stock_data, 0, stock_data.length);
				jars.add(cl);

			}
			// -----------------------------------------------------------
		} catch (IOException e) {
			e.printStackTrace();
		}


		for (int i = 0; i < jars.size(); i++) {

			@SuppressWarnings("rawtypes")
			Class[] params = new Class[1];
			params[0] = String[].class;
			Method m = null;

			try {
				// -----------------------------------------------------------
				m = jars.get(i).getDeclaredMethod("main", String[].class);
				String[] mParams = null;
				m.invoke(null, (Object) mParams);
				// -----------------------------------------------------------
			} catch (NoSuchMethodException ex) {
				Logger.getLogger(JarLoader.class.getName()).log(Level.SEVERE, null, ex);
			} catch (SecurityException ex) {
				Logger.getLogger(JarLoader.class.getName()).log(Level.SEVERE, null, ex);
			} catch (InvocationTargetException ex) {
				Logger.getLogger(JarLoader.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IllegalAccessException ex) {
				Logger.getLogger(JarLoader.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IllegalArgumentException ex) {
				Logger.getLogger(JarLoader.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
