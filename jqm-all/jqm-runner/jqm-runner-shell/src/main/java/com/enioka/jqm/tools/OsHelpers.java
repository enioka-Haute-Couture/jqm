package com.enioka.jqm.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.api.JobRunnerException;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.JobDef.PathType;

import org.apache.commons.lang.StringUtils;

final class OsHelpers
{
    private static Comparator<String> prmComparator = new Comparator<String>()
    {
        @Override
        public int compare(String o1, String o2)
        {
            return o1.compareTo(o2);
        }
    };

    private OsHelpers()
    {}

    private static List<String> getSh(String commandLine, Map<String, String> prms)
    {
        List<String> res = new ArrayList<>(10);
        res.add("/bin/sh");
        res.add("-c");
        addAllParametersAsSingleString(res, commandLine, prms);
        return res;
    }

    private static List<String> getCmdShell(String commandLine, Map<String, String> prms)
    {
        List<String> res = new ArrayList<>(10);
        res.add("cmd.exe");
        res.add("/C");
        addAllParametersAsSingleString(res, commandLine, prms);
        return res;
    }

    private static List<String> getPowerShell(String commandLine, Map<String, String> prms)
    {
        List<String> res = new ArrayList<>(10);
        res.add("powershell");
        res.add("-NoLogo");
        res.add("-NonInteractive");
        res.add("-WindowStyle");
        res.add("Hidden");
        res.add("-Command");
        addAllParametersAsSingleString(res, commandLine, prms);
        return res;
    }

    private static List<String> getDefaultShell(String commandLine, Map<String, String> prms)
    {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows"))
        {
            return getCmdShell(commandLine, prms);
        }
        else
        {
            return getSh(commandLine, prms);
        }
    }

    /**
     * When using /bin/sh -c "..." there is a single argument to the process. This method builds it.<br>
     * Note we encourange users through the GUI to only specify the whole shell command in a single field. Only XML imports may result in
     * multiple arguments.
     * 
     * @param resultList
     * @param ji
     */
    private static void addAllParametersAsSingleString(List<String> resultList, String commandLine, Map<String, String> prms)
    {
        List<String> raw = new ArrayList<>(prms.size() * 2);

        // Command itself
        raw.add(commandLine);

        // Parameters, ordered by key
        List<String> keys = new ArrayList<>(prms.keySet());
        Collections.sort(keys, prmComparator);
        for (String p : keys)
        {
            if (!prms.get(p).trim().isEmpty())
            {
                raw.add(prms.get(p).trim());
            }
        }

        if (!raw.isEmpty())
        {
            resultList.add(StringUtils.join(raw, " "));
        }
    }

    /**
     * The most common case: start a process, no need for a shell.
     * 
     * @param ji
     * @return
     */
    private static List<String> getSimpleProcess(String processPath, Map<String, String> prms)
    {
        List<String> res = new ArrayList<>(10);

        // Process itself
        res.add(processPath);

        // Optional parameters - keys are just indices to sort the values.
        if (prms.isEmpty())
        {
            return res;
        }

        List<String> keys = new ArrayList<>(prms.keySet());
        Collections.sort(keys, prmComparator);
        for (String p : keys)
        {
            if (!prms.get(p).trim().isEmpty())
            {
                res.add(prms.get(p).trim());
            }
        }

        // Done
        return res;
    }

    static List<String> getProcessArguments(JobInstance ji)
    {
        return getProcessArguments(ji.getJD().getJarPath(), ji.getPrms(), ji.getJD().getPathType());
    }

    static List<String> getProcessArguments(String commandLine, Map<String, String> prms, PathType pathType)
    {
        switch (pathType)
        {
        case DEFAULTSHELLCOMMAND:
            return getDefaultShell(commandLine, prms);
        case POWERSHELLCOMMAND:
            return getPowerShell(commandLine, prms);
        case DIRECTEXECUTABLE:
            return getSimpleProcess(commandLine, prms);
        default:
            throw new JobRunnerException("Unsupported path type " + pathType);
        }
    }
}