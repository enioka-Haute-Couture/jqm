package com.enioka.jqm.jdbc;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.ServiceRegistration;

@Header(name = org.osgi.framework.Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator implements BundleActivator
{
    private ServiceRegistration<?> registration;

    public void start(BundleContext context) throws Exception
    {
        registration = context.registerService(DbManager.class, new DbManager(), null);
    }

    public void stop(BundleContext context) throws Exception
    {
        registration.unregister();
    }
}
