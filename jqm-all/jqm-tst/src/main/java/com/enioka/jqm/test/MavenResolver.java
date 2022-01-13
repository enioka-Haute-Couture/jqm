package com.enioka.jqm.test;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very simple Maven-Resolver/Aether resolution of an artifact in order to get all transitive dependencies.<br>
 * It makes the hypothesis that everything useful is already inside the local repository. Also, local repository must be at ~/.m2 or
 * designated by Maven usual system property <code>maven.repo.local</code> - settings.xml is not used (for now).
 */
class MavenResolver
{
    private static Logger jqmlogger = LoggerFactory.getLogger(MavenResolver.class);

    static HashSet<MavenDependency> getArtifactDependencies(String mavenCoordinates)
    {
        DependencyFilter filter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE); // , JavaScopes.PROVIDED);
        CollectRequest cr = new CollectRequest();
        cr.setRoot(new Dependency(new DefaultArtifact(mavenCoordinates), JavaScopes.COMPILE));
        DependencyRequest dependencyRequest = new DependencyRequest(cr, filter);

        RepositorySystem system = getRepositorySystemFromServiceLocator();
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        DependencySelector depFilter = new org.eclipse.aether.util.graph.selector.AndDependencySelector(
                new org.eclipse.aether.util.graph.selector.ScopeDependencySelector("test", "provided", "runtime"),
                new org.eclipse.aether.util.graph.selector.OptionalDependencySelector(),
                new org.eclipse.aether.util.graph.selector.ExclusionDependencySelector());
        session.setDependencySelector(depFilter);

        LocalRepository localRepo = new LocalRepository(getLocalRepoPath());
        jqmlogger.info("Using {} as local artifact repository", localRepo.getBasedir());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        DependencyResult result;
        try
        {
            result = system.resolveDependencies(session, dependencyRequest);
        }
        catch (DependencyResolutionException e)
        {
            jqmlogger.error("Could not determine dependencies of artifact " + mavenCoordinates, e);
            throw new RuntimeException(e);
        }

        HashSet<MavenDependency> allDeps = new HashSet<>();
        walkNodeRecursive(result.getRoot(), 0, allDeps);

        return allDeps;
    }

    private static String getLocalRepoPath()
    {
        // TODO: try and read ~/.m2/settings.xml one day.
        return System.getProperty("maven.repo.local", System.getProperty("user.home") + "/.m2/repository/");
    }

    private static RepositorySystem getRepositorySystemFromServiceLocator()
    {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        // locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        // locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler()
        {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception)
            {
                jqmlogger.error("Service creation failed for {} with implementation {}", type, impl, exception);
            }
        });

        return locator.getService(RepositorySystem.class);
    }

    private static void walkNodeRecursive(DependencyNode node, int level, HashSet<MavenDependency> dependencies)
    {
        MavenDependency dep = new MavenDependency(node);
        dependencies.add(dep);

        String tabs = StringUtils.repeat("\t", level);
        jqmlogger.info("{}{}", tabs, dep.toString());

        for (DependencyNode childNode : node.getChildren())
        {
            walkNodeRecursive(childNode, level + 1, dependencies);
        }
    }
}
