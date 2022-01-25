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

    @Configuration
    public Option[] config()
    {
        Option[] res = new Option[] {
                // LOG
                mavenBundle("org.osgi", "org.osgi.service.log").versionAsInProject().startLevel(1), //
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject().startLevel(1),
                mavenBundle("org.slf4j", "jul-to-slf4j").versionAsInProject().startLevel(1),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject().startLevel(1),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject().startLevel(1),
                mavenBundle("commons-logging", "commons-logging").versionAsInProject().startLevel(1),
                mavenBundle("org.apache.felix", "org.apache.felix.logback").versionAsInProject().startLevel(1),
                systemProperty("org.eclipse.jetty.util.log.class").value("org.eclipse.jetty.util.log.Slf4jLog"),
                systemProperty("eclipse.log.enabled").value("false"), //
                systemProperty("hsqldb.reconfig_logging").value("false"), //
                systemProperty("org.apache.felix.http.log.jul").value("jul"), //
                systemProperty("logback.configurationFile")
                        .value("file:" + Paths.get("./src/test/resources/logback-test.xml").toAbsolutePath().normalize().toString()),

                // SPI-Fly is needed in order to load java.util.ServiceLoader services (here needed for JAXB implementations)
                // Note that we cannot use the "one bundle" framework extension in PAX-EXAM, hence the many bundles here.
                systemProperty("org.apache.aries.spifly.auto.consumers").value("jakarta.*"),
                systemProperty("org.apache.aries.spifly.auto.providers").value("com.sun.*"),
                mavenBundle("org.apache.aries.spifly", "org.apache.aries.spifly.dynamic.bundle", "1.3.3").startLevel(1),
                mavenBundle("org.ow2.asm", "asm", "9.1").startLevel(1), //
                mavenBundle("org.ow2.asm", "asm-commons", "9.1").startLevel(1), //
                mavenBundle("org.ow2.asm", "asm-util", "9.1").startLevel(1), //
                mavenBundle("org.ow2.asm", "asm-tree", "9.1").startLevel(1), //
                mavenBundle("org.ow2.asm", "asm-analysis", "9.1").startLevel(1), //
                mavenBundle("org.apache.aries", "org.apache.aries.util", "1.1.3").startLevel(1),

                // OSGi DECLARATIVE SERVICES
                mavenBundle("org.osgi", "org.osgi.service.cm").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.scr").versionAsInProject(),
                mavenBundle("org.osgi", "org.osgi.util.promise", "1.1.1"), //
                mavenBundle("org.osgi", "org.osgi.util.function", "1.1.0"),

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
                mavenBundle("org.apache.httpcomponents", "httpclient-osgi").versionAsInProject(),
                mavenBundle("org.apache.httpcomponents", "httpcore-osgi").versionAsInProject(),

                // Cron
                mavenBundle("com.enioka.jqm", "jqm-osgi-repackaging-cron4j").versionAsInProject(),

                // CLI
                mavenBundle("com.enioka.jqm", "jqm-osgi-repackaging-jcommander").versionAsInProject(),

                // XML & binding through annotations APIs
                mavenBundle("com.enioka.jqm", "jqm-osgi-repackaging-jdom").versionAsInProject(), //
                // mavenBundle("jakarta.activation", "jakarta.activation-api").versionAsInProject(), depends on Java version
                mavenBundle("jakarta.xml.bind", "jakarta.xml.bind-api").versionAsInProject(), // JAXB

                // Mail session lib
                mavenBundle("com.sun.mail", "javax.mail").versionAsInProject(),

                // Needed on Java8 to kill processes properly (inside shell runner)
                mavenBundle("com.enioka.jqm", "jqm-osgi-repackaging-winp").versionAsInProject(),

                // Shiro is needed by test helpers & client lib for password generation
                mavenBundle("org.apache.shiro", "shiro-core").versionAsInProject(), //

                // Needed for certificate init on main service startup.
                mavenBundle("org.bouncycastle", "bcpkix-jdk15on").versionAsInProject(),
                mavenBundle("org.bouncycastle", "bcprov-jdk15on").versionAsInProject(),
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

        if (getJavaVersion() > 1.8)
        {
            res = java.util.Arrays.copyOf(res, res.length + 1);
            res[res.length - 1] = mavenBundle("jakarta.activation", "jakarta.activation-api").versionAsInProject();
        }

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

                // OSGi JAX-RS whiteboard (based on CXF, with full useless SOAP implementation) + function + promise.
                mavenBundle("org.osgi", "org.osgi.service.jaxrs").versionAsInProject(),
                mavenBundle("org.apache.geronimo.specs", "geronimo-jaxrs_2.1_spec").versionAsInProject(),
                mavenBundle("org.apache.aries.jax.rs", "org.apache.aries.jax.rs.whiteboard").versionAsInProject(),
                mavenBundle("jakarta.xml.ws", "jakarta.xml.ws-api").versionAsInProject(),
                mavenBundle("jakarta.xml.soap", "jakarta.xml.soap-api").versionAsInProject(),
                mavenBundle("jakarta.annotation", "jakarta.annotation-api").versionAsInProject(),

                systemProperty("org.apache.felix.http.enable").value("false"),
                systemProperty("org.apache.felix.https.enable").value("false"), //
                systemProperty("org.osgi.service.http.port").value("-1"),
                systemProperty("org.apache.aries.jax.rs.whiteboard.default.enabled").value("false"),

                // CXF
                mavenBundle("org.codehaus.woodstox", "stax2-api").versionAsInProject(), //
                mavenBundle("com.fasterxml.woodstox", "woodstox-core").versionAsInProject(), //
                mavenBundle("org.apache.aries.component-dsl", "org.apache.aries.component-dsl.component-dsl").versionAsInProject(),

                mavenBundle("org.apache.cxf", "cxf-rt-transports-http", "3.4.3"), //
                mavenBundle("org.apache.cxf", "cxf-core", "3.4.3").startLevel(2), // sadly level is very important...
                mavenBundle("org.apache.cxf", "cxf-rt-frontend-jaxrs", "3.4.3"), //
                mavenBundle("org.apache.cxf", "cxf-rt-rs-client", "3.4.3"), //
                mavenBundle("org.apache.cxf", "cxf-rt-rs-security-cors", "3.4.3"), //
                mavenBundle("org.apache.cxf", "cxf-rt-rs-sse", "3.4.3"), //
                mavenBundle("org.apache.cxf", "cxf-rt-security", "3.4.3"), //
                mavenBundle("org.apache.ws.xmlschema", "xmlschema-core", "2.2.5"), //

                systemProperty("org.apache.cxf.osgi.http.transport.disable").value("true"), // no useless /cxf registration

                // Web security
                // mavenBundle("org.apache.shiro", "shiro-core", "1.7.1"), // core is already present above.
                mavenBundle("org.apache.shiro", "shiro-web", "1.7.1"), //
                mavenBundle("org.owasp.encoder", "encoder").versionAsInProject(),

                // Our web app project
                mavenBundle("com.enioka.jqm", "jqm-ws").versionAsInProject(), // Used to be a war.

                // JAXB implementation
                mavenBundle("com.sun.xml.bind", "jaxb-osgi").versionAsInProject(),

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
        serviceWaiter.waitForService("[javax.servlet.Servlet]"); // HTTP whiteboard
        serviceWaiter.waitForService("[javax.servlet.Servlet]"); // JAX-RS whiteboard
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
