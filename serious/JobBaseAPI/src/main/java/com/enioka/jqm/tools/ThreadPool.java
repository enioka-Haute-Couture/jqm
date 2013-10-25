package com.enioka.jqm.tools;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.temp.Polling;

public class ThreadPool
{
	Logger jqmlogger = Logger.getLogger(ThreadPool.class);
	private Queue queue = null;
	private int nbThread = 0;
	ExecutorService pool = null;
	Map<String, URL[]> cache = null;

	public ThreadPool(Queue queue, int n, Map<String, URL[]> cache)
	{
		this.queue = queue;
		this.cache = cache;
		nbThread = n;
		pool = Executors.newFixedThreadPool(nbThread);
	}

	public void run(com.enioka.jqm.jpamodel.JobInstance ji, Polling p)
	{
		jqmlogger.info("Job instance will be inserted inside a thread pool: " + ji.getId());
		pool.submit(new Loader(ji, cache, p));
	}

	public Queue getQueue()
	{
		return queue;
	}

	public void setQueue(Queue queue)
	{
		this.queue = queue;
	}

	public int getNbThread()
	{
		return nbThread;
	}

	public void setNbThread(int nbThread)
	{
		this.nbThread = nbThread;
	}

}
