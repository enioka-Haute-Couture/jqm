package com.enioka.jqm.test.helpers;

import org.hsqldb.Server;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.ops4j.pax.exam.CoreOptions.*;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestReferentialCrud
{
    @Configuration
    public Option[] config()
    {
        return options(
            wrappedBundle(mavenBundle("commons-io", "commons-io", "2.6")),
            wrappedBundle(mavenBundle("log4j", "log4j", "1.2.17")),
            wrappedBundle(mavenBundle("org.slf4j", "slf4j-api", "1.7.25")),
            wrappedBundle(mavenBundle("org.slf4j", "slf4j-simple", "1.7.25")).noStart(),
            wrappedBundle(mavenBundle("org.hsqldb", "hsqldb", "2.3.4")),
            wrappedBundle(mavenBundle("javax.servlet", "servlet-api", "2.5")),
            wrappedBundle(mavenBundle("org.apache.shiro", "shiro-core", "1.3.2")),
            wrappedBundle(mavenBundle("org.apache.shiro", "shiro-web", "1.3.2")),
            wrappedBundle(mavenBundle("javax.activation", "activation", "1.1.1")),
            mavenBundle("com.enioka.jqm", "jqm-test-helpers", "3.0.0-SNAPSHOT"),
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

    @Test
    public void testCreation()
    {
        Server s;
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();

        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:testdbengine");

        Db db = new Db(ds, true);
        DbConn cnx = db.getConn();

        TestHelpers.createTestData(cnx);

        cnx.close();
    }
}
