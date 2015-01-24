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
import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.jboss.shrinkwrap.resolver.api.NoResolvedResultException;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.repository.MavenRemoteRepositories;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class PackageTest extends JqmBaseTest
{
    @Test
    public void testPomOnlyInJar() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemavennopom/target/test.jar", TestHelpers.qVip, 42,
                "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testNoPomLibDir() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemavennopomlib/target/test.jar", TestHelpers.qVip,
                42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", false, em);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testLibInJar() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemavenjarinlib/target/test.jar", TestHelpers.qVip,
                42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testNoDependencyDefinition() throws Exception
    {
        CreationTools.createJobDef(null, true, "App", null, "jqm-tests/jqm-test-datetimemavennolibdef/target/test.jar", TestHelpers.qVip,
                42, "MarsuApplication", null, "Franquin", "ModuleMachin", "other", "other", true, em);
        JobRequest.create("MarsuApplication", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(0, TestHelpers.getOkCount(em));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(em));
    }

    @Test
    public void testInheritedLegacyPayload() throws Exception
    {
        JqmSimpleTest.create(em, "pyl.PckJBInheritance").run(this);
    }

    // Can't work with Java6: http://bugs.java.com/view_bug.do?bug_id=4950148 (2003!)
    // @Test
    public void testLibInJarReload() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testLibInJarReload");
        EntityManager em = Helpers.getNewEm();
        TestHelpers.cleanup(em);
        TestHelpers.createTestData(em);

        ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
        JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
        jdargs.add(jdp);

        @SuppressWarnings("unused")
        JobDef jdDemoMaven = CreationTools.createJobDef(null, true, "App", jdargs,
                "jqm-tests/jqm-test-datetimemavenjarinlib/target/test.jar", TestHelpers.qVip, 42, "MarsuApplication", null, "Franquin",
                "ModuleMachin", "other", "other", true, em);

        JobRequest j = new JobRequest("MarsuApplication", "TestUser");
        JqmClientFactory.getClient().enqueue(j);

        JqmEngine engine1 = new JqmEngine();
        engine1.start("localhost");
        TestHelpers.waitFor(1, 10000, em);

        File libDir = new File("../jqm-tests/jqm-test-datetimemavenjarinlib/target/libFromJar");
        long firstDate = libDir.lastModified();

        // Modify the jar file - it should trigger a reload
        System.gc();
        Thread.sleep(5000); // Most systems are 1s precise
        File jarFile = new File("../jqm-tests/jqm-test-datetimemavenjarinlib/target/test.jar");
        boolean res = jarFile.setLastModified(firstDate + 100000);
        System.out.println(res);
        System.out.println(jarFile.isFile());
        System.out.println(firstDate);

        // New run - should reload
        JqmClientFactory.getClient().enqueue(j);
        TestHelpers.waitFor(2, 10000000, em);
        System.out.println(libDir.lastModified());

        // Check the lib dir has been reconstructed
        Assert.assertTrue(libDir.lastModified() > firstDate);
        long secondDate = libDir.lastModified();

        // Now, check that if nothing is modified, the libs are not reloaded
        JqmClientFactory.getClient().enqueue(j);
        TestHelpers.waitFor(3, 10000, em);
        Assert.assertEquals(secondDate, libDir.lastModified());

        // Done
        engine1.stop();
    }

    @Test(expected = NoResolvedResultException.class)
    public void testFailingDependency() throws Exception
    {
        jqmlogger.debug("**********************************************************");
        jqmlogger.debug("Starting test testFailingDependency");

        SLF4JBridgeHandler.install();

        Maven.configureResolver()
                .withRemoteRepo(MavenRemoteRepositories.createRemoteRepository("marsu", "http://marsupilami.com", "default"))
                .withMavenCentralRepo(false).resolve("com.enioka.jqm:marsu:1.1.4").withTransitivity().asFile();
    }
}
