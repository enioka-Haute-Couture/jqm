import org.joda.time.DateTime;

import com.enioka.jqm.api.JobBase;

public class App extends JobBase{

	@Override
	public void start()
	{
		DateTime d = DateTime.now();
		System.out.println("Date GENERATED: " + d);
	}

}