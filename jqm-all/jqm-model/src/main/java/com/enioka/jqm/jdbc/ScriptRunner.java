package com.enioka.jqm.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptRunner
{
    private static Logger jqmlogger = LoggerFactory.getLogger(ScriptRunner.class);
    private static String nl = System.getProperty("line.separator");

    public static void run(DbConn cnx, String classpath)
    {
        InputStream is = null;

        try
        {
            is = ScriptRunner.class.getResourceAsStream(classpath);
        }
        catch (Exception e)
        {
            throw new RuntimeException("cannot find db script " + classpath, e);
        }
        if (is == null)
        {
            throw new RuntimeException("cannot find db script " + classpath);
        }

        InputStreamReader isr = null;
        String line;
        StringBuilder sb = new StringBuilder();
        try
        {
            isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            line = br.readLine();
            while (line != null)
            {
                sb.append(line.replace("~", ";"));
                sb.append(nl);

                if (line.contains(";"))
                {
                    // End of order - run it.
                    cnx.runRawUpdate(sb.substring(0, sb.length() - 1 - nl.length()));
                    sb = new StringBuilder();
                }

                line = br.readLine();
            }

        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("SQL script encoding issue", e);
        }
        catch (IOException e)
        {
            throw new RuntimeException("SQL script cannot be read", e);
        }
        finally
        {
            DbHelper.closeQuietly(isr);
            DbHelper.closeQuietly(is);
        }

        jqmlogger.trace("File {} was run OK", classpath);
    }
}
