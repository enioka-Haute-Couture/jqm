package com.enioka.jqm.tools;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class SpringTest extends JqmBaseTest
{
    @Before
    public void prepare()
    {
        FileUtils.deleteQuietly(new File("./target/TEST.db"));
    }

    /**
     * no specific CL - isolated launch
     */
    @Test
    public void testSimpleSingleLaunch()
    {
        CreationTools.createDatabaseProp("jdbc/spring_ds", "org.h2.Driver", "jdbc:h2:./target/TEST.db;DB_CLOSE_ON_EXIT=FALSE", "sa", "sa",
                em, "SELECT 1", null, false); // Not a singleton: driver is provided by the job itself, not in "ext".
        CreationTools.createJobDef(null, true, "com.enioka.jqm.test.spring1.Application", new ArrayList<JobDefParameter>(),
                "jqm-tests/jqm-test-spring-1/target/test.jar", TestHelpers.qVip, -1, "TestSpring1", null, null, null, null, null, false, em,
                null);

        addAndStartEngine();
        JobRequest.create("TestSpring1", null).submit();

        TestHelpers.waitFor(1, 10000, em);

        Assert.assertEquals(1, TestHelpers.getOkCount(em));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(em));
    }
}
