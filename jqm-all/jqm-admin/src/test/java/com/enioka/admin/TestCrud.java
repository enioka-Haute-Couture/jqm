package com.enioka.admin;

import org.apache.log4j.BasicConfigurator;
import org.hsqldb.Server;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.enioka.api.admin.GlobalParameterDto;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;

public class TestCrud
{
    public static Server s;
    protected static Db db;
    protected DbConn cnx = null;

    @BeforeClass
    public static void testInit() throws Exception
    {
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
