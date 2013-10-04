
package com.enioka.jqm.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.enioka.jqm.temp.Polling;

public class ThreadPool {

	ExecutorService pool = Executors.newFixedThreadPool(Main.dp.getNbThread());

	public ThreadPool(Polling p) {

		// try {
		pool.submit(new Loader(p.getJob().get(0)));
		// pool.shutdown();

		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

}
