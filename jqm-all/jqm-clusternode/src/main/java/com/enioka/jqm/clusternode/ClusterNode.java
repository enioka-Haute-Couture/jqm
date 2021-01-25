package com.enioka.jqm.clusternode;

import com.enioka.jqm.engine.api.exceptions.JqmInitError;
import com.enioka.jqm.engine.api.lifecycle.JqmEngineOperations;
import com.enioka.jqm.shared.exceptions.JqmRuntimeException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            BundleContext bundleContext = FrameworkUtil.getBundle(ClusterNode.class).getBundleContext();
            if (bundleContext == null)
            {
                throw new JqmInitError("Not in an OSGi context, cannot start a cluster node");
            }
            ServiceReference<JqmEngineOperations> jqmEngineSR = bundleContext.getServiceReference(JqmEngineOperations.class);
            if (jqmEngineSR == null)
            {
                throw new JqmInitError("No jqm engine service instance available - check jqm-engine bundle is started");
            }
            this.jqmEngine = bundleContext.getServiceObjects(jqmEngineSR).getService();

            jqmEngine.start(nodeName, new EngineCallback());
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
