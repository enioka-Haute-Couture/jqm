package com.enioka.jqm.cli.api;

import com.beust.jcommander.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all CLI commands. Command naming must follow powershell cmdlet conventions (see
 * https://docs.microsoft.com/en-us/powershell/developer/cmdlet/approved-verbs-for-windows-powershell-commands)
 */
public abstract class CommandBase
{
    protected static Logger jqmlogger = LoggerFactory.getLogger(CommandBase.class);

    @Parameter(names = { "-s",
            "--settings" }, description = "Path (inside JQM_ROOT/conf/) to JQM properties file. Default is resources.xml.", required = false)
    protected String settingsFile;

    @Parameter(names = { "-h", "--help", "/?", "-?" }, description = "Display command help.", help = true)
    protected boolean help = false;

    /**
     * Actually run the command.
     */
    public abstract int doWork();

    public String getSettingsFile()
    {
        return settingsFile;
    }

    public boolean isHelp()
    {
        return help;
    }
}
