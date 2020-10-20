package com.enioka.jqm.loader;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.annotation.bundle.Header;

@Header(name = org.osgi.framework.Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator implements BundleActivator
{
    public void start(BundleContext context) throws Exception
    {
    }

    public void stop(BundleContext context) throws Exception
    {
    }
}
