import com.enioka.jqm.api.JobBase;

public class App extends JobBase {

	@Override
	public void start() {
		System.out.println("PRINTARG: ");

		for (int i = 0; i < this.parameters.size(); i++) {
			System.out.println("PARAMETER n°" + i);
			System.out.println(this.parameters.get(i).getValue());
		}
	}
}
