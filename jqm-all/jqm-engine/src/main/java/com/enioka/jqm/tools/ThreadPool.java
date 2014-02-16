/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.tools;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.Queue;

class ThreadPool
{
    private static Logger jqmlogger = Logger.getLogger(ThreadPool.class);
    private Queue queue = null;
    private int nbThread = 0;
    private ExecutorService pool = null;
    private Map<String, URL[]> cache = null;

    ThreadPool(Queue queue, int n, Map<String, URL[]> cache)
    {
        this.queue = queue;
        this.cache = cache;
        nbThread = n;
        pool = Executors.newFixedThreadPool(nbThread);
    }

    void run(com.enioka.jqm.jpamodel.JobInstance ji, Polling p)
    {
        jqmlogger.info("Job instance will be inserted inside a thread pool: " + ji.getId());
        jqmlogger.debug("ThreadPool ActualNbThread: " + p.getCurrentActiveThreadCount());
        pool.submit(new Loader(ji, cache, p));
    }

    void stop()
    {
        jqmlogger.debug("A thread pool will now try to stop");
        this.pool.shutdown();
        jqmlogger.debug("A thread pool has stopped properly");
    }

    Queue getQueue()
    {
        return queue;
    }

    int getNbThread()
    {
        return nbThread;
    }
}
