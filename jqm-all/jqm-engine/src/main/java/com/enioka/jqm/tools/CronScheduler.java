package com.enioka.jqm.tools;

import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.QueryResult;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.Node;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.ScheduledJob;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import it.sauronsoftware.cron4j.TaskTable;

/**
 * All cron integration code is concentrated here. It holds the Scheduler itself (from cron4j lib, which itself hosts a thread pool) and the
 * thread to check if this node should be the one doing the scheduling in the cluster.
 *
 */
class CronScheduler implements Runnable, TaskCollector
{
    private static Logger jqmlogger = Logger.getLogger(CronScheduler.class);

    private Node node = null;
    private Integer schedulerKeepAlive;
    private Scheduler s;
    private boolean run = true;
    private boolean masterScheduler = false;
    private Thread t;

    public CronScheduler(JqmEngine e)
    {
        this.node = e.getNode();

        DbConn cnx = Helpers.getNewDbSession();
        this.schedulerKeepAlive = Integer.parseInt(GlobalParameter.getParameter(cnx, "schedulerKeepAlive", "30000"));

        boolean shouldStart = !Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "disableScheduler", "false"));
        if (!shouldStart)
        {
            jqmlogger.info("Scheduling functions are disabled");
            return;
        }

        try
        {
            cnx.runUpdate("w_insert", this.node.getId());
            cnx.commit();
            jqmlogger.debug("This node is the first scheduling node to start inside the cluster");
        }
        catch (DatabaseException ex)
        {
            // Ignore it (UK error). It means the line already exists inside the database.
        }
        cnx.close();

        // Start the thread
        t = new Thread(this);
        t.start();
    }

    void stop()
    {
        this.run = false;
        this.t.interrupt();
        // No need to wait... only daemon threads here, so cannot prevent engine shutdown.
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName("Scheduler keepalive");
        jqmlogger.info("Start of the scheduler (cron) manager");
        while (run)
        {
            DbConn cnx = null;
            QueryResult qr = null;

            try
            {
                cnx = Helpers.getNewDbSession();

                // Try to take the lead.
                Calendar limit = Calendar.getInstance();
                limit.add(Calendar.MILLISECOND, (int) (-this.schedulerKeepAlive * 1.2));
                qr = cnx.runUpdate("w_update_take", this.node.getId(), this.node.getId(), this.node.getId(), limit);
                cnx.commit();
            }
            catch (DatabaseException ex)
            {
                // Always stop when DB issue.
                jqmlogger.error("Could not retrieve scheduler status from database", ex);
                stopScheduler();
                waits();
                continue;
            }
            finally
            {
                Helpers.closeQuietly(cnx);
            }

            if (qr.nbUpdated == 1)
            {
                if (!masterScheduler)
                {
                    jqmlogger.info("This node is being promoted to master scheduler");
                    startScheduler();
                }
                jqmlogger.trace("This node is confirmed as master scheduler");
            }
            else
            {
                if (masterScheduler)
                {
                    jqmlogger.info("This node is no longer master scheduler");
                    stopScheduler();
                }
            }

            // Wait.
            waits();
        }

        jqmlogger.info("Scheduler (cron) manager has stopped");
    }

    private void waits()
    {
        try
        {
            Thread.sleep(this.schedulerKeepAlive);
        }
        catch (InterruptedException e)
        {
            if (Thread.currentThread().isInterrupted())
            {
                run = false;
                stopScheduler();
            }
        }
    }

    private void startScheduler()
    {
        s = new Scheduler();
        s.setDaemon(true);

        s.addTaskCollector(this);
        s.start();
        masterScheduler = true;
        jqmlogger.info("Scheduler (cron) has started");
    }

    private void stopScheduler()
    {
        if (s != null && s.isStarted())
        {
            s.stop();
            jqmlogger.info("Scheduler (cron) is now down");
        }
        s = null;
        masterScheduler = false;
    }

    @Override
    public TaskTable getTasks()
    {
        TaskTable res = new TaskTable();

        DbConn cnx = null;

        try
        {
            cnx = Helpers.getNewDbSession();
            for (ScheduledJob sj : ScheduledJob.select(cnx, "sj_select_all"))
            {
                res.add(new SchedulingPattern(sj.getCronExpression()), new JqmTask(sj));
            }
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }

        return res;
    }

    private class JqmTask extends Task
    {
        private ScheduledJob sj;

        private JqmTask(ScheduledJob sj)
        {
            this.sj = sj;
        }

        @Override
        public void execute(TaskExecutionContext context) throws RuntimeException
        {
            DbConn cnx = null;
            JobDef jd = null;
            Queue q = null;
            try
            {
                cnx = Helpers.getNewDbSession();

                List<JobDef> jdd = JobDef.select(cnx, "jd_select_by_id", sj.getJobDefinition());
                if (jdd.size() != 1)
                {
                    throw new JqmRuntimeException("Cannot launch a schedule job without associated JobDef");
                }
                jd = jdd.get(0);

                if (sj.getQueue() != null && sj.getQueue() > 0)
                {
                    List<Queue> qq = Queue.select(cnx, "q_select_by_id", sj.getQueue());
                    q = qq.get(0);
                }
            }
            finally
            {
                Helpers.closeQuietly(cnx);
            }

            JobRequest jr = JobRequest.create(jd.getApplicationName(), "cron").addParameters(sj.getParameters()).setKeyword1("cron");
            if (q != null)
            {
                jr.setQueueName(q.getName());
            }

            JqmClientFactory.getClient().enqueue(jr);
        }

    }

}
