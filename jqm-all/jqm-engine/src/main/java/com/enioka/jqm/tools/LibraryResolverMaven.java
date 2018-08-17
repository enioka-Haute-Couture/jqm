package com.enioka.jqm.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobInstance;

public class LibraryResolverMaven
{
    private static Logger jqmlogger = LoggerFactory.getLogger(LibraryResolverMaven.class);

    private static List<String> REPO_LIST = null;
    private static String MAVEN_SETTINGS_CL = null;
    private static String MAVEN_SETTINGS_FILE = null;

    URL[] resolve(JobInstance ji, DbConn cnx) throws JqmPayloadException
    {
        ConfigurableMavenResolverSystem resolver = getMavenResolver(cnx);

        try
        {
            return extractMavenResults(resolver.resolve(ji.getJD().getJarPath()).withTransitivity().asFile());
        }
        catch (JqmPayloadException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new JqmPayloadException("Could not resolve a Maven payload path", e);
        }

    }

    static ConfigurableMavenResolverSystem getMavenResolver(DbConn cnx)
    {
        // Retrieve resolver configuration
        if (REPO_LIST == null)
        {
            REPO_LIST = new ArrayList<String>(5);
            for (String gp : GlobalParameter.getParameter(cnx, "mavenRepo", "http://repo1.maven.org/maven2/").split(","))
            {
                REPO_LIST.add(gp);
            }

            MAVEN_SETTINGS_CL = GlobalParameter.getParameter(cnx, "mavenSettingsCL", null);
            MAVEN_SETTINGS_FILE = GlobalParameter.getParameter(cnx, "mavenSettingsFile", null);
        }

        boolean withCentral = false;
        String withCustomSettings = null;
        String withCustomSettingsFile = null;
        if (MAVEN_SETTINGS_CL != null && MAVEN_SETTINGS_FILE == null)
        {
            jqmlogger.trace("Custom settings file will be used: " + MAVEN_SETTINGS_CL);
            withCustomSettings = MAVEN_SETTINGS_CL;
        }
        if (MAVEN_SETTINGS_FILE != null)
        {
            jqmlogger.trace("Custom settings file will be used: " + MAVEN_SETTINGS_FILE);
            withCustomSettingsFile = MAVEN_SETTINGS_FILE;
        }

        // Configure resolver
        ConfigurableMavenResolverSystem resolver = Maven.configureResolver();
        if (withCustomSettings != null && withCustomSettingsFile == null)
        {
            resolver.fromClassloaderResource(withCustomSettings);
        }
        if (withCustomSettingsFile != null)
        {
            resolver.fromFile(withCustomSettingsFile);
        }

        for (String repo : REPO_LIST)
        {
            if (repo.contains("repo1.maven.org"))
            {
                withCentral = true;
                continue;
            }
            resolver = resolver
                    .withRemoteRepo(MavenRemoteRepositories.createRemoteRepository("repo" + Math.abs(repo.hashCode()), repo, "default")
                            .setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER));
        }
        resolver.withMavenCentralRepo(withCentral);
        return resolver;
    }

    static URL[] extractMavenResults(File[] depFiles) throws JqmPayloadException
    {
        int size = 0;
        for (File artifact : depFiles)
        {
            if (!"pom".equals(FilenameUtils.getExtension(artifact.getName())))
            {
                size++;
            }
        }
        URL[] tmp = new URL[size];
        int i = 0;
        for (File artifact : depFiles)
        {
            if ("pom".equals(FilenameUtils.getExtension(artifact.getName())))
            {
                continue;
            }
            try
            {
                tmp[i] = artifact.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                throw new JqmPayloadException("Incorrect dependency in POM file", e);
            }
            i++;
        }

        return tmp;
    }
}
