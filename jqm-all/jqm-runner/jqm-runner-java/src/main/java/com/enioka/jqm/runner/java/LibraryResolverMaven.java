package com.enioka.jqm.runner.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JobInstance;

import org.apache.commons.io.FilenameUtils;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenUpdatePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LibraryResolverMaven
{
    private static Logger jqmlogger = LoggerFactory.getLogger(LibraryResolverMaven.class);

    private static List<String> repoList = null;
    private static String mavenSettingsCl = null;
    private static String mavenSettingsFile = null;

    private String[] mavenRepos;
    private String mavenSettingsClPath;
    private String mavenSettingsFilePath;

    LibraryResolverMaven(DbConn cnx)
    {
        mavenRepos = GlobalParameter.getParameter(cnx, "mavenRepo", "http://repo1.maven.org/maven2/").split(",");
        mavenSettingsClPath = GlobalParameter.getParameter(cnx, "mavenSettingsCL", null);
        mavenSettingsFilePath = GlobalParameter.getParameter(cnx, "mavenSettingsFile", null);
    }

    URL[] resolve(JobInstance ji) throws JqmPayloadException
    {
        ConfigurableMavenResolverSystem resolver = getMavenResolver();

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

    ConfigurableMavenResolverSystem getMavenResolver()
    {
        // Retrieve resolver configuration
        if (repoList == null)
        {
            repoList = new ArrayList<>(5);
            for (String gp : mavenRepos)
            {
                repoList.add(gp);
            }

            mavenSettingsCl = mavenSettingsClPath;
            mavenSettingsFile = mavenSettingsFilePath;
        }

        // Resolver. Sadly Shrinkwrap uses CL hacks and we must take them into account here.
        ClassLoader currentCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(LibraryResolverMaven.class.getClassLoader()); // Will usually be the engine CL.
        ConfigurableMavenResolverSystem resolver = Maven.configureResolver();

        // settings.xml?
        if (mavenSettingsCl != null && mavenSettingsFile == null)
        {
            jqmlogger.trace("Custom settings file from class-path will be used: " + mavenSettingsCl);
            resolver.fromClassloaderResource(mavenSettingsCl);
        }
        if (mavenSettingsFile != null)
        {
            jqmlogger.trace("Custom settings file from file system will be used: " + mavenSettingsFile);
            resolver.fromFile(mavenSettingsFile);
        }
        Thread.currentThread().setContextClassLoader(currentCl); // only now (needed for CL search)

        // Repositories to use.
        boolean withCentral = false;
        for (String repo : repoList)
        {
            if (repo.contains("repo1.maven.org"))
            {
                withCentral = true;
                jqmlogger.trace("Using Maven central as a Maven repository");
                continue;
            }
            jqmlogger.trace("Using Maven repository {}", repo);
            resolver = resolver
                    .withRemoteRepo(MavenRemoteRepositories.createRemoteRepository("repo" + Math.abs(repo.hashCode()), repo, "default")
                            .setUpdatePolicy(MavenUpdatePolicy.UPDATE_POLICY_NEVER));
        }
        resolver.withMavenCentralRepo(withCentral);
        return resolver;
    }

    URL[] extractMavenResults(File[] depFiles) throws JqmPayloadException
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
