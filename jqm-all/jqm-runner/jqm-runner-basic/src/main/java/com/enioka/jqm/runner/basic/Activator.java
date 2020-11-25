package com.enioka.jqm.runner.basic;

import com.enioka.jqm.api.JavaJobRunner;

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
        Dictionary<String, String> properties1 = new Hashtable<String, String>();
        properties1.put("Plugin-Type", "JavaJobRunner");
        properties1.put("Runner-Type", "main");
        registration = context.registerService(JavaJobRunner.class, new MainRunner(), properties1);

        Dictionary<String, String> properties2 = new Hashtable<String, String>();
        properties2.put("Plugin-Type", "JavaJobRunner");
        properties2.put("Runner-Type", "runnable");
        registration = context.registerService(JavaJobRunner.class, new RunnableRunner(), properties2);

        Dictionary<String, String> properties3 = new Hashtable<String, String>();
        properties3.put("Plugin-Type", "JavaJobRunner");
        properties3.put("Runner-Type", "legacy");
        registration = context.registerService(JavaJobRunner.class, new LegacyRunner(), properties3);
    }

    public void stop(BundleContext context) throws Exception
    {
        registration.unregister();
    }
}
