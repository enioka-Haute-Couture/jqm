package com.enioka.jqm.api;

/**
 * An exception caused by a job instance runner (and not by the payload the runner was trying to run).
 */
public class JobRunnerException extends RuntimeException
{
    private static final long serialVersionUID = -3294636949636696437L;

    public JobRunnerException(String message, Exception e)
    {
        super(message, e);
    }

    public JobRunnerException(String message, Throwable e)
    {
        super(message, e);
    }

    public JobRunnerException(String message)
    {
        super(message);
    }

    public JobRunnerException(Exception e)
    {
        super(e);
    }
}