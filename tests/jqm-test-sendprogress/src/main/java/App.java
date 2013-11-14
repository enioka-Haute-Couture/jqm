import com.enioka.jqm.api.JobBase;



public class App extends JobBase
{
	@Override
	public void start()
	{
		for (int i = 0; i <= 1500; i++)
		{
			if (i == 500 || i == 1000 || i == 1500) {
				System.out.println("Progress: " + i);
				sendProgress(i);
			}
		}
	}
}