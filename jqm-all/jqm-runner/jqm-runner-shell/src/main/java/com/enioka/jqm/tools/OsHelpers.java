package com.enioka.jqm.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.api.JobRunnerException;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.JobDef.PathType;

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

    private static List<String> getSh(JobInstance ji)
    {
        List<String> res = new ArrayList<String>(10);
        res.add("/bin/sh");
        res.add("-c");
        addAllParametersAsSingleString(res, ji);
        return res;
    }

    private static List<String> getCmdShell(JobInstance ji)
    {
        List<String> res = new ArrayList<String>(10);
        res.add("cmd.exe");
        res.add("/C");
        addAllParametersAsSingleString(res, ji);
        return res;
    }

    private static List<String> getPowerShell(JobInstance ji)
    {
        List<String> res = new ArrayList<String>(10);
        res.add("powershell.exe");
        res.add("-NoLogo");
        res.add("-NonInteractive");
        res.add("-WindowStyle");
        res.add("Hidden");
        res.add("-Command");
        addAllParametersAsSingleString(res, ji);
        return res;
    }

    private static List<String> getDefaultShell(JobInstance ji)
    {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows"))
        {
            return getCmdShell(ji);
        }
        else
        {
            return getSh(ji);
        }
    }

    /**
     * When using /bin/sh -c "..." there is a single argument to the process. This method builds it.<br>
     * Note we encourange users through the GUI to only specify the whole shell command in a single field. Only XML imports may result in
     * multiple arguments.
     * 
     * @param prms
     * @param ji
     */
    private static void addAllParametersAsSingleString(List<String> prms, JobInstance ji)
    {
        if (prms.isEmpty())
        {
            return;
        }

        List<String> raw = new ArrayList<String>(ji.getPrms().size() * 2);

        // Command itself
        raw.add(ji.getJD().getJarPath());

        // Parameters, ordered by key
        List<String> keys = new ArrayList<String>(ji.getPrms().keySet());
        Collections.sort(keys, prmComparator);
        for (String p : keys)
        {
            if (!ji.getPrms().get(p).trim().isEmpty())
            {
                raw.add(ji.getPrms().get(p).trim());
            }
        }

        if (!prms.isEmpty())
        {
            prms.add(String.join(" ", raw));
        }
    }

    /**
     * The most common case: start a process, no need for a shell.
     * 
     * @param ji
     * @return
     */
    private static List<String> getSimpleProcess(JobInstance ji)
    {
        List<String> res = new ArrayList<String>(10);

        // Process itself
        res.add(ji.getJD().getJarPath());

        // Optional parameters - keys are just indices to sort the values.
        if (ji.getPrms().isEmpty())
        {
            return res;
        }

        List<String> keys = new ArrayList<String>(ji.getPrms().keySet());
        Collections.sort(keys, prmComparator);
        for (String p : keys)
        {
            if (!ji.getPrms().get(p).trim().isEmpty())
            {
                res.add(ji.getPrms().get(p).trim());
            }
        }

        // Done
        return res;
    }

    static List<String> getProcessArguments(JobInstance ji)
    {
        switch (ji.getJD().getPathType())
        {
        case DEFAULTSHELLCOMMAND:
            return getDefaultShell(ji);
        case POWERSHELLCOMMAND:
            return getPowerShell(ji);
        case DIRECTEXECUTABLE:
            return getSimpleProcess(ji);
        default:
            throw new JobRunnerException("Unsupported path type " + ji.getJD().getPathType());
        }
    }
}