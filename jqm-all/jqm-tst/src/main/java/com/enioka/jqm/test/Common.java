package com.enioka.jqm.test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.hsqldb.Server;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Queue;

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

    static Properties jpaProperties(Server s)
    {
        Properties p = new Properties();
        p.put("hibernate.hbm2ddl.auto", "update");
        p.put("hibernate.dialect", "com.enioka.jqm.tools.HSQLDialect7479");
        p.put("hibernate.pool_size", 5);
        p.put("javax.persistence.nonJtaDataSource", "");
        p.put("hibernate.connection.url", "jdbc:hsqldb:hsql://localhost/" + s.getDatabaseName(0, false));
        return p;
    }

    static JobDef createJobDef(TestJobDefinition d, Map<String, Queue> queues)
    {
        JobDef j = new JobDef();

        j.setApplicationName(d.getName());
        j.setDescription(d.getDescription());

        j.setJavaClassName(d.getJavaClassName());
        j.setJarPath(d.getPath());
        j.setPathType(d.getPathType());

        j.setParameters(d.getParameters());
        j.setQueue(d.getQueueName() != null ? queues.get(d.getQueueName()) : queues.values().iterator().next());
        j.setHighlander(d.isHighlander());

        j.setApplication(d.getApplication());
        j.setModule(d.getModule());
        j.setKeyword1(d.getKeyword1());
        j.setKeyword2(d.getKeyword2());
        j.setKeyword3(d.getKeyword3());

        j.setSpecificIsolationContext(d.getSpecificIsolationContext());
        j.setChildFirstClassLoader(d.isChildFirstClassLoader());
        j.setHiddenJavaClasses(d.getHiddenJavaClasses());
        j.setClassLoaderTracing(d.isClassLoaderTracing());

        return j;
    }
}
