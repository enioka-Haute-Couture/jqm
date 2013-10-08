
package com.enioka.jqm.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.temp.Polling;

public class ThreadPool {

	private Queue queue = null;
	private int nbThread = 0;

	public ThreadPool(Queue queue, int n) {

		this.queue = queue;
		nbThread = n;
	}

	public void run(Polling p) {

		ExecutorService pool = Executors.newFixedThreadPool(nbThread);
		// try {
		pool.submit(new Loader(p.getJob().get(0)));
		// pool.shutdown();

		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public Queue getQueue() {

		return queue;
	}

	public void setQueue(Queue queue) {

		this.queue = queue;
	}

	public int getNbThread() {

		return nbThread;
	}

	public void setNbThread(int nbThread) {

		this.nbThread = nbThread;
	}

}
