package com.enioka.jqm.loader;

import java.util.ArrayList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class Loader implements ServiceListener
{
    public BundleContext context;
    public String interfaceName;
    public String filter;
    public ArrayList<ServiceReference<?>> references;

    public Loader(BundleContext context, String interfaceName, String filter) throws Exception
    {
        this.context = context;
        this.interfaceName = interfaceName;
        this.filter = filter;
        references = new ArrayList<ServiceReference<?>>();
    }

    public void start() throws Exception
    {
        ServiceReference<?>[] tmp = context.getAllServiceReferences(interfaceName, filter);

        if (tmp == null)
            return;

        for (ServiceReference<?> ref : tmp)
        {
            references.add(ref);
        }
    }

    public void stop()
    {
        for (ServiceReference<?> ref : references)
        {
            context.ungetService(ref);
        }
    }

    public void useService()
    {
        while (references.isEmpty())
            continue;

        ServiceReference<?> sr = references.get(0);
        // Do Something
        context.ungetService(sr);
    }

    @Override
    public void serviceChanged(ServiceEvent event)
    {
        int type = event.getType();

        if (type == ServiceEvent.REGISTERED)
        {
            references.add(event.getServiceReference());
        }
        else if (type == ServiceEvent.UNREGISTERING)
        {
            references.remove(event.getServiceReference());
        }
    }
}
