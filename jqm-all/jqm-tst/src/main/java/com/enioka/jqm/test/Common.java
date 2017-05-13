package com.enioka.jqm.test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.hsqldb.Server;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Cl;
import com.enioka.jqm.model.JobDef;

final class Common
{
    private Common()
    {}

    static File createTempDirectory()
    {
        final File temp;
        try
        {
            temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

            if (!(temp.delete()))
            {
                throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
            }

            if (!(temp.mkdir()))
            {
                throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            // Hatred of checked exceptions.
            throw new RuntimeException(e);
        }
        return (temp);
    }

    static Server createHsqlServer()
    {
        Server s = new Server();
        String dbName = "testdb_" + Math.random();
        s.setDatabaseName(0, dbName);
        s.setDatabasePath(0, "mem:" + dbName);
        s.setLogWriter(null);
        s.setSilent(true);
        return s;
    }

    static Properties dbProperties(Server s)
    {
        Properties p = new Properties();
        p.put("com.enioka.jqm.jdbc.allowSchemaUpdate", "true");
        p.put("com.enioka.jqm.jdbc.datasource", "jdbc/jqm");
        return p;
    }

    static void createJobDef(DbConn cnx, TestJobDefinition d, Map<String, Integer> queues)
    {
        int clId = Cl.create(cnx, d.getSpecificIsolationContext() == null ? d.getName() : d.getSpecificIsolationContext(),
                d.isChildFirstClassLoader(), d.getHiddenJavaClasses(), d.isClassLoaderTracing(), false, null);

        JobDef.create(cnx, d.getDescription(), d.getJavaClassName(), d.parameters, d.getPath(),
                d.getQueueName() != null ? queues.get(d.getQueueName()) : queues.values().iterator().next(), 0, d.getName(),
                d.getApplication(), d.getModule(), d.getKeyword1(), d.getKeyword2(), d.getKeyword3(), d.isHighlander(), clId,
                d.getPathType());
    }
}
