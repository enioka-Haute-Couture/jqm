package com.enioka.jqm.runner.shell;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.model.JobDef.PathType;

class KillHelpers
{
    private static Logger jqmlogger = LoggerFactory.getLogger(KillHelpers.class);

    private static Boolean java9Plus = null;
    private static Method java9PlusPidMethod = null;

    private static Boolean processWithPid = null;
    private static Field processPidField = null;

    private KillHelpers()
    {}

    private static boolean onWindows()
    {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    static void kill(Process process)
    {
        if (onWindows())
        {
            killByPidWindows(process.pid());
        }
        else
        {
            killByPidUnix(process.pid());
        }
    }

    private static void killByPidGeneric(long pid, String command)
    {
        List<String> args = OsHelpers.getProcessArguments(command, new HashMap<>(), PathType.DEFAULTSHELLCOMMAND);
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process;
        try
        {
            jqmlogger.debug("Starting process for killing process tree under PID " + pid + " - arguments: " + args.toString());
            process = pb.start();
        }
        catch (IOException e)
        {
            jqmlogger.error("Could not launch a shell kill process", e);
            return;
        }

        int res;
        try
        {
            res = process.waitFor();
        }
        catch (InterruptedException e)
        {
            // We do not care.
            return;
        }

        if (res != 0)
        {
            jqmlogger.error("Could not kill process");
        }
        // Note: we do not fetch the outputs of the command, so debugging can be hard. But it would be costly and should always be under
        // buffer size.
    }

    private static void killByPidWindows(long pid)
    {
        // F is compulsory under Windows when dealing with trees.
        killByPidGeneric(pid, "taskkill /PID " + pid + " /T /F");
    }

    private static void killByPidUnix(long pid)
    {
        // Negative PID means process group. Cannot use this on all Unixes sadly.
        killByPidGeneric(pid, "pstree -p pid | sed -e 's/[^0-9]/ /g' -e  's/ \\+/ /g' -e 's/^ //g' -e 's/ $//g' -e 's/ /\\n/g' | xargs kill"
                .replace("pid", "" + pid));
    }
}
