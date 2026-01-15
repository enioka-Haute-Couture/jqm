package com.enioka.jqm.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.client.api.JqmDbClientFactory;
import com.enioka.jqm.jdbc.DatabaseException;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.QueryResult;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.Node;
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
    private static Logger jqmlogger = LoggerFactory.getLogger(CronScheduler.class);

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

        try {
            String timezone = cnx.runSelectSingle("get_timezone_psql", String.class);
            jqmlogger.info("Timezone is {}", timezone);
            String date = cnx.runSelectSingle("get_witness_timestamp", String.class);
            jqmlogger.info("Witness timestamp is {}", date);
        }
        catch (DatabaseException ex)
        {
            jqmlogger.info("Error while accessing database for timezone and witness timestamp");
        }
        try {

            jqmlogger.info("Inserting into witness : {}", this.node.getId());
            cnx.runUpdate("w_insert", this.node.getId());
            cnx.commit();
            jqmlogger.info("This node is the first scheduling node to start inside the cluster");
        }
        catch (DatabaseException ex)
        {
            jqmlogger.info("Could not insert into witness, another node is probably already the master scheduler.");
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
        stopScheduler();
        this.t.interrupt();
        // No need to wait... only daemon threads here, so cannot prevent engine shutdown.
    }

    @Override
    public void run()
    {
        Thread.currentThread().setName("Scheduler keepalive");
        jqmlogger.info("Start of the manager of the scheduler (not the scheduler itself)");
        while (run)
        {
            QueryResult qr = null;

            try (DbConn cnx = Helpers.getNewDbSession())
            {
                // Try to take the lead.
                jqmlogger.info("Trying to take the lead in the scheduler cluster. Node id : {}, ", this.node.getId());
                qr = cnx.runUpdate("w_update_take", this.node.getId(), this.node.getId(), this.node.getId(),
                        (int) (schedulerKeepAlive * 1.2 / 1000));
                cnx.commit();
                jqmlogger.info("Scheduler time is {}", (int) (schedulerKeepAlive * 1.2 / 1000));
                jqmlogger.info("qr is {}", qr.toString());
            }
            catch (DatabaseException ex)
            {
                // Always stop when DB issue.
                jqmlogger.error("Could not retrieve scheduler status from database", ex);
                stopScheduler();
                waits();
                continue;
            }

            if (qr.nbUpdated == 1)
            {
                if (!masterScheduler)
                {
                    jqmlogger.info("This node is being promoted to master scheduler");
                    startScheduler();
                }
                else
                {
                    jqmlogger.info("This node is confirmed as master scheduler");
                }
            }
            else
            {
                if (masterScheduler)
                {
                    jqmlogger.info("This node is no longer master scheduler");
                    stopScheduler();
                }
                else
                {
                    jqmlogger.info("This node is confirmed as slave scheduler");
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
        jqmlogger.info("Trying to get all cron tasks");
        try (DbConn cnx = Helpers.getNewDbSession())
        {
            for (ScheduledJob sj : ScheduledJob.select(cnx, "sj_select_all"))
            {
                jqmlogger.info("New cron expression : {} from the scheduled job : {}", sj.getCronExpression(), sj);
                res.add(new SchedulingPattern(sj.getCronExpression()), new JqmTask(sj));
            }

            // Also check delayed jobs
            cnx.runUpdate("ji_update_delayed");
            cnx.commit();
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
            JqmDbClientFactory.getClient().newJobRequest("", "cron").setScheduleId(sj.getId()).enqueue();
        }

    }

}
