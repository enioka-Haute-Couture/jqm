import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobBase;
import com.enioka.jqm.api.JobDefinition;



public class App extends JobBase{

	@Override
    public void start()
	{
		System.out.println("BEGINING GEOJOB: " + this.parameters.get("nbJob"));
		JobDefinition jd = new JobDefinition("Geo", "MAG");
		jd.addParameter("nbJob", (Integer.parseInt(this.parameters.get("nbJob") + 1) + ""));

		System.out.println("LAUNCHING FIRST JOB");
		Dispatcher.enQueue(jd);
		System.out.println("LAUNCHING SECOND JOB");
		Dispatcher.enQueue(jd);
		System.out.println("ENDING GEOJOB");
	}
}
