package com.enioka.jqm.test;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

public class TestResolver
{
    @Test
    public void getVersion()
    {
        String version = Common.getMavenVersion();
        Assert.assertNotNull(version);
        Assert.assertNotEquals("${project.version}", version);
    }

    @Test
    public void testResolverSimple()
    {
        HashSet<MavenDependency> allDeps = MavenResolver.getArtifactDependencies("com.enioka.jqm:jqm-engine:" + Common.getMavenVersion());
        Assert.assertTrue(allDeps.size() > 10);
    }
}
