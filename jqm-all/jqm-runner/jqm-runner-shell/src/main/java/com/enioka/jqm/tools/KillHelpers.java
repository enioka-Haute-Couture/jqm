package com.enioka.jqm.tools;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import com.enioka.jqm.model.JobDef.PathType;

import org.jvnet.winp.WinProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        //////////////////////////////////
        // Preparation

        // Check if we are on Java 9+, which has a new pid() method allowing us to easily kill the process tree.
        if (java9Plus == null)
        {
            try
            {
                java9PlusPidMethod = Process.class.getMethod("pid");
                java9Plus = true;
                jqmlogger.debug("Using Java9+ Process.pid() public method to fetch PID");
            }
            catch (NoSuchMethodException ex)
            {
                jqmlogger.info("Running on Java < 9. Old kill methods will be used");
                java9Plus = false;
            }
        }

        // Check if we can access the old fashioned Process.pid hidden field on Unix.
        if (processWithPid == null && !java9Plus && !onWindows())
        {
            try
            {
                processPidField = process.getClass().getDeclaredField("pid");
                processPidField.setAccessible(true);
                processWithPid = true;
                jqmlogger.debug("Using Unix Process.pid private field to fetch PID");
            }
            catch (Exception e)
            {
                processWithPid = false;
                jqmlogger.debug("Cannot access Unix Process.pid field", e);
            }
        }

        //////////////////////////////////
        // Try kill methods one by one

        // Kill by PID, recent Java version
        long pid;
        if (java9PlusPidMethod != null)
        {
            try
            {
                pid = (Long) java9PlusPidMethod.invoke(process);
            }
            catch (Exception e)
            {
                jqmlogger.error("Could not get PID from Process method. Kill order is ignored.", e);
                return;
            }
            killByPid(pid);
            return;
        }

        // Kill by native API, old version, Windows.
        if (onWindows())
        {
            WinProcess wp = new WinProcess(process);
            wp.killRecursively();
            // killByPid(wp.getPid()); // would work.
            return;
        }

        // Kill by PID, old version, Unix.
        if (processPidField != null)
        {
            try
            {
                pid = processPidField.getLong(process);
            }
            catch (Exception e)
            {
                jqmlogger.error("Could not get PID from Process.pid field. Kill order is ignored.", e);
                return;
            }
            killByPid(pid);
            return;
        }

        // If here, that's a problem.
        jqmlogger.error("Could not find a way to kill the process tree");
    }

    private static void killByPid(long pid)
    {
        if (onWindows())
        {
            killByPidWindows(pid);
        }
        else
        {
            killByPidUnix(pid);
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
