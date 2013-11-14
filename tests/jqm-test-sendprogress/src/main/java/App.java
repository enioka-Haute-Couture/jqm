import com.enioka.jqm.api.JobBase;



public class App extends JobBase
{
	@Override
	public void start()
	{
		try {
			sendProgress(10);
			Thread.sleep(1000);
			sendProgress(30);
			Thread.sleep(1000);
			sendProgress(60);
			Thread.sleep(1000);
			sendProgress(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}