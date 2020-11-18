package com.enioka.jqm.test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.ops4j.pax.exam.CoreOptions.*;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.State;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JqmTesterTest
{
    @Configuration
    public Option[] config()
    {
        return options(
            wrappedBundle(mavenBundle("org.hsqldb", "hsqldb", "2.3.4")),
            wrappedBundle(mavenBundle("commons-io", "commons-io", "2.6")),
            wrappedBundle(mavenBundle("commons-lang", "commons-lang", "2.6")),
            wrappedBundle(mavenBundle("org.apache.commons", "commons-lang3", "3.11")),
            wrappedBundle(mavenBundle("it.sauronsoftware.cron4j", "cron4j", "2.2.5")),
            wrappedBundle(mavenBundle("javax.servlet", "servlet-api", "2.5")),
            wrappedBundle(mavenBundle("org.apache.shiro", "shiro-core", "1.3.2")),
            wrappedBundle(mavenBundle("org.apache.shiro", "shiro-web", "1.3.2")),
            wrappedBundle(mavenBundle("org.jboss.shrinkwrap.resolver", "shrinkwrap-resolver-api", "3.1.3")),
            wrappedBundle(mavenBundle("org.jboss.shrinkwrap.resolver", "shrinkwrap-resolver-api-maven", "3.1.3")),
            wrappedBundle(mavenBundle("org.jboss.shrinkwrap.resolver", "shrinkwrap-resolver-impl-maven", "3.1.3")),
            wrappedBundle(mavenBundle("org.jvnet.winp", "winp", "1.27")),
            mavenBundle("com.enioka.jqm", "jqm-tst", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-engine", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-runner-api", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-runner-basic", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-runner-java", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-runner-shell", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-api", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-loader", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-model", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-impl-hsql", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-impl-pg", "3.0.0-SNAPSHOT"),
            junitBundles()
            );
    }

    @BeforeClass
    public static void init()
    {
        systemProperty("org.ops4j.pax.url.mvn.repositories").value("https://repo1.maven.org/maven2@id=central");
        systemProperty("org.ops4j.pax.url.mvn.useFallbackRepositories").value("false");
    }

    // Simple run
    @Test
    public void testOne()
    {
        JobInstance res = JqmTester.create("com.enioka.jqm.test.Payload1").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

    // Arguments
    @Test
    public void testTwo()
    {
        JobInstance res = JqmTester.create("com.enioka.jqm.test.Payload2").addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }

    // JNDI JDBC datasource
    @Test
    public void testThree()
    {
        JobInstance res = JqmTester.create("com.enioka.jqm.test.Payload3").addParameter("arg1", "testvalue").run();
        Assert.assertEquals(State.ENDED, res.getState());
    }
}
