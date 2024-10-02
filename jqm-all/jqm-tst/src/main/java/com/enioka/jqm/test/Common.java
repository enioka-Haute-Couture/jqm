package com.enioka.jqm.test;

import com.enioka.jqm.configservices.DefaultConfigurationService;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Cl;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobDef;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

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
        p.put("com.enioka.jqm.jdbc.allowSchemaUpdate", "true");
        p.put("com.enioka.jqm.jdbc.waitForSchemaValid", "false");
        p.put("com.enioka.jqm.jdbc.datasource", "jdbc/jqm");
        return p;
    }

    /**
     * A small helper which reads a file set by Maven to get the current artifact version. Only way that works reliably both at runtime and
     * in unit tests.
     *
     * @return
     */
    static String getMavenVersion()
    {
        InputStream is = Common.class.getResourceAsStream("/version.txt");

        if (is == null)
        {
            throw new RuntimeException("Cannot find version text file, your pom file may be wrong.");
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String version = null;
        try
        {
            version = in.readLine();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return version;
    }

    static void createJobDef(DbConn cnx, TestJobDefinitionImpl d, Map<String, Long> queues)
    {
        long clId = Cl.create(cnx, d.getSpecificIsolationContext() == null ? d.getName() : d.getSpecificIsolationContext(),
                d.isChildFirstClassLoader(), d.getHiddenJavaClasses(), d.isClassLoaderTracing(), false, null);

        JobDef.create(cnx, d.getDescription(), d.getJavaClassName(), d.parameters, d.getPath(),
                d.getQueueName() != null ? queues.get(d.getQueueName()) : queues.values().iterator().next(), 0, d.getName(),
                d.getApplication(), d.getModule(), d.getKeyword1(), d.getKeyword2(), d.getKeyword3(), d.isHighlander(), clId,
                d.getPathType());
    }

    public static void cleanupOperationalDbData(DbConn cnx)
    {
        cnx.runUpdate("deliverable_delete_all");
        cnx.runUpdate("message_delete_all");
        cnx.runUpdate("history_delete_all");
        cnx.runUpdate("jiprm_delete_all");
        cnx.runUpdate("ji_delete_all");
        cnx.commit();
    }

    public static void cleanupAllJobDefinitions(DbConn cnx)
    {
        cleanupOperationalDbData(cnx);

        cnx.runUpdate("jdprm_delete_all");
        cnx.runUpdate("jd_delete_all");
        cnx.runUpdate("cl_delete_all");
        cnx.commit();
    }

    public static void resetAllData(DbConn cnx)
    {
        cleanupAllJobDefinitions(cnx);

        cnx.runUpdate("dp_delete_all");
        cnx.runUpdate("q_delete_all");
        cnx.runUpdate("node_delete_all");

        DefaultConfigurationService.updateConfiguration(cnx);
        cnx.runUpdate("q_delete_all"); // remove default queue created by DefaultConfigurationService
        GlobalParameter.setParameter(cnx, "defaultConnection", "");
        GlobalParameter.setParameter(cnx, "disableWsApi", "true");
        GlobalParameter.setParameter(cnx, "logFilePerLaunch", "false");

        cnx.commit();
    }
}
