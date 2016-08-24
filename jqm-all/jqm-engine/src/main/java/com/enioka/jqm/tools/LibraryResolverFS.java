/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.jqm.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.persistence.EntityManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Node;

/**
 * The cache is responsible for resolving the dependencies of a payload (from a pom, from a lib directory, ...). As the resolution is
 * costly, it is only done the first time and cached afterwards. <br>
 * Cache invalidation is done by analyzing the last modification date of the payload jar and of the lib directory (if any) on each call.<br>
 * There is one library cache per engine.<br>
 * This object is thread-safe.
 */
class LibraryResolverFS
{
    private static Logger jqmlogger = Logger.getLogger(LibraryResolverFS.class);

    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static class JobDefLibrary
    {
        URL[] urls;
        Date loadTime;
    }

    private Map<String, JobDefLibrary> cache = new HashMap<String, LibraryResolverFS.JobDefLibrary>();

    /**
     * 
     * @param n
     *            the JQM Node that holds the binaries (local node)
     * @param jd
     *            the JobDefinition that should be resolved
     * @param em
     *            an EM that will be used only if not in cache, to fetch the Maven repository list from the database.
     * @throws JqmPayloadException
     */
    synchronized URL[] getLibraries(Node n, JobDef jd, EntityManager em) throws JqmPayloadException
    {
        if (shouldLoad(n, jd))
        {
            loadCache(n, jd, em);
        }
        return cache.get(jd.getApplicationName()).urls;
    }

    /**
     * Returns true if the libraries should be loaded in cache. Two cases: never loaded and should be reloaded (jar is more recent than
     * cache)
     */
    private boolean shouldLoad(Node node, JobDef jd)
    {
        if (!cache.containsKey(jd.getApplicationName()))
        {
            return true;
        }
        // If here: cache exists.
        JobDefLibrary libs = cache.get(jd.getApplicationName());

        // Is cache stale?
        Date lastLoaded = libs.loadTime;
        File jarFile = new File(FilenameUtils.concat(new File(node.getRepo()).getAbsolutePath(), jd.getJarPath()));
        File jarDir = jarFile.getParentFile();
        File libDir = new File(FilenameUtils.concat(jarDir.getAbsolutePath(), "lib"));

        if (lastLoaded.before(new Date(jarFile.lastModified())) || lastLoaded.before(new Date(jarDir.lastModified()))
                || lastLoaded.before(new Date(libDir.lastModified())))
        {
            jqmlogger.info("The cache for application " + jd.getApplicationName() + " will be reloaded");
            return true;
        }

        // If here, the cache is OK
        return false;
    }

