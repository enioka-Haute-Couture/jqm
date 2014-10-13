package com.enioka.jqm.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.test.helpers.TestHelpers;
import com.saucelabs.common.SauceOnDemandAuthentication;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import com.saucelabs.junit.ConcurrentParameterized;
import com.saucelabs.junit.SauceOnDemandTestWatcher;

/**
 * Selenium tests for administration GUI
 */
@RunWith(ConcurrentParameterized.class)
public class AdminSeleniumTest implements SauceOnDemandSessionIdProvider
{
    private static Logger jqmlogger = Logger.getLogger(AdminSeleniumTest.class);

    // Authentication uses values from system or environment variables
    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication();
    public String seleniumBaseUrl = System.getProperty("SAUCE_URL");
    public String tunnelId = System.getenv("TRAVIS_JOB_NUMBER");
    public String travisBuildNumber = System.getenv("TRAVIS_BUILD_NUMBER");
    public String travisBranch = System.getenv("TRAVIS_BRANCH");
    public String travisCommit = System.getenv("TRAVIS_COMMIT");
    public String travisJdk = System.getenv("TRAVIS_JDK_VERSION");

    @Rule
    public SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);
    @Rule
    public TestName testName = new TestName();

    private String browser;
    private String os;
    private String version;

    private String sauceJobId;
    private WebDriver driver;

    static JqmEngine engine1;
    static EntityManager em;
    public static org.hsqldb.Server s;
    static Node n;

    /**
     * @return a LinkedList containing String arrays representing the browser combinations the test should be run against. The values in the
     *         String array are used as part of the invocation of the test constructor
     */
    @ConcurrentParameterized.Parameters
    public static List<String[]> browsersStrings()
    {
        LinkedList<String[]> browsers = new LinkedList<String[]>();
        browsers.add(new String[] { "Windows 8.1", "11", "internet explorer" });
        browsers.add(new String[] { "OSX 10.8", "6", "safari" });
        return browsers;
    }

    static String getMavenVersion()
    {
        return System.getProperty("mavenVersion");
    }

    /**
     * Constructs a new instance of the test. The constructor requires three string parameters, which represent the operating system,
     * version and browser to be used when launching a Sauce VM. The order of the parameters should be the same as that of the elements
     * within the {@link #browsersStrings()} method.
     * 
     * @param os
     * @param version
     * @param browser
     */
    public AdminSeleniumTest(String os, String version, String browser)
    {
        super();
        this.os = os;
        this.version = version;
        this.browser = browser;
    }

    @BeforeClass
    public static void startServer() throws Exception
    {
        JndiContext.createJndiContext();
        s = new org.hsqldb.Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();

        // Test envt
        em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createTestData(em);

        // Start in SSL mode with web services
        File jar = FileUtils.listFiles(new File("../jqm-ws/target/"), new String[] { "war" }, false).iterator().next();
        FileUtils.copyFile(jar, new File("./webapp/jqm-ws.war"));
        Helpers.setSingleParam("disableWsApi", "false", em);
        Helpers.setSingleParam("enableWsApiAuth", "false", em);
        Helpers.setSingleParam("enableWsApiSsl", "false", em);

        em.getTransaction().begin();
        TestHelpers.node.setLoadApiAdmin(true);
        TestHelpers.node.setLoadApiClient(true);
        TestHelpers.node.setLoapApiSimple(true);
        TestHelpers.node.setPort(8080); // A standard port is required by Sauce Connect
        TestHelpers.node.setDns("localhost");
        em.getTransaction().commit();

        engine1 = new JqmEngine();
        engine1.start("localhost");

        n = em.find(Node.class, TestHelpers.node.getId());
        em.refresh(n);
    }

    @AfterClass
    public static void stopServer() throws NamingException
    {
        engine1.stop();
        em.close();
        JqmClientFactory.resetClient(null);
        Helpers.resetEmf();
        ((JndiContext) NamingManager.getInitialContext(null)).resetSingletons();
        s.shutdown();
        s.stop();
    }

    @After
    public void tearDown() throws Exception
    {
        driver.quit();
    }

    @Override
    public String getSessionId()
    {
        return sauceJobId;
    }

    /**
     * Constructs a new {@link RemoteWebDriver} instance which is configured to use the capabilities defined by the {@link #browser},
     * {@link #version} and {@link #os} instance variables, and which is configured to run against ondemand.saucelabs.com, using the
     * username and access key populated by the {@link #authentication} instance.
     * 
     * @throws Exception
     *             if an error occurs during the creation of the {@link RemoteWebDriver} instance.
     */
    @Before
    public void setUp() throws Exception
    {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        // Classic Selenium capabilities
        capabilities.setCapability(CapabilityType.BROWSER_NAME, browser);
        if (version != null)
        {
            capabilities.setCapability(CapabilityType.VERSION, version);
        }
        capabilities.setCapability(CapabilityType.PLATFORM, os);

        // Sauce tags
        capabilities.setCapability("name", testName.getMethodName());
        if (tunnelId != null)
        {
            capabilities.setCapability("tunnel-identifier", tunnelId);
        }
        if (travisBuildNumber != null)
        {
            capabilities.setCapability("build", travisBuildNumber);
        }

        List<String> tags = new ArrayList<String>();
        if (travisJdk != null)
        {
            tags.add("CI");
            tags.add(travisJdk);
        }
        else
        {
            tags.add("MANUAL");
        }
        tags.add(getMavenVersion());
        if (travisBranch != null)
        {
            tags.add(travisBranch);
        }
        /*
         * if (travisCommit != null) { tags.add(travisCommit); }
         */

        if (tags.size() > 0)
        {
            capabilities.setCapability("tags", tags.toArray());
        }

        // Connection
        this.driver = new RemoteWebDriver(new URL("http://" + authentication.getUsername() + ":" + authentication.getAccessKey() + "@"
                + seleniumBaseUrl), capabilities);
        this.sauceJobId = (((RemoteWebDriver) driver).getSessionId()).toString();
    }

    /**
     * Runs a simple test verifying the title of the admin homepage.
     */
    @Test
    public void homepage() throws Exception
    {
        driver.get("http://127.0.0.1:8080");
        assertEquals("JQM administration", driver.getTitle());
    }
}
