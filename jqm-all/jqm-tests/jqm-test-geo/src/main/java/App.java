import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.api.JobBase;

public class App extends JobBase
{

	@Override
	public void start()
	{
		System.out.println("BEGINING GEOJOB: " + this.parameters.get("nbJob"));
		Map<String, String> p = new HashMap<String, String>();
		p.put("nbJob", ((Integer.parseInt(this.parameters.get("nbJob")) + 1) + ""));

		if (Integer.parseInt(this.parameters.get("nbJob")) >= 5)
		{
			System.out.println("END OF GEO - reached 5th generation");
			return;
		}

		sendMsg("launching first job");
		System.out.println("LAUNCHING FIRST JOB");
		enQueue("Geo", "Franquin", null, sessionID, application, module, other1, other2, other3, parentID, canBeRestart, p);
		sendMsg("launching second job");
		System.out.println("LAUNCHING SECOND JOB");
		enQueue("Geo", "Franquin", null, sessionID, application, module, other1, other2, other3, parentID, canBeRestart, p);
		System.out.println("ENDING GEOJOB");
	}
}
