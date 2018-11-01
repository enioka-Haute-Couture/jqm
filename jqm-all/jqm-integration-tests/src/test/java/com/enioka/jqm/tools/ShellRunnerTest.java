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

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

import org.junit.Assert;
import org.junit.Test;

public class ShellRunnerTest extends JqmBaseTest
{
    @Test
    public void testDefaultShellCommandBasic()
    {
        AssumeWindows();

        CreationTools.createJobDef("test job", true, "", new HashMap<String, String>(), "set", TestHelpers.qNormal, 0, "TestApp1", null,
                "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, PathType.DEFAULTSHELLCOMMAND);
        JobRequest.create("TestApp1", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testDefaultShellCommand()
    {
        // Also tests env vars.
        String command1 = "set | grep JQM_JD_APPLICATION_NAME";
        String command2 = "set | grep JQM_XXXX"; // should fail
        if (onWindows())
        {
            command1 = "set | findstr JQM_JD_APPLICATION_NAME";
            command2 = "set | findstr JQM_XXXX";
        }

        CreationTools.createJobDef("test job", true, "", new HashMap<String, String>(), command1, TestHelpers.qNormal, 0, "TestApp1", null,
                "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, PathType.DEFAULTSHELLCOMMAND);
        CreationTools.createJobDef("failing test job", true, "", new HashMap<String, String>(), command2, TestHelpers.qNormal, 0,
                "TestApp2", null, "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, PathType.DEFAULTSHELLCOMMAND);
        JobRequest.create("TestApp1", "TestUser").submit();
        JobRequest.create("TestApp2", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(2, 20000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testDefaultShellCommandEscaping()
    {
        String command1 = "/bin/sh -c \"echo 'aa bb'\""; // this will be called within another /bin/sh!
        if (onWindows())
        {
            command1 = "cmd.exe /C \"echo 'aa bb'\"";
        }

        CreationTools.createJobDef("test job", true, "", new HashMap<String, String>(), command1, TestHelpers.qNormal, 0, "TestApp1", null,
                "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, PathType.DEFAULTSHELLCOMMAND);
        JobRequest.create("TestApp1", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testPowerShellCommand()
    {
        AssumeWindows();

        String command1 = "echo 'aa' ; echo 'bb';";

        CreationTools.createJobDef("test job", true, "", new HashMap<String, String>(), command1, TestHelpers.qNormal, 0, "TestApp1", null,
                "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, PathType.POWERSHELLCOMMAND);
        JobRequest.create("TestApp1", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testSimpleExe()
    {
        String command1 = "/bin/sh";
        Map<String, String> args = new HashMap<String, String>();
        args.put("01", "-c");
        args.put("02", "echo 'aa bb'");

        if (onWindows())
        {
            command1 = "cmd.exe";
            args.put("01", "/C");
            args.put("02", "echo aa bb");
            args.put("00", "/D"); // Note the order - should arrive first in result command
        }

        CreationTools.createJobDef("test job", true, "", args, command1, TestHelpers.qNormal, 0, "TestApp1", null, "module1", "kw1", "kw2",
                null, false, cnx, null, false, null, false, PathType.DIRECTEXECUTABLE);
        JobRequest.create("TestApp1", "TestUser").submit();

        addAndStartEngine();
        TestHelpers.waitFor(1, 20000, cnx);

        Assert.assertEquals(1, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(0, TestHelpers.getNonOkCount(cnx));
    }

    @Test
    public void testKill()
    {
        if (onWindows())
        {
            CreationTools.createJobDef("test job", true, "", new HashMap<String, String>(), "Start-Sleep 3600", TestHelpers.qNormal, 0,
                    "TestApp1", null, "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, PathType.POWERSHELLCOMMAND);
        }
        else
        {
            CreationTools.createJobDef("test job", true, "", new HashMap<String, String>(), "sleep 3600", TestHelpers.qNormal, 0,
                    "TestApp1", null, "module1", "kw1", "kw2", null, false, cnx, null, false, null, false, PathType.DEFAULTSHELLCOMMAND);
        }

        int i = JobRequest.create("TestApp1", "TestUser").submit();
        Helpers.setSingleParam("internalPollingPeriodMs", "500", cnx);

        addAndStartEngine();
        TestHelpers.waitForRunning(1, 20000, cnx);
        JqmClientFactory.getClient().killJob(i);
        TestHelpers.waitFor(1, 20000, cnx);

        Assert.assertEquals(0, TestHelpers.getOkCount(cnx));
        Assert.assertEquals(1, TestHelpers.getNonOkCount(cnx));
    }
}
