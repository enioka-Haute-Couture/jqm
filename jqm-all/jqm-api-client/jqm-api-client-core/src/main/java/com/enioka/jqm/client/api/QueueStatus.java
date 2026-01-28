package com.enioka.jqm.client.api;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * The different status a queue can take.
 */
@XmlRootElement
public enum QueueStatus {
    /** The queue is enabled on all the nodes which are configured to poll the queue with at least one thread **/
    RUNNING,
    /** The queue is paused on at least one node, but running on at least one other **/
    PARTIALLY_RUNNING,
    /** The queue is paused on all the nodes which are configured to poll the queue **/
    PAUSED;

    /**
     * Get the queue status from a string representation.
     * @param param the string representation
     * @return the queue status
     */
    public static QueueStatus fromString(String param)
    {
        String toUpper = param.toUpperCase();
        try
        {
            return valueOf(toUpper);
        }
        catch (Exception e)
        {
            throw new RuntimeException("invalid enum value");
        }
    }
}
