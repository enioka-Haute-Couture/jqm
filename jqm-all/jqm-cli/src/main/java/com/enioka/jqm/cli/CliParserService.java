package com.enioka.jqm.cli;

import java.util.ServiceLoader;

import com.enioka.jqm.cli.api.CommandBase;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.enioka.jqm.cli.bootstrap.CommandLine;
import com.enioka.jqm.jndi.api.JqmJndiContextControlService;
import com.enioka.jqm.model.updater.cli.DbUpdateVerb;
import com.enioka.jqm.shared.services.ServiceLoaderHelper;

/**
 * Main entry point of the CLI (which is also the entry point for the daemon).
 */
@MetaInfServices(CommandLine.class)
public class CliParserService implements CommandLine
{
    private static final Logger jqmlogger = (Logger) LoggerFactory.getLogger(CliParserService.class);

    @Override
    public int runServiceCommand(String[] args)
    {
        return CliParserService.runCommand(args);
    }

    @Override
    public void stopIfRunning()
    {
        if (CommandStartNode.engineNode != null)
        {
            CommandStartNode.engineNode.stop();
        }
    }

    public static int runCommand(String[] args)
    {
        // JNDI registration - most commands need a JNDI context.
        ServiceLoaderHelper.getService(ServiceLoader.load(JqmJndiContextControlService.class)).registerIfNeeded();

        // Get commands registred as services
        var additionalCommands = ServiceLoaderHelper.getServices(ServiceLoader.load(CommandBase.class));

        // Create parser
        var jcBuilder = JCommander.newBuilder().addCommand(new CommandExportJobDef()).addCommand(new CommandExportQueue())
                .addCommand(new CommandGetEngineVersion()).addCommand(new CommandGetJiStatus()).addCommand(new CommandGetNodeCount())
                .addCommand(new CommandGetRole()).addCommand(new CommandImportClusterConfiguration()).addCommand(new CommandImportJobDef())
                .addCommand(new CommandImportQueue()).addCommand(new CommandInstallNodeTemplate()).addCommand(new CommandNewJi())
                .addCommand(new CommandNewNode()).addCommand(new CommandResetRoot()).addCommand(new CommandResetUser())
                .addCommand(new CommandSetWebConfiguration()).addCommand(new CommandStartNode()).addCommand(new CommandStartSingle());

        for (var command : additionalCommands)
        {
            jcBuilder.addCommand(command);
        }
        var jc = jcBuilder.build();

        jc.setColumnSize(160);
        jc.setCaseSensitiveOptions(false);

        if (args == null || args.length == 0)
        {
            jc.usage();
            return 1;
        }

        // We do not bother with a non-command parsing for global help.
        if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help") || args[0].equals("-?") || args[0].equals("/?")))
        {
            jc.usage();
            return 0;
        }

        // Parse command
        jc.parse(args);
        CommandBase command = (CommandBase) jc.getCommands().get(jc.getParsedCommand()).getObjects().get(0);

        if (command.isHelp())
        {
            jc.usage();
            return 0;
        }

        // It is possible to actually switch from one environment to another by setting a different 'settings' file.
        // (and therefore a different datasource)
        if (command.getSettingsFile() != null)
        {
            jqmlogger.info("Using alternative settings file {}", command.getSettingsFile());
            System.setProperty("com.enioka.jqm.resourceFiles", command.getSettingsFile());
        }

        // Go.
        return command.doWork();
    }
}
