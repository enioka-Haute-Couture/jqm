
package com.enioka.jqm.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.enioka.jqm.temp.Polling;

public class ThreadPool {

	public ThreadPool(Polling p, int nbThread) {

		ExecutorService pool = Executors.newFixedThreadPool(nbThread);
		// try {
		pool.submit(new Loader(p.getJob().get(0)));
		// pool.shutdown();

		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

}
