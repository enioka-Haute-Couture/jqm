import com.enioka.jqm.api.JobBase;




public class Entry extends JobBase{


	@Override
	public void start() {

		System.out.println("testJobCallAJob: " + Thread.currentThread().getName());

		LaunchJob t = new LaunchJob(this.parameters.get(0).getValue());
		t.run();

		System.out.println("FINISH");
	}
}
//	public static void main(String[] args) {
//
//		System.out.println("testJobCallAJob: " + Thread.currentThread().getName());
//
//				try {
//			        JarClassLoader cl = new JarClassLoader(new URL("file:/Users/pico/Dropbox/projets/enioka/tests/DemoMaven/target/DemoMaven-0.0.1-SNAPSHOT.jar"));
//			        cl.invokeMain();
//		        } catch (MalformedURLException e) {
//			        // TODO Auto-generated catch block
//			        e.printStackTrace();
//		        } catch (ClassNotFoundException e) {
//			        // TODO Auto-generated catch block
//			        e.printStackTrace();
//		        } catch (NoSuchMethodException e) {
//			        // TODO Auto-generated catch block
//			        e.printStackTrace();
//		        } catch (InvocationTargetException e) {
//			        // TODO Auto-generated catch block
//			        e.printStackTrace();
//		        } catch (IOException e) {
//			        // TODO Auto-generated catch block
//			        e.printStackTrace();
//		        }
//			}
