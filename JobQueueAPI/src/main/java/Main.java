import mcd.JobDefinition;
import temp.Dispatcher;
import tools.CreationTools;



public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CreationTools c = new CreationTools();

		JobDefinition jd = c.initJobDefinition("MarsuIsClass", "france/AmeriqueSud/Palombie", c.initQueue("VIPQueue", "Queue for the winners", 42 , 100));
		Dispatcher.enQueue(jd);
		c.close();
	}

}