    private void loadCache(Node node, JobDef jd, EntityManager em) throws JqmPayloadException
    {
        jqmlogger.debug("Resolving classpath for job definition " + jd.getApplicationName());

        File jarFile = new File(FilenameUtils.concat(new File(node.getRepo()).getAbsolutePath(), jd.getJarPath()));
        File jarDir = jarFile.getParentFile();
        File libDir = new File(FilenameUtils.concat(jarDir.getAbsolutePath(), "lib"));
        File libDirExtracted = new File(FilenameUtils.concat(jarDir.getAbsolutePath(), "libFromJar"));
        File pomFile = new File(FilenameUtils.concat(jarDir.getAbsolutePath(), "pom.xml"));

        if (!jarFile.canRead())
        {
            jqmlogger.warn("Cannot read file at " + jarFile.getAbsolutePath()
                    + ". Job instance will crash. Check job definition or permissions on file.");
            throw new JqmPayloadException("File " + jarFile.getAbsolutePath() + " cannot be read");
        }

        // POM file should be deleted if it comes from the jar file. Otherwise, it would stay into place and modifications to the internal
        // pom would be ignored.
        boolean pomFromJar = false;

        // 1st: if no pom, no lib dir => find a pom inside the JAR. (& copy it, we will read later)
        if (!pomFile.exists() && !libDir.exists())
        {
            jqmlogger.trace("No pom inside jar directory. Checking for a pom inside the jar file");
            InputStream is = null;
            FileOutputStream os = null;
            ZipFile zf = null;

            try
            {
                zf = new ZipFile(jarFile);
                Enumeration<? extends ZipEntry> zes = zf.entries();
                while (zes.hasMoreElements())
                {
                    ZipEntry ze = zes.nextElement();
                    if (ze.getName().endsWith("pom.xml"))
                    {

                        is = zf.getInputStream(ze);
                        os = new FileOutputStream(pomFile);
                        IOUtils.copy(is, os);
                        pomFromJar = true;
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                throw new JqmPayloadException("Could not handle pom inside jar", e);
            }
            finally
            {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
                Helpers.closeQuietly(zf);
            }
        }

        // 2nd: no pom, no pom inside jar, no lib dir => find a lib dir inside the jar
        if (!pomFile.exists() && !libDir.exists())
        {
            jqmlogger.trace("Checking for a lib directory inside jar");
            InputStream is = null;
            FileOutputStream os = null;
            ZipFile zf = null;
            FileUtils.deleteQuietly(libDirExtracted);

            try
            {
                zf = new ZipFile(jarFile);
                Enumeration<? extends ZipEntry> zes = zf.entries();
                while (zes.hasMoreElements())
                {
                    ZipEntry ze = zes.nextElement();
                    if (ze.getName().startsWith("lib/") && ze.getName().endsWith(".jar"))
                    {
                        if (!libDirExtracted.isDirectory() && !libDirExtracted.mkdir())
                        {
                            throw new JqmPayloadException("Could not extract libraries from jar");
                        }

                        is = zf.getInputStream(ze);
                        os = new FileOutputStream(
                                FilenameUtils.concat(libDirExtracted.getAbsolutePath(), FilenameUtils.getName(ze.getName())));
                        IOUtils.copy(is, os);
                    }
                }
            }
            catch (Exception e)
            {
                throw new JqmPayloadException("Could not handle internal lib directory", e);
            }
            finally
            {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
                Helpers.closeQuietly(zf);
            }

            // If libs were extracted, put in cache and return
            if (libDirExtracted.isDirectory())
            {
                FileFilter fileFilter = new WildcardFileFilter("*.jar");
                File[] files = libDirExtracted.listFiles(fileFilter);
                URL[] libUrls = new URL[files.length];
                try
                {
                    for (int i = 0; i < files.length; i++)
                    {
                        libUrls[i] = files[i].toURI().toURL();
                    }
                }
                catch (Exception e)
                {
                    throw new JqmPayloadException("Could not handle internal lib directory", e);
                }

                // Put in cache
                putInCache(libUrls, jd.getApplicationName());
                return;
            }
        }

        // 3rd: if pom, use pom!
        if (pomFile.exists() && !libDir.exists())
        {
            jqmlogger.trace("Reading a pom file");

            ConfigurableMavenResolverSystem resolver = LibraryResolverMaven.getMavenResolver(em);

            // Resolve
            File[] depFiles = null;
            try
            {
                depFiles = resolver.loadPomFromFile(pomFile).importRuntimeDependencies().resolve().withTransitivity().asFile();
            }
            catch (IllegalArgumentException e)
            {
                // Happens when no dependencies inside pom, which is a weird use of the feature...
                jqmlogger.trace("No dependencies inside pom.xml file - no libs will be used", e);
                depFiles = new File[0];
            }

            // Extract results
            URL[] tmp = LibraryResolverMaven.extractMavenResults(depFiles);

            // Put in cache
            putInCache(tmp, jd.getApplicationName());

            // Cleanup
            if (pomFromJar && !pomFile.delete())
            {
                jqmlogger.warn("Could not delete the temp pom file extracted from the jar.");
            }
            return;
        }

        // 4: if lib, use lib... (lib has priority over pom)
        if (libDir.exists())
        {
            jqmlogger.trace("Using the lib directory " + libDir.getAbsolutePath() + " as the source for dependencies");
            FileFilter fileFilter = new WildcardFileFilter("*.jar");
            File[] files = libDir.listFiles(fileFilter);
            URL[] tmp = new URL[files.length];
            for (int i = 0; i < files.length; i++)
            {
                try
                {
                    tmp[i] = files[i].toURI().toURL();
                }
                catch (MalformedURLException e)
                {
                    throw new JqmPayloadException("incorrect file inside lib directory", e);
                }
            }

            // Put in cache
            putInCache(tmp, jd.getApplicationName());
            return;
        }

        throw new JqmPayloadException(
                "There is no lib dir or no pom.xml inside the directory containing the jar or inside the jar. The jar cannot be launched.");
    }

    private void putInCache(URL[] urls, String applicationName)
    {
        JobDefLibrary jdl = new JobDefLibrary();
        jdl.loadTime = new Date();
        jdl.urls = urls;

        this.cache.put(applicationName, jdl);
    }
}
