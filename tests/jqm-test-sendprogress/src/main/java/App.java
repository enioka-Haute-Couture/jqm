import com.enioka.jqm.api.JobBase;



public class App extends JobBase
{
	@Override
	public void start()
	{
		for (int i = 0; i <= 1500; i++)
		{
			//System.out.println(i);
			try {
				if (i == 500 || i == 1000 || i == 1500) {
					Thread.sleep(1000);
					sendProgress(i);
					System.out.println("Progress: " + i);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}