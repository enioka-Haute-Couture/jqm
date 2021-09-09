package com.enioka.jqm.configservices;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple helper for logging some information on installed bundles.
 */
public class OsgiBundleLogger
{
    private static Logger jqmlogger = LoggerFactory.getLogger(OsgiBundleLogger.class);

    public static void logAllBundles()
    {
        BundleContext ctx = FrameworkUtil.getBundle(OsgiBundleLogger.class).getBundleContext();
        Bundle[] bundles = ctx.getBundles();

        jqmlogger.info("The following OSGi bundles are available: ");
        for (Bundle bundle : bundles)
        {
            jqmlogger.info("\tBundle [{}] state [{}] version [{}]", bundle.getSymbolicName(), bundle.getState(), bundle.getVersion());
        }
    }
}
