package com.enioka.jqm.repository;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionRepository
{
    private static Logger jqmlogger = LoggerFactory.getLogger(VersionRepository.class);

    public static String getMavenVersion()
    {
        String res = System.getProperty("mavenVersion");
        if (res != null)
        {
            return res;
        }

        InputStream is = VersionRepository.class.getResourceAsStream("/META-INF/maven/com.enioka.jqm/jqm-engine/pom.properties");
        Properties p = new Properties();
        try
        {
            p.load(is);
            res = p.getProperty("version");
        }
        catch (Exception e)
        {
            res = "maven version not found";
            jqmlogger.warn("Maven version not found");
        }
        return res;
    }

}
