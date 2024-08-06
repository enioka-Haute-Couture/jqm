package com.enioka.jqm.runner.shell.api.jmx;

import java.util.Calendar;

import javax.management.MXBean;

/**
 * JMX bean for running shell job instances
 */
@MXBean
public interface ShellJobInstanceTrackerMBean
{
    /**
     * Immediately kill the job instance/
     *
     */
    void kill();

    /**
     * The name of the Job Definition that was used for this launch.
     */
    String getApplicationName();

    /**
     * Time a request was made. (request time, no startup time)
     */
    Calendar getEnqueueDate();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getKeyword1();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getKeyword2();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getKeyword3();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getModule();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getUser();

    /**
     * An optional tag that can be defined at enqueue time.
     */
    String getSessionId();

    /**
     * The unique ID of the launch. It holds no special meaning.
     */
    Long getId();
}
