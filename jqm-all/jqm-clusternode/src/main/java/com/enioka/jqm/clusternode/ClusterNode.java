package com.enioka.jqm.clusternode;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.engine.api.lifecycle.JqmEngineOperations;
import com.enioka.jqm.shared.exceptions.JqmRuntimeException;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;

public class ClusterNode
{
    protected static Logger jqmlogger = LoggerFactory.getLogger(ClusterNode.class);

    private String nodeName;
    private JqmEngineOperations jqmEngine;

    public int startAndWaitEngine(String nodeName)
    {
        this.nodeName = nodeName;

        try
        {
            // Retrieve engine service (always a new one)
            this.jqmEngine = ServiceLoaderHelper.getService(ServiceLoader.load(JqmEngineOperations.class), false);

            // Go. The callback will be called once the engine is up to allow the end if the init sequence.
            jqmEngine.start(this.nodeName, new EngineCallback());
            jqmEngine.join();
            return 0;
        }
        catch (JqmRuntimeException e)
        {
            jqmlogger.error("Error running engine", e);
            return 111;
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not launch the engine named " + nodeName
                    + ". This may be because no node with this name was declared (with command line option createnode).", e);
            throw new JqmRuntimeException("Could not start the engine", e);
        }
    }

    public void stop()
    {
        if (jqmEngine != null)
        {
            jqmEngine.stop();
        }
    }
}
