package com.enioka.jqm.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobInstance;

public class LibraryResolverMaven
{
    private static Logger jqmlogger = Logger.getLogger(LibraryResolverMaven.class);

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
            List<GlobalParameter> repolist = GlobalParameter.select(cnx, "globalprm_select_by_key", "mavenRepo");
            REPO_LIST = new ArrayList<String>(repolist.size());
            for (GlobalParameter gp : repolist)
            {
                REPO_LIST.add(gp.getValue());
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
            }
            resolver = resolver.withRemoteRepo(MavenRemoteRepositories.createRemoteRepository(repo, repo, "default")
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
