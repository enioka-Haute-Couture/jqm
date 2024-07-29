package com.enioka.jqm.service;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class loader for the engine. It will load all jars in the plugins directory. It uses the class loader used to load this class as a
 * parent. Usually, that will be the system class loader.
 */
public class EngineClassLoader extends URLClassLoader
{
    private static Logger jqmlogger = LoggerFactory.getLogger(EngineClassLoader.class);

    public EngineClassLoader()
    {
        super(getPlugins(), EngineClassLoader.class.getClassLoader());
    }

    private static URL[] getPlugins()
    {
        File currentJar;
        try
        {
            currentJar = new File(EngineClassLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        }
        catch (URISyntaxException e)
        {
            throw new Error("Cound not fetch current jar location", e);
        }

        File root = currentJar.getParentFile();
        File plugins = new File(root, "plugins");

        if (plugins == null || !plugins.exists() || !plugins.isDirectory())
        {
            jqmlogger.debug("No plugin directory found. Using system class loader");
            return new URL[0];
        }

        jqmlogger.debug("Using {} as the plugin directory", plugins.getAbsolutePath());
        var res = new ArrayList<URL>();
        for (var f : plugins.listFiles())
        {
            if (f.isFile() && f.canRead() && f.getName().endsWith(".jar"))
            {
                try
                {
                    res.add(f.toURI().toURL());
                }
                catch (Exception e)
                {
                    throw new Error("Error when parsing the content of plugin directory. File will be ignored", e);
                }
            }
        }

        return res.toArray(new URL[0]);
    }
}
