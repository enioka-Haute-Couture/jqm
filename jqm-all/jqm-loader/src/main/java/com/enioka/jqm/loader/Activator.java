package com.enioka.jqm.loader;

import com.enioka.jqm.jdbc.DbAdapter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.annotation.bundle.Header;

@Header(name = org.osgi.framework.Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator implements BundleActivator
{
    public Loader<DbAdapter> loader;

    public void start(BundleContext context) throws Exception
    {
        loader = new Loader<DbAdapter>(context, DbAdapter.class, null);
        loader.start();
        DbAdapter adapter = loader.getService();

        if (adapter != null)
        {
            System.out.println("Found");
        }
    }

    public void stop(BundleContext context) throws Exception
    {
    }
}
