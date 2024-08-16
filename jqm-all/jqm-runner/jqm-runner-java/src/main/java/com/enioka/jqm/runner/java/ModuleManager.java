package com.enioka.jqm.runner.java;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.model.JobInstance;

public final class ModuleManager
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ModuleManager.class);

    public static ModuleLayer createModuleLayerIfNeeded(PayloadClassLoader cl, ModuleLayer parentModuleLayer, JobInstance ji)
    {
        // TODO : cache layer if needed.
        var loadedModuleNames = new HashSet<String>();

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

        // Module path determination. We consider all files inside the JI path to be module roots.
        // Therefore take all URLs in the class loader including parents (i.e. potentially including ext CL).
        var pathList = new ArrayList<Path>(cl.getURLs().length);
        ClassLoader currentCL = cl;
        while (currentCL instanceof URLClassLoader) // meaning stop at the system class loader
        {
            for (var url : cl.getURLs())
            {
                var path = getPathSafe(url);
                var mf = ModuleFinder.of(path);
                var moduleName = mf.findAll().stream().map(ModuleReference::descriptor).map(ModuleDescriptor::name).findFirst()
                        .orElse(null);
                if (moduleName == null || (!parentModuleLayer.findModule(moduleName).isPresent() && loadedModuleNames.add(moduleName)))
                {
                    pathList.add(path);
                }
                else
                {
                    jqmlogger.warn("Module {} is present multiple times in the module path, keeping parent version", moduleName);
                }
            }
            currentCL = cl.getParent();
        }

        var paths = pathList.toArray(new Path[pathList.size()]);
        jqmlogger.debug("JPMS module path is {}", pathList);

        // Create layer, using same class loader for all modules.
        var moduleFinder = ModuleFinder.of(paths);
        Configuration cf = parentModuleLayer.configuration().resolveAndBind(moduleFinder, ModuleFinder.of(), loadedModuleNames);
        ModuleLayer layer = parentModuleLayer.defineModules(cf, (String t) -> cl);

        return layer;
    }

    private static Path getPathSafe(URL url)
    {
        try
        {
            return Path.of(url.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new JqmPayloadException("wrong JPMS configuration", e);
        }
    }
}
