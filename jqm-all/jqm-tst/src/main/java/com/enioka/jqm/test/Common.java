package com.enioka.jqm.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.hsqldb.Server;

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
}
