package com.enioka.jqm.shared.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import com.enioka.jqm.shared.exceptions.JqmMissingPluginException;

public class ServiceLoaderHelper
{
    private static Map<String, Object> cache = new HashMap<>();

    /**
     * Get all plugins of the given generic type. Always returns fresh instances.
     *
     * @param <T>
     * @param serviceLoader
     *            - instead of using directly the type, we ask the calling module to use ServiceLoader itself as this is how JPMS works
     *            (otherwose this lirbary should "use" all service types...)
     * @return
     */
    public static <T> List<T> getServices(ServiceLoader<T> serviceLoader)
    {
        return serviceLoader.stream().map(serviceFactoryProvider -> {
            return serviceFactoryProvider.get();
        }).collect(Collectors.toList());
    }

    public static <T> T getService(ServiceLoader<T> serviceLoader)
    {
        return getService(serviceLoader, false);
    }

    public static <T> T getService(ServiceLoader<T> serviceLoader, boolean useCache)
    {
        var serviceLoaderDescription = serviceLoader.toString();
        if (useCache && cache.get(serviceLoaderDescription) != null)
        {
            return (T) cache.get(serviceLoaderDescription);
        }

        if (serviceLoader.stream().count() == 0)
        {
            throw new JqmMissingPluginException(serviceLoaderDescription);
        }
        else if (serviceLoader.stream().count() > 1)
        {
            throw new JqmMissingPluginException(serviceLoaderDescription + " - multiple implementations found");
        }

        var res = serviceLoader.findFirst();
        if (!res.isPresent())
        {
            throw new JqmMissingPluginException(serviceLoaderDescription);
        }
        if (useCache)
        {
            cache.put(serviceLoaderDescription, res.get());
        }
        return res.get();
    }
}
