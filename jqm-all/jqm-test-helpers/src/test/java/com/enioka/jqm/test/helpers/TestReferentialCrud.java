package com.enioka.jqm.test.helpers;

import org.hsqldb.Server;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Test;

import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;

public class TestReferentialCrud
{
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
