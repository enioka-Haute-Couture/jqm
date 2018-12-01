/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

import com.beust.jcommander.JCommander;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Starter class & parameter parsing
 *
 */
public class Main
{
    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static Logger jqmlogger = LoggerFactory.getLogger(Main.class);

    private Main()
    {
        // Static class
    }

    /**
     * Windows service entry point for service start
     *
     * @param args
     */
    static void start(String[] args)
    {
        jqmlogger.info("Service start");
        main(args);
    }

    /**
     * Windows service entry point for service stop
     *
     * @param args
     */
    static void stop(String[] args)
    {
        jqmlogger.info("Service stop");
        CommandStartNode.engine.stop();
    }

    /**
     * Startup method for the packaged JAR
     *
     * @param args
     *                 0 is node name
     */
    public static void main(String[] args)
    {
        System.exit(runCommand(args));
    }

    public static int runCommand(String[] args)
    {
        CommonService.setLogFileName("cli");

        // Create parser
        JCommander jc = JCommander.newBuilder().addCommand(new CommandExportJobDef()).addCommand(new CommandExportQueue())
                .addCommand(new CommandGetEngineVersion()).addCommand(new CommandGetJiStatus()).addCommand(new CommandGetNodeCount())
                .addCommand(new CommandGetRole()).addCommand(new CommandImportClusterConfiguration()).addCommand(new CommandImportJobDef())
                .addCommand(new CommandImportQueue()).addCommand(new CommandInstallNodeTemplate()).addCommand(new CommandNewJi())
                .addCommand(new CommandNewNode()).addCommand(new CommandResetRoot()).addCommand(new CommandResetUser())
                .addCommand(new CommandSetWebConfiguration()).addCommand(new CommandStartNode()).addCommand(new CommandStartSingle())
                .addCommand(new CommandUpdateSchema()).build();
        jc.setColumnSize(160);
        jc.setCaseSensitiveOptions(false);

        // We do not bother with a non-command parsing for global help.
        if (args.length == 1 && (args[0].equals("-h") || args[0].equals("--help") || args[0].equals("-?") || args[0].equals("/?")))
        {
            jc.usage();
            return 0;
        }

        // Parse command
        jc.parse(args);
        CommandBase command = (CommandBase) jc.getCommands().get(jc.getParsedCommand()).getObjects().get(0);

        if (command.help)
        {
            jc.usage(jc.getParsedCommand());
            return 0;
        }

        // It is possible to actually switch from one environment to another by seting a different settings file.
        // (and therefore a different datasource)
        if (command.settingsFile != null)
        {
            jqmlogger.info("Using alternative settings file {}", command.settingsFile);
            Helpers.resourceFile = command.settingsFile;
        }

        // Connection
        Helpers.registerJndiIfNeeded();

        // Go.
        return command.doWork();
    }
}
