import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;



public class LaunchJob extends Thread{

	String value = null;

	public LaunchJob(String value) {

		this.value = value;

	}

	@Override
	public void run() {

		System.out.println("JobCalled: " + Thread.currentThread().getName());
		JarClassLoader jobClassLoader = null;
		// Go! (launches the main function in the startup class designated in
		// the manifest)
		try {

			// Create our classloader


			jobClassLoader = new JarClassLoader(
					new URL(
							"file:" + value));

	        jobClassLoader.invokeMain();

        } catch (ClassNotFoundException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (NoSuchMethodException e) {
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
