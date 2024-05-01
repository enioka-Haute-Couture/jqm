package com.enioka.jqm.test;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.aether.graph.DependencyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All the info we need about a Maven artifact in order to start it inside an OSGi framework.
 */
class MavenDependency
{
    private static Logger jqmlogger = LoggerFactory.getLogger(MavenDependency.class);

    private File file;
    private String coordinates;
    private String scope;
    private String bundleName;

    MavenDependency(DependencyNode node)
    {
        this.file = node.getArtifact().getFile();
        this.coordinates = node.getArtifact().toString();
        this.scope = node.getDependency() != null ? node.getDependency().getScope() : "root";
    }

    @Override
    public int hashCode()
    {
        return coordinates.hashCode();
    }

    private void refreshOsgiDataIfNeeded()
    {
        if (bundleName != null || this.file == null || !this.file.getName().endsWith(".jar"))
        {
            return;
        }

        JarFile jarFile = null;
        try
        {
            jarFile = new JarFile(file);
            Manifest m = jarFile.getManifest();

            if (m == null || m.getMainAttributes() == null)
            {
                return;
            }

            bundleName = m.getMainAttributes().getValue("Bundle-SymbolicName");
        }
        catch (IOException e)
        {
            jqmlogger.warn("Could not read jar manifest", e);
            return;
        }
        finally
        {
            if (jarFile != null)
            {
                try
                {
                    jarFile.close();
                }
                catch (IOException e)
                {
                    // nothing to do.
                }
            }
        }
    }

    public boolean isOsgiBundle()
    {
        refreshOsgiDataIfNeeded();
        return this.bundleName != null;
    }

    @Override
    public String toString()
    {
        return this.coordinates + " - " + this.scope + " - " + (this.file != null ? this.file.getAbsolutePath() : "NO_FILE") + " - "
                + (isOsgiBundle() ? "bundle " + getBundleName() : "not an OSGi bundle");
    }

    public String getPaxUrl()
    {
        if (this.file == null)
        {
            return null;
        }
        // return isOsgiBundle() ? "file:" + this.coordinates.replace(':', '/') : "wrap:file:" + this.coordinates.replace(':', '/');
        return (isOsgiBundle() ? "file:" : "wrap:file:") + this.file.getAbsolutePath();
    }

    public String getBundleName()
    {
        refreshOsgiDataIfNeeded();
        return this.bundleName;
    }

    public File getFile()
    {
        return file;
    }
}
