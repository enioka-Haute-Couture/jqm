package com.enioka.jqm.loader;

import java.util.ArrayList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class Loader<TYPE> implements ServiceListener
{
    public BundleContext context;
    public String filter;
    public Class<TYPE> type;
    public ArrayList<ServiceReference<?>> references;

    public Loader(BundleContext context, Class<TYPE> type, String filter) throws Exception
    {
        this.context = context;
        this.type = type;
        this.filter = filter;
        references = new ArrayList<ServiceReference<?>>();
    }

    public void start()
    {
        try
        {
            ServiceReference<?>[] tmp = context.getAllServiceReferences(type.getName(), filter);

            if (tmp == null)
                return;

            for (ServiceReference<?> ref : tmp)
            {
                references.add(ref);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        for (ServiceReference<?> ref : references)
        {
            context.ungetService(ref);
        }
    }

    public TYPE getService()
    {
        if (references.isEmpty())
            return null;

        ServiceReference<?> sr = references.get(0);
        TYPE res = (TYPE) context.getService(sr);

        return res;
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
