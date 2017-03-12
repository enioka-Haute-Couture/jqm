package com.enioka.jqm.jdbc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ScriptRunner
{
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
                sb.append(line);
                sb.append(nl);

                if (line.contains(";"))
                {
                    // End of order - run it.
                    System.out.println(sb.toString());
                    cnx.runRawUpdate(sb.toString());
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
            try
            {
                isr.close();
            }
            catch (Exception e)
            {
                // Ignore.
            }
            try
            {
                is.close();
            }
            catch (Exception e)
            {
                // Ignore.
            }
        }

    }
}
