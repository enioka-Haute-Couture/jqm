package com.enioka.jqm.api;

/**
 * The different status a {@link JobInstance} (and therefore an {@link History}, which is simply an archive created from a JobInstance) can
 * take.
 */
public enum State {
    /** The job requests was just created. The {@link JobInstance} is waiting in queue. **/
    SUBMITTED,
    /** An engine has taken the {@link JobInstance} and will soon run it. JobInstance cannot be modified any more. **/
    ATTRIBUTED,
    /**
     * The payload is running. Check {@link JobInstance#getMessages()} and {@link JobInstance#getProgress()} to know what is happening
     * inside the payload.
     **/
    RUNNING,
    /** The payload has ended in error (it has thrown an exception). **/
    CRASHED,
    /** The payload has ended correctly (no exception). **/
    ENDED,
    /** The payload is still running but has received a KILL order. It may or may not obey it. **/
    KILLED,
    /** The execution request is still waiting inside the queue, but will not advance anymore. **/
    HOLDED,
    /** Reserved for future use. **/
    CANCELLED
}
