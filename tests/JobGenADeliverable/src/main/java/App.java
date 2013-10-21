import java.io.FileWriter;
import java.io.PrintWriter;

import com.enioka.jqm.api.JobBase;

public class App extends JobBase{

	@Override
	public void start() {
		String file = this.getParameters().get("filepath");
//		String fileName = this.getParameters().get("fileName");
//		System.out.println("jobGEN: " + fileName);
		try{
			PrintWriter out  = new PrintWriter(new FileWriter(file + "JobGenADeliverable.txt"));
			out.println("Hello World!");
			out.close();

			addDeliverable(file, "JobGenADeliverable.txt", "JobGenADeliverableFamily");
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
