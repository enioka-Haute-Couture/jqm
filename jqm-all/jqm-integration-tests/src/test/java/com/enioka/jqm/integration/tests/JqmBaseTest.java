/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.jqm.integration.tests;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.enioka.jqm.client.api.JobInstance;
import com.enioka.jqm.client.api.JqmClient;
import com.enioka.jqm.client.jdbc.api.JqmClientFactory;
import com.enioka.jqm.clusternode.EngineCallback;
import com.enioka.jqm.engine.api.lifecycle.JqmEngineOperations;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.test.helpers.DebugHsqlDbServer;
import com.enioka.jqm.test.helpers.ServiceWaiter;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JqmBaseTest
{
    public static Logger jqmlogger = LoggerFactory.getLogger(JqmBaseTest.class);

    protected static Db db;

    public Map<String, JqmEngineOperations> engines = new HashMap<>();
    public List<DbConn> cnxs = new ArrayList<>();
    protected DbConn cnx;

    @Inject
    protected static BundleContext bundleContext;

    @Inject
    protected DebugHsqlDbServer s;

    @Inject
    ConfigurationAdmin adminService;

    @Inject
    ServiceWaiter serviceWaiter;

    JqmClient jqmClient;

    @Rule
    public TestName testName = new TestName();

    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Configuration
    public Option[] config()
    {
        Option[] res = new Option[] {
                // LOG - we share logback/slf4j from the test classpath with the OSGi container through system packages.
                mavenBundle("org.osgi", "org.osgi.service.log").versionAsInProject().startLevel(1), //
                systemProperty("org.eclipse.jetty.util.log.class").value("org.eclipse.jetty.util.log.Slf4jLog"),
                systemProperty("eclipse.log.enabled").value("false"), //
                systemProperty("hsqldb.reconfig_logging").value("false"), //
                systemProperty("org.apache.felix.http.log.jul").value("jul"), //
                systemProperty("logback.configurationFile")
                        .value("file:" + Paths.get("./src/test/resources/logback-test.xml").toAbsolutePath().normalize().toString()),

                // We cheat - we export slf4j V2 as both V1 and V2, which works because the V2 client API is backward compatible with V1.
                systemPackages("org.slf4j;version=1.999.0", "org.slf4j;version=2.999.0", "org.slf4j.spi;version=2.999.0",
                        "org.slf4j.helpers;version=2.999.0", "ch.qos.logback.classic;version=1.999.0",
                        "ch.qos.logback.classic.spi;version=1.999.0", "ch.qos.logback.core;version=1.999.0",
                        "ch.qos.logback.core.rolling;version=1.999.0", "org.apache.commons.logging", "org.apache.commons.logging.impl",
                        "org.slf4j.bridge"),

                // SPI-Fly is needed in order to load java.util.ServiceLoader services (here needed for JAXB implementations)
                // Note that we cannot use the "one bundle" framework extension in PAX-EXAM, hence the many bundles here.
                systemProperty("org.apache.aries.spifly.auto.consumers").value("jakarta.*"),
                systemProperty("org.apache.aries.spifly.auto.providers").value("com.sun.*"),
                mavenBundle("org.apache.aries.spifly", "org.apache.aries.spifly.dynamic.bundle", "1.3.7").startLevel(1),
                mavenBundle("org.ow2.asm", "asm", "9.6").startLevel(1), //
                mavenBundle("org.ow2.asm", "asm-commons", "9.6").startLevel(1), //
                mavenBundle("org.ow2.asm", "asm-util", "9.6").startLevel(1), //
                mavenBundle("org.ow2.asm", "asm-tree", "9.6").startLevel(1), //
                mavenBundle("org.ow2.asm", "asm-analysis", "9.6").startLevel(1), //
                mavenBundle("org.apache.aries", "org.apache.aries.util", "1.1.3").startLevel(1),

                // OSGi DECLARATIVE SERVICES
                mavenBundle("org.osgi", "org.osgi.service.cm").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),
                mavenBundle("org.osgi", "org.osgi.util.promise", "1.3.0"), //
                mavenBundle("org.osgi", "org.osgi.util.function", "1.2.0"), //
                mavenBundle("org.osgi", "org.osgi.service.component", "1.5.1"), //

                // OSGi configuration service
                mavenBundle("org.apache.felix", "org.apache.felix.configadmin").versionAsInProject(),

                // Our test database (for most tests)
                mavenBundle("org.hsqldb", "hsqldb").versionAsInProject(),

                // Apache commons
                mavenBundle("commons-io", "commons-io").versionAsInProject(),
                mavenBundle("commons-lang", "commons-lang").versionAsInProject(),
                mavenBundle("commons-collections", "commons-collections").versionAsInProject(),
                mavenBundle("commons-beanutils", "commons-beanutils").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-lang3", "3.12.0"),

                // Cron
                mavenBundle("com.enioka.jqm", "jqm-osgi-repackaging-cron4j").versionAsInProject(),

                // CLI
                mavenBundle("org.jcommander", "jcommander").versionAsInProject(),

                // XML & binding through annotations APIs
                mavenBundle("com.enioka.jqm", "jqm-osgi-repackaging-jdom").versionAsInProject(), //
                mavenBundle("jakarta.activation", "jakarta.activation-api", "2.1.3"), //
                mavenBundle("jakarta.xml.bind", "jakarta.xml.bind-api").versionAsInProject(), // JAXB

                // Mail session lib
                mavenBundle("jakarta.mail", "jakarta.mail-api").versionAsInProject(),
                mavenBundle("org.eclipse.angus", "angus-mail").versionAsInProject(),

                // Shiro is needed by test helpers & client lib for password generation
                mavenBundle("org.apache.shiro", "shiro-crypto-core").versionAsInProject(), //
                mavenBundle("org.apache.shiro", "shiro-crypto-hash").versionAsInProject(), //
                mavenBundle("org.apache.shiro", "shiro-lang").versionAsInProject(), //

                // Needed for certificate init on main service startup.
                mavenBundle("org.bouncycastle", "bcpkix-jdk18on").versionAsInProject(),
                mavenBundle("org.bouncycastle", "bcprov-jdk18on").versionAsInProject(),
                mavenBundle("org.bouncycastle", "bcutil-jdk18on").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-pki").versionAsInProject(),

                // JQM tested libraries
                url("reference:file:../jqm-cli/target/classes/"), //
                url("reference:file:../jqm-cli-bootstrap/target/classes/"), //
                url("reference:file:../jqm-clusternode/target/classes/"), //
                url("reference:file:../jqm-api/target/classes/"), //
                url("reference:file:../jqm-loader/target/classes/"), //
                url("reference:file:../jqm-xml/target/classes/"), //
                url("reference:file:../jqm-admin/target/classes/"), //
                url("reference:file:../jqm-model/target/classes/"), //
                url("reference:file:../jqm-model-repository/target/classes/"), //
                url("reference:file:../jqm-jndi-context/target/classes/"), //
                url("reference:file:../jqm-engine/target/classes/"), //
                url("reference:file:../jqm-engine-api/target/classes/"),
                url("reference:file:../jqm-api-client/jqm-api-client-core/target/classes/"),
                url("reference:file:../jqm-api-client/jqm-api-client-jdbc/target/classes/"),
                url("reference:file:../jqm-dbadapter/jqm-dbadapter-hsql/target/classes/"),
                url("reference:file:../jqm-dbadapter/jqm-dbadapter-mysql/target/classes/"),
                url("reference:file:../jqm-dbadapter/jqm-dbadapter-mysql8/target/classes/"),
                url("reference:file:../jqm-dbadapter/jqm-dbadapter-oracle/target/classes/"),
                url("reference:file:../jqm-dbadapter/jqm-dbadapter-db2/target/classes/"),
                url("reference:file:../jqm-dbadapter/jqm-dbadapter-pg/target/classes/"),
                url("reference:file:../jqm-runner/jqm-runner-api/target/classes/"), //
                // url("reference:file:../jqm-runner/jqm-runner-java/target/classes/"), //
                url("reference:file:../jqm-runner/jqm-runner-spring/target/classes/"), //
                url("reference:file:../jqm-runner/jqm-runner-java-api/target/classes/"), //
                url("reference:file:../jqm-runner/jqm-runner-shell/target/classes/"), //
                url("reference:file:../jqm-runner/jqm-runner-shell-api/target/classes/"),
                url("reference:file:../jqm-shared/target/classes/"), //
                url("reference:file:../jqm-test-helpers/target/classes/"),

                mavenBundle("com.enioka.jqm", "jqm-runner-java").versionAsInProject(), // must stay as a jar cause shaded (for now)

                // Junit itself
                junitBundles(),

                // Maven config
                systemProperty("org.ops4j.pax.url.mvn.repositories").value("https://repo1.maven.org/maven2@id=central"),
                systemProperty("org.ops4j.pax.url.mvn.useFallbackRepositories").value("false"),

                systemProperty("com.enioka.jqm.alternateJqmRoot").value("./target/server"),

        };

        Option[] additionnal = moreOsgiconfig();
        int localOptionsCount = res.length;
        res = java.util.Arrays.copyOf(res, localOptionsCount + additionnal.length);
        for (int i = 0; i < additionnal.length; i++)
        {
            res[localOptionsCount + i] = additionnal[i];
        }

        return res;
    }

    /**
     * To be optionaly overloaded by tests.
     */
    protected Option[] moreOsgiconfig()
    {
        return options();
    }

    /**
     * Separated web configuration (as it is rather heavy only load it inside {@link #moreOsgiconfig()} if needed)
     */
    protected Option[] webConfig()
    {
        return options(
                // OSGi HTTP Whiteboard (based on Jetty, providing OSGi HTTP service, used by JAX-RS whiteboard)
                mavenBundle("org.apache.felix", "org.apache.felix.http.servlet-api").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.http.jetty").versionAsInProject(),

                // APIs implemented
                mavenBundle("jakarta.servlet", "jakarta.servlet-api").versionAsInProject(),
                mavenBundle("jakarta.ws.rs", "jakarta.ws.rs-api").versionAsInProject(),
                mavenBundle("jakarta.xml.ws", "jakarta.xml.ws-api").versionAsInProject(),
                mavenBundle("jakarta.xml.soap", "jakarta.xml.soap-api").versionAsInProject(),

                // APIs transitively used
                mavenBundle("jakarta.inject", "jakarta.inject-api").versionAsInProject(),
                mavenBundle("jakarta.xml.bind", "jakarta.xml.bind-api").versionAsInProject(),
                mavenBundle("jakarta.validation", "jakarta.validation-api").versionAsInProject(),

                // OSGi Jakarta-RS whiteboard (based on Jersey)
                mavenBundle("org.osgi", "org.osgi.service.jakartars").versionAsInProject(),
                mavenBundle("org.eclipse.osgi-technology.rest", "org.eclipse.osgitech.rest").versionAsInProject(),
                mavenBundle("org.eclipse.osgi-technology.rest", "org.eclipse.osgitech.rest.servlet.whiteboard").versionAsInProject(),
                mavenBundle("org.eclipse.osgi-technology.rest", "org.eclipse.osgitech.rest.sse").versionAsInProject().noStart(), // fragment

                systemProperty("org.apache.felix.http.enable").value("false"),
                systemProperty("org.apache.felix.https.enable").value("false"), //
                systemProperty("org.osgi.service.http.port").value("-1"),

                // Jersey
                mavenBundle("org.glassfish.jersey.core", "jersey-common").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.core", "jersey-server").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.core", "jersey-client").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.containers", "jersey-container-servlet-core").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.containers", "jersey-container-servlet").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.inject", "jersey-hk2").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.media", "jersey-media-jaxb").versionAsInProject(),
                mavenBundle("org.glassfish.jersey.media", "jersey-media-sse").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-api").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-utils").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "hk2-locator").versionAsInProject(),
                mavenBundle("org.glassfish.hk2", "osgi-resource-locator").versionAsInProject(),
                mavenBundle("org.glassfish.hk2.external", "aopalliance-repackaged").versionAsInProject(),
                mavenBundle("org.javassist", "javassist").versionAsInProject(),
                mavenBundle("jakarta.annotation", "jakarta.annotation-api", "2.1.1"), // additional version for HK2

                // Web security
                // mavenBundle("org.apache.shiro", "shiro-core").classifier("jakarta").versionAsInProject(), //
                // mavenBundle("org.apache.shiro", "shiro-web").classifier("jakarta").versionAsInProject(), //
                url("reference:file:../jqm-osgi-repackaging/jqm-osgi-repackaging-shiro/target/classes/"),
                mavenBundle("org.apache.shiro", "shiro-cache").versionAsInProject(), //
                mavenBundle("org.apache.shiro", "shiro-event").versionAsInProject(), //
                mavenBundle("org.apache.shiro", "shiro-crypto-cipher").versionAsInProject(), //
                mavenBundle("org.apache.shiro", "shiro-config-core").versionAsInProject(), //
                mavenBundle("org.apache.shiro", "shiro-config-ogdl").versionAsInProject(), //
                mavenBundle("org.owasp.encoder", "encoder").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-configuration2").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-text").versionAsInProject(),

                // JAXB implementation
                mavenBundle("com.sun.xml.bind", "jaxb-osgi").versionAsInProject(),

                // Our web app project
                url("reference:file:../jqm-ws/target/classes/"),

                // The JAX-RS/Jersey JQM client library (i.e. the tested library)
                url("reference:file:../jqm-api-client/jqm-api-client-jersey/target/classes/"));
    }

    @Before
    public void beforeEachTest() throws NamingException
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test " + testName.getMethodName());

        JqmClientFactory.resetClient(null);
        jqmClient = JqmClientFactory.getClient();

        if (db == null)
        {
            // In all cases load the datasource. (the helper itself will load the property file if any).
            db = DbManager.getDb();
        }

        cnx = getNewDbSession();
        TestHelpers.cleanup(cnx);
        TestHelpers.createTestData(cnx);
        cnx.commit();

        // Force JNDI directory loading
        InitialContext.doLookup("string/debug");
    }

    @After
    public void afterEachTest()
    {
        jqmlogger.debug("*** Cleaning after test " + testName.getMethodName());
        for (String k : engines.keySet())
        {
            JqmEngineOperations e = engines.get(k);
            e.stop();
        }
        engines.clear();
        for (DbConn cnx : cnxs)
        {
            cnx.close();
        }
        cnxs.clear();

        // Reset the caches - no side effect between tests?
        try
        {
            InitialContext.doLookup("internal://reset");
        }
        catch (NamingException e)
        {
            // jqmlogger.warn("Could not purge test JNDI context", e);
        }

        // Java 6 GC being rather inefficient, we must run it multiple times to correctly collect Jetty-created class loaders and avoid
        // permgen issues
        System.runFinalization();
        System.gc();
        System.runFinalization();
        System.gc();
        System.gc();
    }

    protected void AssumeWindows()
    {
        Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
    }

    protected void AssumeNotWindows()
    {
        Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));
    }

    protected void AssumeHsqldb()
    {
        Assume.assumeTrue(s.isHsqldb());
    }

    protected void assumeJavaVersionStrictlyGreaterThan(double version)
    {
        Assume.assumeTrue(getJavaVersion() > version);
    }

    protected void assumeJavaVersionStrictlyLowerThan(double version)
    {
        Assume.assumeTrue(getJavaVersion() < version);
    }

    protected double getJavaVersion()
    {
        String ver = System.getProperty("java.version");
        return Double.parseDouble(ver.substring(0, ver.indexOf('.') + 2));
    }

    protected boolean onWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    protected JqmEngineOperations addAndStartEngine()
    {
        return addAndStartEngine("localhost");
    }

    protected JqmEngineOperations addAndStartEngine(String nodeName)
    {
        beforeStartEngine(nodeName);

        ServiceReference<JqmEngineOperations> jqmEngineFactory = bundleContext.getServiceReference(JqmEngineOperations.class);
        JqmEngineOperations e = bundleContext.getServiceObjects(jqmEngineFactory).getService();

        e.start(nodeName, new EngineCallback());
        engines.put(nodeName, e);
        afterStartEngine(nodeName);
        return e;
    }

    protected void beforeStartEngine(String nodeName)
    {
        // For overrides.
    }

    protected void afterStartEngine(String nodeName)
    {
        // For overrides.
    }

    protected void stopAndRemoveEngine(String nodeName)
    {
        JqmEngineOperations e = engines.get(nodeName);
        e.stop();
        engines.remove(nodeName);
    }

    protected DbConn getNewDbSession()
    {
        DbConn cnx = db.getConn();
        cnxs.add(cnx);
        return cnx;
    }

    protected void sleep(int s)
    {
        sleepms(1000 * s);
    }

    protected static void sleepms(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            // not an issue in tests
        }
    }

    protected void waitForWsStart()
    {
        serviceWaiter.waitForService("[com.enioka.jqm.ws.api.ServiceSimple]");
        serviceWaiter.waitForService("[org.osgi.service.http.runtime.HttpServiceRuntime]"); // HTTP whiteboard
        serviceWaiter.waitForService("[org.osgi.service.http.runtime.HttpServiceRuntime]"); // second time for config modif after startup
        serviceWaiter.waitForService("[org.osgi.service.jakartars.runtime.JakartarsServiceRuntime]"); //
    }

    protected void simulateDbFailure()
    {
        if (db.getProduct().contains("hsql"))
        {
            jqmlogger.info("DB is going down");
            s.stop();
            jqmlogger.info("DB is now fully down");
            this.sleep(1);
            jqmlogger.info("Restarting DB");
            s.start();
        }
        else if (db.getProduct().contains("postgresql"))
        {
            try
            {
                // update pg_database set datallowconn = false where datname = 'jqm' // Cannot run, as we cannot reconnect afterward!
                cnx.runRawSelect("select pg_terminate_backend(pid) from pg_stat_activity where datname='jqm';");
            }
            catch (Exception e)
            {
                // Do nothing - the query is a suicide so it cannot work fully.
            }
            cnx.close();
            cnx = getNewDbSession();
        }
    }

    protected void displayAllHistoryTable()
    {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
        jqmlogger.debug("==========================================================================================");
        for (JobInstance h : jqmClient.newQuery().invoke())
        {
            jqmlogger.debug("JobInstance Id: " + h.getId() + " | " + h.getState() + " | JD: " + h.getApplicationName() + " | "
                    + h.getQueueName() + " | enqueue: " + format.format(h.getEnqueueDate().getTime()) + " | exec: "
                    + (h.getBeganRunningDate() != null ? format.format(h.getBeganRunningDate().getTime()) : null) + " | end: "
                    + (h.getEndDate() != null ? format.format(h.getEndDate().getTime()) : null));
        }
        jqmlogger.debug("==========================================================================================");
    }

    protected void displayAllQueueTable()
    {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
        jqmlogger.debug("==========================================================================================");
        for (JobInstance h : jqmClient.newQuery().setQueryHistoryInstances(false).setQueryLiveInstances(true).invoke())
        {
            jqmlogger.debug("JobInstance Id: " + h.getId() + " | " + h.getState() + " | JD: " + h.getApplicationName() + " | "
                    + h.getQueueName() + " | enqueue: " + format.format(h.getEnqueueDate().getTime()) + " | exec: "
                    + (h.getBeganRunningDate() != null ? format.format(h.getBeganRunningDate().getTime()) : null) + " | position: "
                    + h.getPosition());
        }
        jqmlogger.debug("==========================================================================================");
    }

    /**
     * This test simply tests pax exam loads.
     */
    @Test
    public void testContainerStarts()
    {
        Assert.assertTrue(true);
    }
}
