
package com.enioka.jqm.tools;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.enioka.jqm.jpamodel.Queue;

public class ThreadPool {

	private Queue queue = null;
	private int nbThread = 0;
	ExecutorService pool = null;
	Map<String, ClassLoader> cache = null;

	public ThreadPool(Queue queue, int n, Map<String, ClassLoader> cache) {

		this.queue = queue;
		this.cache = cache;
		nbThread = n;
		pool = Executors.newFixedThreadPool(nbThread);
	}

	public void run(com.enioka.jqm.jpamodel.JobInstance ji) {

		System.out.println("AVANT LOADER");
		System.out.println("JOB WILL BE POOLED: " + ji.getId());

		pool.submit(new Loader(ji, cache));

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
