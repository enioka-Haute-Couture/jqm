import com.enioka.jqm.api.JobBase;



public class App extends JobBase{

	@Override
    public void start()
	{
		System.out.println((this.parameters.containsValue("argument1")) ? true : false);
		System.out.println((this.parameters.containsValue("argument2")) ? true : false);
	}
}
