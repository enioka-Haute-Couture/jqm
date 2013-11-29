import com.enioka.jqm.api.JobBase;

public class App extends JobBase
{
	@Override
	public void start()
	{
		for (int i = 0; i <= 5000; i++)
		{
			// System.out.println(i);
			if (i == 500 || i == 1000 || i == 3500 || i == 5000)
			{
				sendProgress(i);
				System.out.println("Progress: " + i);
			}
			try
			{
				Thread.sleep(1);
			} catch (InterruptedException e)
			{
				// Nothing
			}
		}
	}
}