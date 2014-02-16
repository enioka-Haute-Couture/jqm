package com.enioka.jqm.tools;

import java.util.Calendar;

public interface LoaderMBean
{
    /**
     * tries to kill the job instance
     * 
     */
    void kill();

    String getApplicationName();

    Calendar getEnqueueDate();

    String getKeyword1();

    String getKeyword2();

    String getKeyword3();

    String getModule();

    String getUser();

    String getSessionId();

    Integer getId();

    Long getRunTimeSeconds();
}
