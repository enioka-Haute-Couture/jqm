package com.enioka.jqm.shared.threads;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseSimplePoller implements Runnable
{
    protected static Logger jqmlogger = LoggerFactory.getLogger(BaseSimplePoller.class);

    protected Semaphore loop = new Semaphore(0);
    protected boolean run = true;
    protected String name;

    public void stop()
    {
        jqmlogger.info("{} internal poller has received a stop request", name);
        this.run = false;
        forceLoop();
    }

    protected void forceLoop()
    {
        this.loop.release(1);
    }

    public Thread start(String name)
    {
        this.name = name;
        var t = new Thread(this);
        t.start();
        return t;
    }

    @Override
    public void run()
    {
        jqmlogger.info("Starting {} internal poller", name);
        Thread.currentThread().setName(name + "_POLLER;polling;");
        var period = getPeriod();

        while (true)
        {
            // Boilerplate for waiting the right period with an escape hatch
            try
            {
                loop.tryAcquire(period, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                run = false;
            }
            if (!run)
            {
                break;
            }

            // Actual work
            pollingLoopWork();
        }

        jqmlogger.info("{} internal poller has stopped", name);
    }

    protected abstract void pollingLoopWork();

    protected abstract long getPeriod();
}
