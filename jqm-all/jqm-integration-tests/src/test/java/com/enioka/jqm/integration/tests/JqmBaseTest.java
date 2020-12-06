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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.xml.stream.XMLStreamException;

import static org.ops4j.pax.exam.CoreOptions.*;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import com.enioka.jqm.api.client.core.JobInstance;
import com.enioka.jqm.api.client.core.JqmClientFactory;
import com.enioka.jqm.api.client.core.Query;
import com.enioka.jqm.engine.Helpers;
import com.enioka.jqm.engine.JqmEngineFactory;
import com.enioka.jqm.engine.JqmEngineOperations;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.service.EngineCallback;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.hsqldb.Server;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JqmBaseTest
{
    public static Logger jqmlogger = LoggerFactory.getLogger(JqmBaseTest.class);
    public static Server s;
    protected static Db db;
    public Map<String, JqmEngineOperations> engines = new HashMap<>();
    public List<DbConn> cnxs = new ArrayList<>();

    protected DbConn cnx;

    @Rule
    public TestName testName = new TestName();

    @Configuration
    public Option[] config()
    {
        return options(
                // OSGI DECLARATIVE SERVICES
                mavenBundle("org.osgi", "org.osgi.service.cm").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.scr", "2.1.24"),
                mavenBundle("org.osgi", "org.osgi.util.promise", "1.1.1"), mavenBundle("org.osgi", "org.osgi.util.function", "1.1.0"),

                // Our test database (for most tests)
                mavenBundle("org.hsqldb", "hsqldb").versionAsInProject(),

                // Apache commons
                mavenBundle("commons-io", "commons-io").versionAsInProject(),
                mavenBundle("commons-lang", "commons-lang", "2.6").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-lang3", "3.11").versionAsInProject(),
                mavenBundle("org.apache.httpcomponents", "httpclient-osgi", "4.5.13"),
                mavenBundle("org.apache.httpcomponents", "httpcore-osgi", "4.4.14"),

                // Cron
                wrappedBundle(mavenBundle("it.sauronsoftware.cron4j", "cron4j").versionAsInProject()),

                // Everything for Jetty
                mavenBundle("javax.servlet", "javax.servlet-api").versionAsInProject(),
                mavenBundle("javax.annotation", "javax.annotation-api").versionAsInProject(),
                mavenBundle("javax.transaction", "javax.transaction-api", "1.2"),
                mavenBundle("javax.interceptor", "javax.interceptor-api", "1.2.2"),
                mavenBundle("jakarta.activation", "jakarta.activation-api", "1.2.2"),
                wrappedBundle(mavenBundle("javax.enterprise", "cdi-api", "1.0")), // versions should resolve once service is OK
                mavenBundle("org.ow2.asm", "asm").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-annotations").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-http").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-io").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-jndi").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-plus").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-security").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-server").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-servlet").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-util").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-webapp").versionAsInProject(),
                mavenBundle("org.eclipse.jetty", "jetty-xml").versionAsInProject(),

                // Web security
                mavenBundle("org.apache.shiro", "shiro-core").versionAsInProject(),
                mavenBundle("org.apache.shiro", "shiro-web").versionAsInProject(),
                mavenBundle("org.bouncycastle", "bcpkix-jdk15on").versionAsInProject(),
                mavenBundle("org.bouncycastle", "bcprov-jdk15on").versionAsInProject(),

                // CLI
                wrappedBundle(mavenBundle("com.beust", "jcommander").versionAsInProject()),

                // LOG
                mavenBundle("org.slf4j", "jul-to-slf4j").versionAsInProject(),
                // mavenBundle("org.slf4j", "slf4j-log4j12").versionAsInProject(),

                // XML
                wrappedBundle(mavenBundle("org.jdom", "jdom").versionAsInProject()), mavenBundle("javax.xml.bind", "jaxb-api", "2.3.1"),

                // Maven resolver libs
                wrappedBundle(mavenBundle("org.jboss.shrinkwrap.resolver", "shrinkwrap-resolver-api", "3.1.3")),
                wrappedBundle(mavenBundle("org.jboss.shrinkwrap.resolver", "shrinkwrap-resolver-spi", "3.1.3")),
                wrappedBundle(mavenBundle("org.jboss.shrinkwrap.resolver", "shrinkwrap-resolver-api-maven", "3.1.3")),
                wrappedBundle(mavenBundle("org.jboss.shrinkwrap.resolver", "shrinkwrap-resolver-impl-maven", "3.1.3")),
                wrappedBundle(mavenBundle("org.jvnet.winp", "winp", "1.27")),

                // JQM tested libraries
                mavenBundle("com.enioka.jqm", "jqm-impl-hsql").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-impl-pg").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-api").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-loader").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-api-client-core").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-xml").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-service").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-pki").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-admin").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-model").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-impl-hsql").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-impl-pg").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-engine").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-runner-api").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-runner-java").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-runner-shell").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-test-helpers").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-jndi-context").versionAsInProject(),
                mavenBundle("com.enioka.jqm", "jqm-integration-tests").versionAsInProject(),

                // Junit itself
                junitBundles());
    }

    @BeforeClass
    public static void testInit() throws Exception
    {
        systemProperty("org.ops4j.pax.url.mvn.repositories").value("https://repo1.maven.org/maven2@id=central");
        systemProperty("org.ops4j.pax.url.mvn.useFallbackRepositories").value("false");

        if (db == null)
        {
            // JndiContext.createJndiContext();

            // If needed, create an HSQLDB server.
            String dbName = System.getenv("DB");
            if (dbName == null || "hsqldb".equals(dbName))
            {
                s = new Server();
                s.setDatabaseName(0, "testdbengine");
                s.setDatabasePath(0, "mem:testdbengine");
                s.setLogWriter(null);
                s.setSilent(true);
                s.start();
            }

            // In all cases load the datasource. (the helper itself will load the property file if any).
            db = DbManager.getDb();
        }
    }

    @Before
    public void beforeTest()
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test " + testName.getMethodName());

        /*
         * try { ((JndiContext) NamingManager.getInitialContext(null)).resetSingletons(); } catch (NamingException e) {
         * jqmlogger.warn("Could not purge test JNDI context", e); }
         */
        JqmClientFactory.resetClient(null);
        cnx = getNewDbSession();
        TestHelpers.cleanup(cnx);
        TestHelpers.createTestData(cnx);
        cnx.commit();
    }

    @After
    public void afterTest()
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
        JqmEngineOperations e = JqmEngineFactory.startEngine(nodeName, new EngineCallback());
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
        this.sleepms(1000 * s);
    }

    protected void sleepms(int ms)
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

    protected void waitDbStop()
    {
        while (s.getState() != 16)
        {
            this.sleepms(1);
        }
    }

    protected void simulateDbFailure()
    {
        if (db.getProduct().contains("hsql"))
        {
            jqmlogger.info("DB is going down");
            s.stop();
            this.waitDbStop();
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
            Helpers.closeQuietly(cnx);
            cnx = getNewDbSession();
        }
    }

    protected void displayAllHistoryTable()
    {
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("HH:mm:ss.SSS");
        jqmlogger.debug("==========================================================================================");
        for (JobInstance h : Query.create().run())
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
        for (JobInstance h : Query.create().setQueryHistoryInstances(false).setQueryLiveInstances(true).run())
        {
            jqmlogger.debug("JobInstance Id: " + h.getId() + " | " + h.getState() + " | JD: " + h.getApplicationName() + " | "
                    + h.getQueueName() + " | enqueue: " + format.format(h.getEnqueueDate().getTime()) + " | exec: "
                    + (h.getBeganRunningDate() != null ? format.format(h.getBeganRunningDate().getTime()) : null) + " | position: "
                    + h.getPosition());
        }
        jqmlogger.debug("==========================================================================================");
    }
}
