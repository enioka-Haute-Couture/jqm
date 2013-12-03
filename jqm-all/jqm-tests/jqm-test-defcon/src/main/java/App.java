import com.enioka.jqm.api.JobBase;

public class App extends JobBase
{

	@Override
	public void start()
	{
		try
		{
			System.out.println(this.getDefaultConnection().getClass());
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("arg", e);
		}
	}
}
