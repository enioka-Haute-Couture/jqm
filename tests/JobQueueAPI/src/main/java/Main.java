import mcd.JobDefinition;
import mcd.Queue;
import temp.Dispatcher;
import tools.CreationTools;



public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Queue qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42 , 100);
		Queue qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7 , 100);
		Queue qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 0 , 100);

		JobDefinition jd = CreationTools.createJobDefinition(true, "MarsuClassName", "/Users/pico/Dropbox/projets/enioka/jqm/JobQueueAPI/", qVip,
				42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true);
		Dispatcher.enQueue(jd);
		CreationTools.close();
	}

}
