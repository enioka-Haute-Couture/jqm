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

import com.enioka.jqm.cli.bootstrap.CommandLine;

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
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

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
                "com.enioka.jqm.cli.bootstrap;version=1.0.0,org.slf4j;version=1.999.0,org.slf4j.spi;version=1.999.0,org.slf4j.helpers;version=1.999.0,ch.qos.logback.classic;version=1.999.0,ch.qos.logback.classic.spi;version=1.999.0,ch.qos.logback.core;version=1.999.0,ch.qos.logback.core.rolling;version=1.999.0,javax.security.auth.x500;version=1.999.0,org.apache.commons.logging");
        osgiConfig.put("org.osgi.framework.startlevel.beginning", "1"); // 0 is framework, 1 is framework extension, 5 is normal.
        osgiConfig.put("felix.auto.deploy.action", ""); // Disable auto deploy
        osgiConfig.put("org.apache.cxf.osgi.http.transport.disable", "true"); // remove /cxf
        osgiConfig.put("org.apache.felix.http.enable", "false");
        osgiConfig.put("org.apache.felix.https.enable", "false");
        osgiConfig.put("org.apache.aries.spifly.auto.consumers", "jakarta.*");
        osgiConfig.put("org.apache.aries.spifly.auto.providers", "com.sun.*");
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
            framework.start();
            jqmlogger.debug("Framework started");
        }
        catch (BundleException e)
        {
            jqmlogger.error("Could not initialize OSGi framework", e);
            System.exit(999);
        }

        // we prefer to use standard OSGi classes rather than Felix ones (which would be cleaner)
        BundleContext ctx = framework.getBundleContext();

        // load our bundles
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

        jqmlogger.debug("Installing standard bundles...");
        for (String file : new File(libPath).list())
        {
            String path = new File(libPath, file).toURI().toString();
            if (loadedBundles.add(file))
            {
                tryInstallBundle(ctx, path);
            }
        }

        // Start the bundles
        Semaphore waitForLevelChange = new Semaphore(0);
        framework.adapt(FrameworkStartLevel.class).setStartLevel(5, new FrameworkListener()
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
        }
        catch (InterruptedException e)
        {
            jqmlogger.warn("Weird interruption while starting the framework");
        }
        for (Bundle bundle : ctx.getBundles())
        {
            tryStartBundle(bundle);
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

    private static Bundle tryInstallBundle(BundleContext ctx, String path)
    {
        return tryInstallBundle(ctx, path, 5);
    }

    private static Bundle tryInstallBundle(BundleContext ctx, String path, int startLevel)
    {
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
            jqmlogger.debug("Testing whether {} is an OSGi bundle", path);
            jarFile = new JarFile(testPath);
            Manifest m = jarFile.getManifest();

            if (m == null || m.getMainAttributes() == null)
            {
                return null;
            }

            isBundle = m.getMainAttributes().getValue("Bundle-SymbolicName") != null;
            newJarVersion = m.getMainAttributes().getValue("Bundle-Version");
            jqmlogger.debug("Answer is {}", isBundle);
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
            return;
        }
        if (bundle.getState() == Bundle.ACTIVE)
        {
            // Already started.
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

        jqmlogger.debug("Bundle {} in version {} is in state {}", bundle.getSymbolicName(), bundle.getVersion(), bundle.getState());
        if (bundle.getRegisteredServices() != null)
        {
            for (ServiceReference<?> sr : bundle.getRegisteredServices())
            {
                jqmlogger.debug("\t\t " + sr.getClass().getCanonicalName());
            }
            for (BundleCapability wire : bundle.adapt(BundleWiring.class).getCapabilities(null))
            {
                jqmlogger.debug(wire.getNamespace());
            }
        }
    }
}
