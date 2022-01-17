package com.enioka.jqm.jdbc;

import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Compatibility helper: used to cleanup JDBC items on bundle unload. Useless outside OSGi.
 */
@Header(name = org.osgi.framework.Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class OsgiActivator implements BundleActivator
{

    @Override
    public void start(BundleContext context) throws Exception
    {
        // Nothing to do.
    }

    @Override
    public void stop(BundleContext context) throws Exception
    {
        DbManager.getDb().close();
    }
}
