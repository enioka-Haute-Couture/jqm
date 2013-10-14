import com.enioka.jqm.api.JobBase;


public class App extends JobBase{

	@Override
    public void start()
	{
		System.out.println("PRINTARG: ");
		System.out.println(this.parameters.get(0).getValue());
	}
}
