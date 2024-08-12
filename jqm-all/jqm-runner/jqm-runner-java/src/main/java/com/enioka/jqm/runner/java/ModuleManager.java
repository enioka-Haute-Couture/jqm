package com.enioka.jqm.runner.java;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URISyntaxException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.enioka.jqm.model.JobInstance;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModuleManager
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ModuleManager.class);

    // This class would be a lambda in Java8+
    private static class ClMapper implements Function<String, ClassLoader>
    {
        private PayloadClassLoader cl;

        public ClMapper(PayloadClassLoader cl)
        {
            this.cl = cl;
        }

        @Override
        public ClassLoader apply(String t)
        {
            return cl;
        }
    }

    public static ModuleLayer createModuleLayerIfNeeded(PayloadClassLoader cl, ModuleLayer parentModuleLayer, JobInstance ji)
    {
        // TODO : cache layer if needed.

        // Test if the target is a module (just for information sake)
        File jarFile = new File(FilenameUtils.concat(new File(ji.getNode().getRepo()).getAbsolutePath(), ji.getJD().getJarPath()));
        ModuleFinder finder = ModuleFinder.of(jarFile.toPath());
        if (finder.findAll().isEmpty())
        {
            jqmlogger.debug("Root {} is not a JPMS module, will be loaded as an automatic module", jarFile.getAbsolutePath());
        }
        else
        {
            String mainModuleName = finder.findAll().iterator().next().descriptor().name();
            jqmlogger.debug("Root file {} is a JPMS module, using JPMS module layer. Root module name is {}", jarFile.getAbsolutePath(),
                mainModuleName);
        }

        // Convert URL to path (no streams in Java 6...)
        Path[] paths = new Path[cl.getURLs().length];
        for (int i = 0; i < cl.getURLs().length; i++)
        {
            try
            {
                paths[i] = Path.of(cl.getURLs()[i].toURI());
            }
            catch (URISyntaxException e)
            {
                throw new JqmPayloadException("wrong JPMS configuration", e);
            }
        }
        if (cl.getParent() instanceof URLClassLoader)
        {
            jqmlogger.debug("JPMS module detected, adding parent classloader to module path");
            paths = new Path[cl.getURLs().length + ((URLClassLoader) cl.getParent()).getURLs().length];
            for (int i = 0; i < cl.getURLs().length; i++)
            {
                try
                {
                    paths[i] = Path.of(cl.getURLs()[i].toURI());
                }
                catch (URISyntaxException e)
                {
                    throw new JqmPayloadException("wrong JPMS configuration", e);
                }
            }
            for (int i = 0; i < ((URLClassLoader) cl.getParent()).getURLs().length; i++)
            {
                try
                {
                    paths[i + cl.getURLs().length] = Path.of(((URLClassLoader) cl.getParent()).getURLs()[i].toURI());
                }
                catch (URISyntaxException e)
                {
                    throw new JqmPayloadException("wrong JPMS configuration", e);
                }
            }
        }

        jqmlogger.debug("JPMS module path is {}", (Object) paths);

        // Add all files inside the JI path inside the new layer configuration as module roots.
        ModuleFinder moduleFinder = ModuleFinder.of(paths);
        Set<String> moduleNames = moduleFinder.findAll().stream().map(ModuleReference::descriptor).map(ModuleDescriptor::name)
            .collect(Collectors.toUnmodifiableSet());

        // TODO: before/after according to child/parent first
        Configuration cf = parentModuleLayer.configuration().resolveAndBind(moduleFinder, ModuleFinder.of(), moduleNames);
        ModuleLayer layer = parentModuleLayer.defineModules(cf, new ClMapper(cl));

        return layer;
    }

}
