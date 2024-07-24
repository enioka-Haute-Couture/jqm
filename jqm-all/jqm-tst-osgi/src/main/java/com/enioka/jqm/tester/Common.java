package com.enioka.jqm.tester;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

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

    static Properties dbProperties()
    {
        Properties p = new Properties();
        p.put("com.enioka.jqm.jdbc.datasource", "jdbc/jqm");
        p.put("com.enioka.jqm.jdbc.waitForConnectionValid", "false");
        p.put("com.enioka.jqm.jdbc.waitForSchemaValid", "false");
        return p;
    }

    static void createJobDef(DbConn cnx, TestJobDefinitionImpl d, Map<String, Long> queues)
    {
        Long clId = Cl.create(cnx, d.getSpecificIsolationContext() == null ? d.getName() : d.getSpecificIsolationContext(),
                d.isChildFirstClassLoader(), d.getHiddenJavaClasses(), d.isClassLoaderTracing(), false, null);

        JobDef.create(cnx, d.getDescription(), d.getJavaClassName(), d.parameters, d.getPath(),
                d.getQueueName() != null ? queues.get(d.getQueueName()) : queues.values().iterator().next(), 0, d.getName(),
                d.getApplication(), d.getModule(), d.getKeyword1(), d.getKeyword2(), d.getKeyword3(), d.isHighlander(), clId,
                d.getPathType());
    }

}
