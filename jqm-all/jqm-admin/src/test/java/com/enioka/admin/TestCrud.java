package com.enioka.admin;

import org.apache.log4j.BasicConfigurator;
import org.hsqldb.Server;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.ops4j.pax.exam.CoreOptions.*;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import com.enioka.api.admin.GlobalParameterDto;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TestCrud
{
    public static Server s;
    protected static Db db;
    protected DbConn cnx = null;

    @Configuration
    public Option[] config()
    {
        return options(
            wrappedBundle(mavenBundle("log4j", "log4j", "1.2.17")),
            wrappedBundle(mavenBundle("org.slf4j", "slf4j-api", "1.7.25")),
            wrappedBundle(mavenBundle("org.hsqldb", "hsqldb", "2.3.4")),
            wrappedBundle(mavenBundle("javax.servlet", "servlet-api", "2.5")),
            wrappedBundle(mavenBundle("org.apache.shiro", "shiro-core", "1.3.2")),
            wrappedBundle(mavenBundle("org.apache.shiro", "shiro-web", "1.3.2")),
            wrappedBundle(mavenBundle("javax.activation", "activation", "1.1.1")),
            mavenBundle("javax.xml.stream", "stax-api", "1.0-2"),
            mavenBundle("javax.xml.bind", "jaxb-api", "2.3.1"),
            mavenBundle("com.enioka.jqm", "jqm-admin", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-loader", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-model", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-impl-hsql", "3.0.0-SNAPSHOT"),
            mavenBundle("com.enioka.jqm", "jqm-impl-pg", "3.0.0-SNAPSHOT"),
            junitBundles()
            );
    }

    @BeforeClass
    public static void testInit() throws Exception
    {
        
        systemProperty("org.ops4j.pax.url.mvn.repositories").value("https://repo1.maven.org/maven2@id=central");
        systemProperty("org.ops4j.pax.url.mvn.useFallbackRepositories").value("false");

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        if (s == null)
        {
            s = new Server();
            s.setDatabaseName(0, "testdbengine");
            s.setDatabasePath(0, "mem:testdbengine");
            s.setLogWriter(null);
            s.setSilent(true);
            s.start();

            JDBCDataSource ds = new JDBCDataSource();
            ds.setDatabase("jdbc:hsqldb:mem:testdbengine");
            db = new Db(ds, true);
        }
    }

    @Before
    public void beforeEachTest()
    {
        cnx = db.getConn();
        MetaService.deleteAllMeta(cnx, true);
        cnx.commit();
    }

    @After
    public void afterEachTest()
    {
        if (cnx != null)
        {
            cnx.close();
        }
    }

    @Test
    public void testGP()
    {
        GlobalParameterDto dto = new GlobalParameterDto();
        dto.setKey("houba");
        dto.setValue("hop");
        MetaService.upsertGlobalParameter(cnx, dto);

        GlobalParameterDto dto2 = new GlobalParameterDto();
        dto2.setKey("key2");
        dto2.setValue("value2");
        MetaService.upsertGlobalParameter(cnx, dto2);
        cnx.commit();

        Assert.assertEquals("hop", MetaService.getGlobalParameter(cnx, "houba").getValue());
        Assert.assertEquals("hop", MetaService.getGlobalParameter(cnx, MetaService.getGlobalParameter(cnx, "houba").getId()).getValue());
        Assert.assertEquals(2, MetaService.getGlobalParameter(cnx).size());
    }

}
