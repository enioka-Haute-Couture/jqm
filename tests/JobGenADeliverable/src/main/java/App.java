import java.io.FileWriter;
import java.io.PrintWriter;

import com.enioka.jqm.api.JobBase;

public class App extends JobBase{

	@Override
	public void start() {
		String file = this.getParameters().get("filepath");
		try{
			PrintWriter out  = new PrintWriter(new FileWriter(file));
			out.println("Hello World!");
			out.close();

			addDeliverable(file, "JobGenADeliverable");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

//	public static void main(String[] args) {
//
//		String file = "/Users/pico/Downloads/tests/JobGenADeliverable.txt";
//		try{
//			PrintWriter out  = new PrintWriter(new FileWriter(file));
//			out.println("Hello World!");
//			out.close();
//
//			//addDeliverable("/Users/pico/Downloads/tests/JobGenADeliverable.txt", "JobGenADeliverable");
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//    }
}
