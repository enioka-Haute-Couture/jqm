package com.enioka.jqm.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.enioka.jqm.cli.bootstrap.CommandLine;

class OsgiRuntime
{
    private static Logger jqmlogger = LoggerFactory.getLogger(OsgiRuntime.class);

    static CommandLine newFramework()
    {
        // Logging stuff
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        // Go for framework
        jqmlogger.info("Initializing OSGi framework instance");
        Set<String> loadedBundles = new HashSet<>();

        // System properties
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("hsqldb.reconfig_logging", "false");
        System.setProperty("org.apache.felix.http.log.jul", "jul");

        File currentJar;
        try
        {
            currentJar = new File(OsgiRuntime.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

        String rootDir = System.getProperty("com.enioka.jqm.service.osgi.rootdir");
        if (rootDir == null)
        {
            rootDir = currentJar.getParent();
        }
        jqmlogger.debug("Using {} as root OSGi deployment directory", rootDir);

        String libPath = new File(rootDir, "bundle").getAbsolutePath();
        String levelOneLibPath = new File(libPath, "level1").getAbsolutePath();
        String tmpPath = new File(rootDir, "tmp/osgicache").getAbsolutePath();

        // OSGi properties
        Map<String, String> osgiConfig = new HashMap<>();
        osgiConfig.put(Constants.FRAMEWORK_STORAGE, tmpPath);
        osgiConfig.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "com.enioka.jqm.cli.bootstrap;version=1.0.0,org.slf4j;version=2.999.0,org.slf4j.spi;version=2.999.0,org.slf4j.helpers;version=2.999.0,org.slf4j;version=1.999.0,org.slf4j.spi;version=1.999.0,org.slf4j.helpers;version=1.999.0,ch.qos.logback.classic;version=1.999.0,ch.qos.logback.classic.spi;version=1.999.0,ch.qos.logback.core;version=1.999.0,ch.qos.logback.core.rolling;version=1.999.0,javax.security.auth.x500;version=1.999.0,org.apache.commons.logging");
        osgiConfig.put("org.osgi.framework.startlevel.beginning", "0"); // 0 is framework, 1 is framework extension, 5 is normal.
        osgiConfig.put("felix.auto.deploy.action", ""); // Disable auto deploy
        osgiConfig.put("org.apache.felix.http.enable", "false");
        osgiConfig.put("org.apache.felix.https.enable", "false");
        osgiConfig.put("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.Slf4jLog");
        osgiConfig.put("eclipse.log.enabled", "false");

        // TODO: shutdown hook here.

        FrameworkFactory factory = null;
        try
        {
            factory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        }
        catch (NoSuchElementException e)
        {
            System.err.println("No available implementation of FrameworkFactory");
            e.printStackTrace();
            System.exit(999);
        }

        Framework framework = factory.newFramework(osgiConfig);
        try
        {
            jqmlogger.debug("Initializing OSGi framework.");
            framework.init();
            jqmlogger.debug("Framework initialized");
        }
        catch (BundleException e)
        {
            jqmlogger.error("Could not initialize OSGi framework", e);
            System.exit(999);
        }

        // we prefer to use standard OSGi classes rather than Felix ones (which would be cleaner)
        BundleContext ctx = framework.getBundleContext();

        // load priority bundles - the ones defining framework extensions like spi-fly and url wrapper.
        jqmlogger.debug("Installing level 1 bundles...");
        List<Bundle> levelBundles = new ArrayList<>();
        for (String file : new File(levelOneLibPath).list())
        {
            String path = new File(levelOneLibPath, file).toURI().toString();
            if (loadedBundles.add(file))
            {
                Bundle b = tryInstallBundle(ctx, path, 1);
                if (b != null)
                {
                    levelBundles.add(b);
                }
            }
        }
        jqmlogger.debug("Starting level 1 bundles...");
        for (Bundle bundle : levelBundles)
        {
            tryStartBundle(bundle);
        }
        levelBundles.clear();

        // Now that extensions are installed, start the framework.
        try
        {
            framework.start();
        }
        catch (BundleException e)
        {
            jqmlogger.error("Could not initialize OSGi framework", e);
            System.exit(999);
        }
        setStartLevel(framework, 2);

        // Install all the standard bundles
        jqmlogger.debug("Installing standard bundles...");
        for (String file : new File(libPath).list())
        {
            String path = new File(libPath, file).toURI().toString();
            if (loadedBundles.add(file))
            {
                var b = tryInstallBundle(ctx, path, 5);
                if (b != null)
                {
                    levelBundles.add(b);
                }
            }
        }

        // Start the standard bundles
        jqmlogger.debug("Starting standard bundles...");
        for (Bundle bundle : levelBundles)
        {
            tryStartBundle(bundle);
        }

        setStartLevel(framework, 5);

        if (jqmlogger.isDebugEnabled())
        {
            dumpContext(ctx);
        }

        // get our main entry point service
        ServiceReference<CommandLine> sr = ctx.getServiceReference(CommandLine.class);
        if (sr == null)
        {
            jqmlogger.error("Could not initialize OSGi framework - missing CLI implementation");
            System.exit(998);
        }

        CommandLine cli = ctx.getService(sr);
        if (cli == null)
        {
            jqmlogger.error("Could not initialize OSGi framework - CLI service reference was disabled during startup");
            System.exit(997);
        }

        return cli;
    }

    private static Bundle tryInstallBundle(BundleContext ctx, String path, int startLevel)
    {
        // SUPER DUPER HACK: SPI-Fly client weaving is not working on Liquibase when org.osgi.util.tracker-1.5.4 is started before it.
        // This tracker is very useful and needed by the Eclipse whiteboard, so we cannot remove it.
        // Why oh why...
        if (path.contains("liquibase"))
        {
            startLevel = 1;
        }
        if (path.contains("tracker"))
        {
            startLevel = 5;
        }
        // end of hack
        ////////////////////////////////////

        jqmlogger.trace("tryInstallBundle {}", path);
        File testPath = new File(path.replace("file:", ""));
        if (!path.endsWith("jar") || !testPath.exists() || testPath.isDirectory())
        {
            jqmlogger.debug("Trying to install a non file {} - {} - {}", path, new File(path).exists(), new File(path).isDirectory());
            return null;
        }

        JarFile jarFile = null;
        String newJarVersion;
        boolean isBundle = true;
        try
        {
            jqmlogger.trace("Testing whether {} is an OSGi bundle", path);
            jarFile = new JarFile(testPath);
            Manifest m = jarFile.getManifest();

            if (m == null || m.getMainAttributes() == null)
            {
                return null;
            }

            var name = m.getMainAttributes().getValue("Bundle-SymbolicName");
            isBundle = name != null;
            newJarVersion = m.getMainAttributes().getValue("Bundle-Version");
            jqmlogger.trace("Answer is {} - bundle name is {}", isBundle, name);
        }
        catch (IOException e)
        {
            jqmlogger.warn("Could not read jar manifest", e);
            return null;
        }
        finally
        {
            if (jarFile != null)
            {
                try
                {
                    jarFile.close();
                }
                catch (IOException e)
                {
                    // nothing to do.
                }
            }
        }

        try
        {
            jqmlogger.debug("\tInstalling bundle {} - it is a {}", path, (isBundle ? "normal bundle" : "simple jar"));

            Bundle b = ctx.getBundle(path);
            if (isBundle && b == null)
            {
                jqmlogger.trace("\tBundle {} is newly installed inside cache", path);
                b = ctx.installBundle(path);
            }
            else if (isBundle && b != null)
            {
                if (!b.getVersion().equals(Version.parseVersion(newJarVersion)))
                {
                    jqmlogger.trace("\tBundle {} is updated inside cache", path);
                    b.update(null);
                }
                else
                {
                    jqmlogger.trace("\tBundle {} is already up-to-date inside cache", path);
                }
            }
            else
            {
                jqmlogger.debug("This bundle {} is not an OSGi bundle and will be wrapped before installing", path);
                b = ctx.installBundle("wrap:" + path);
            }
            b.adapt(BundleStartLevel.class).setStartLevel(startLevel);

            jqmlogger.debug("\t\t** Bundle installed as {}:{} with start level {}", b.getSymbolicName(), b.getVersion(), startLevel);
            return b;
        }
        catch (BundleException e)
        {
            jqmlogger.error("Cound not install bundle " + path, e);
            System.exit(996);
            return null;
        }
    }

    private static void tryStartBundle(Bundle bundle)
    {
        if ((bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0)
        {
            // Do not start fragments
            jqmlogger.debug("Fragment bundle {} does not need to be started", bundle.getSymbolicName());
            dumpBundleDebugInfo(bundle);
            return;
        }
        if (bundle.getState() == Bundle.ACTIVE)
        {
            // Already started.
            jqmlogger.debug("Bundle {} is already started", bundle.getSymbolicName());
            dumpBundleDebugInfo(bundle);
            return;
        }
        if (bundle == null || bundle.getSymbolicName() == null)
        {
            jqmlogger.error("Bundle {} has no name and cannot be started", bundle);
            return;
        }

        try
        {
            jqmlogger.debug("Starting bundle {}", bundle.getSymbolicName());
            bundle.start();
        }
        catch (BundleException e)
        {
            jqmlogger.error("Could not start bundle " + bundle.getSymbolicName(), e);
        }

        dumpBundleDebugInfo(bundle);
    }

    /**
     * Debug logging, helpful when looking for an OSGi lib conflict
     *
     * @param bundle
     *            bundle to analyze
     */
    private static void dumpBundleDebugInfo(Bundle bundle)
    {
        jqmlogger.debug("Bundle {} in version {} is in state {}", bundle.getSymbolicName(), bundle.getVersion(), bundle.getState());
        if (bundle.getRegisteredServices() != null && bundle.getRegisteredServices().length > 0)
        {
            jqmlogger.debug("\tServices provided:");
            for (ServiceReference<?> sr : bundle.getRegisteredServices())
            {
                jqmlogger.debug("\t\t{} - {} {}", sr.getProperty(org.osgi.framework.Constants.OBJECTCLASS),
                        sr.getProperty(org.osgi.service.component.ComponentConstants.COMPONENT_NAME),
                        sr.getProperty("serviceloader.mediator") != null ? "mediator service" : "");
            }
        }
        if (bundle.adapt(BundleWiring.class) != null)
        {
            if (bundle.adapt(BundleWiring.class).getCapabilities(BundleRevision.PACKAGE_NAMESPACE).size() > 0)
            {
                jqmlogger.debug("\tPackages exposed:");
                for (var wire : bundle.adapt(BundleWiring.class).getCapabilities(BundleRevision.PACKAGE_NAMESPACE))
                {
                    jqmlogger.debug("\t\t{}", wire.toString());
                }
            }
            if (bundle.adapt(BundleWiring.class).getRequiredResourceWires(BundleRevision.PACKAGE_NAMESPACE).size() > 0)
            {
                jqmlogger.debug("\tPackages consumed:");
                for (var wire : bundle.adapt(BundleWiring.class).getRequiredResourceWires(BundleRevision.PACKAGE_NAMESPACE))
                {
                    jqmlogger.debug("\t\t{}", wire.toString());
                }
            }
        }
    }

    private static void dumpContext(BundleContext ctx)
    {
        for (var bundle : ctx.getBundles())
        {
            dumpBundleDebugInfo(bundle);
        }
    }

    private static void setStartLevel(Framework framework, int level)
    {
        Semaphore waitForLevelChange = new Semaphore(0);
        framework.adapt(FrameworkStartLevel.class).setStartLevel(level, new FrameworkListener()
        {
            @Override
            public void frameworkEvent(FrameworkEvent event)
            {
                if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED)
                {
                    waitForLevelChange.release();
                }
            }
        });
        try
        {
            waitForLevelChange.acquire();
            jqmlogger.debug("Start level was correctly set to {}", level);
        }
        catch (InterruptedException e)
        {
            jqmlogger.warn("Weird interruption while starting the framework");
        }
    }
}
