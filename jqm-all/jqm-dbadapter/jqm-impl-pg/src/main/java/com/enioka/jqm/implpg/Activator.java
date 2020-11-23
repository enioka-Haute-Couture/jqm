package com.enioka.jqm.implpg;

import com.enioka.jqm.jdbc.DbAdapter;

import java.util.Dictionary;
import java.util.Hashtable;

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
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("Plugin-Type", "DbAdapter");
        properties.put("Adapter-Type", "postgresql");
        registration = context.registerService(DbAdapter.class, new DbImplPg(), properties);
    }

    public void stop(BundleContext context) throws Exception
    {
        registration.unregister();
    }
}
