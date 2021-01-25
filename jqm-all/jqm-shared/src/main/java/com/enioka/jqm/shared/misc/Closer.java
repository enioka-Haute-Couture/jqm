package com.enioka.jqm.shared.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for handling closing "opened" instances which require cleaning.
 */
public class Closer
{
    private static Logger jqmlogger = LoggerFactory.getLogger(Closer.class);

    /**
     * Close utility method. No exception if it does not work or ig the parameter is null.
     *
     * @param ps
     *            closeable object to close.
     */
    public static void closeQuietly(AutoCloseable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (Exception e)
            {
                jqmlogger.warn("Could not close a closeable - possible leak", e);
            }
        }
    }

}
