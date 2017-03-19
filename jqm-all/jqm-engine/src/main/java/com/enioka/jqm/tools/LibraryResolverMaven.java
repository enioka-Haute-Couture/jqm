package com.enioka.jqm.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JobInstance;

public class LibraryResolverMaven
{
    private static Logger jqmlogger = Logger.getLogger(LibraryResolverMaven.class);

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
        List<GlobalParameter> repolist = GlobalParameter.select(cnx, "mavenRepo");
        String settings = GlobalParameter.getParameter(cnx, "mavenSettingsCL", null);
        String settingsFile = GlobalParameter.getParameter(cnx, "mavenSettingsFile", null);

        boolean withCentral = false;
        String withCustomSettings = null;
        String withCustomSettingsFile = null;
        if (settings != null && settingsFile == null)
        {
            jqmlogger.trace("Custom settings file will be used: " + settings);
            withCustomSettings = settings;
        }
        if (settingsFile != null)
        {
            jqmlogger.trace("Custom settings file will be used: " + settingsFile);
            withCustomSettingsFile = settingsFile;
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

        for (GlobalParameter gp : repolist)
        {
            if (gp.getValue().contains("repo1.maven.org"))
            {
                withCentral = true;
            }
            resolver = resolver
                    .withRemoteRepo(MavenRemoteRepositories.createRemoteRepository(gp.getId().toString(), gp.getValue(), "default")
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
