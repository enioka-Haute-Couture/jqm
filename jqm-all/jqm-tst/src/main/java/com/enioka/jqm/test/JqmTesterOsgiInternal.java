package com.enioka.jqm.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JqmTesterOsgiInternal
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JqmTesterOsgiInternal.class);

    private static Framework framework;
    private HashSet<String> bundlesToLoad = new HashSet<>(Arrays.asList("jakarta.xml.ws:jakarta.xml.ws-api:2.3.3",
            "org.apache.felix:org.apache.felix.configadmin:1.9.20", "org.apache.felix:org.apache.felix.scr:2.1.26",
            "org.osgi:org.osgi.service.cm:1.6.0", "org.osgi:org.osgi.util.promise:1.1.1", "org.osgi:org.osgi.util.function:1.1.0",
            "com.enioka.jqm:jqm-tst-osgi:" + Common.getMavenVersion()));

    protected Map<String, String> defaultOsgiFrameworkConfiguration()
    {
        // OSGi properties
        Map<String, String> osgiConfig = new HashMap<>();
        osgiConfig.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "com.enioka.jqm.tester.api;version=3.999.0,com.enioka.jqm.client.api;version=3.999.0,org.slf4j;version=1.7.32,org.slf4j.spi;version=1.7.32,org.slf4j.helpers;version=1.7.32");
        osgiConfig.put(Constants.FRAMEWORK_STORAGE, "./target/osgicachetester");
        osgiConfig.put("org.osgi.framework.storage.clean", "onFirstInit");
        osgiConfig.put("org.osgi.framework.startlevel.beginning", "5"); // 0 is framework, 1 is framework extension, 5 is normal.
        osgiConfig.put("felix.auto.deploy.action", ""); // Disable auto deploy
        osgiConfig.put("eclipse.log.enabled", "false");

        return osgiConfig;
    }

    void start()
    {
        if (framework != null)
        {
            return;
        }

        // System properties
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("hsqldb.reconfig_logging", "false");
        System.setProperty("org.apache.felix.http.log.jul", "jul");
        System.setProperty("org.ops4j.pax.url.mvn.useFallbackRepositories", "false");

        // Framework init
        FrameworkFactory factory = null;
        try
        {
            factory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
        }
        catch (NoSuchElementException e)
        {
            System.err.println("No available implementation of FrameworkFactory");
            e.printStackTrace();
            return;
        }

        framework = factory.newFramework(defaultOsgiFrameworkConfiguration());
        try
        {
            framework.start();
            jqmlogger.debug("Framework started");
        }
        catch (BundleException e)
        {
            jqmlogger.error("Could not initialize OSGi framework", e);
            return;
        }

        // List all bundles to load
        HashSet<MavenDependency> allDeps = new HashSet<>();
        for (String dep : bundlesToLoad)
        {
            allDeps.addAll(MavenResolver.getArtifactDependencies(dep));
        }

        // Register them
        BundleContext ctx = framework.getBundleContext();
        for (MavenDependency dep : allDeps)
        {
            // No log - provided as a system class.
            if (dep.isOsgiBundle() && dep.getBundleName().equals("slf4j.api"))
            {
                continue;
            }

            try
            {
                jqmlogger.debug("\tInstalling bundle {} - it is a {}", dep.getFile(),
                        (dep.isOsgiBundle() ? "normal bundle " + dep.getBundleName() : "simple jar"));

                Bundle b = ctx.installBundle(dep.getPaxUrl());
                b.adapt(BundleStartLevel.class).setStartLevel(5);

                jqmlogger.debug("\t\t** Bundle installed as {}:{} with start level {}", b.getSymbolicName(), b.getVersion(), 5);
            }
            catch (BundleException e)
            {
                jqmlogger.error("Cound not install bundle " + dep, e);
            }
        }

        // Start the bundles
        for (Bundle bundle : ctx.getBundles())
        {
            tryStartBundle(bundle);
        }

        dumpServices();
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
            /*
             * for (ServiceReference<?> sr : bundle.getRegisteredServices()) { jqmlogger.debug("\t\tService: {}",
             * sr.getProperty("component.name")); }
             */
            /*
             * for (BundleCapability wire : bundle.adapt(BundleWiring.class).getCapabilities(null)) { jqmlogger.debug("\t\tWire: {}",
             * wire.getNamespace()); }
             */
        }
    }

    private void dumpServices()
    {
        for (Bundle bundle : framework.getBundleContext().getBundles())
        {
            jqmlogger.debug("Bundle {} in version {} is in state {}", bundle.getSymbolicName(), bundle.getVersion(), bundle.getState());
            if (bundle.getRegisteredServices() != null)
            {
                for (ServiceReference<?> sr : bundle.getRegisteredServices())
                {
                    jqmlogger.debug("\t\tService: {}", sr.getProperty("component.name"));
                }
                /*
                 * for (BundleCapability wire : bundle.adapt(BundleWiring.class).getCapabilities(null)) { jqmlogger.debug("\t\tWire: {}",
                 * wire.getNamespace()); }
                 */
            }
        }
    }

    /**
     * Helper to get a service implementing a given interface. This interface must be part of the OSGi framework's system packages
     * (otherwise there would be two different versions of the interface and no service will be found). Throws exceptions if no service is
     * found.
     *
     * @param <T>
     *            inferred from <ode>clazz</code>
     * @param clazz
     *            the interface which must be implemented by the requested service
     * @return the first (according to OSGi rules) service implementing <code>clazz</code>. Cannot be null.
     */
    <T> T getSystemApi(Class<T> clazz)
    {
        ServiceReference<T> sr = framework.getBundleContext().getServiceReference(clazz);
        if (sr == null)
        {
            throw new RuntimeException(
                    "Could not initialize OSGi framework - missing implementation for system class " + clazz.getCanonicalName());
        }

        T result = framework.getBundleContext().getService(sr);
        if (result == null)
        {
            throw new RuntimeException("Could not initialize OSGi framework - expected system service reference " + clazz.getCanonicalName()
                    + " was disabled during startup");
        }

        return result;
    }
}
