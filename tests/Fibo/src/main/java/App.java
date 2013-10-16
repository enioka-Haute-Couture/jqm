import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobBase;
import com.enioka.jqm.api.JobDefinition;



public class App extends JobBase{

	@Override
    public void start() {

		System.out.println("PARAMETRE FIBO 2: " + this.parameters.get(0).getValue());

        JobDefinition j = new JobDefinition("Fibo");

        j.addParameter("p1", this.parameters.get(0).getValue());
		j.addParameter("p2", (Integer.parseInt(this.parameters.get(1).getValue())
				+ Integer.parseInt(this.parameters.get(0).getValue()) + ""));
		System.out.println("AVANT ENQUEUE");
		Dispatcher.enQueue(j);
		System.out.println("QUIT FIBO");
	}
}
