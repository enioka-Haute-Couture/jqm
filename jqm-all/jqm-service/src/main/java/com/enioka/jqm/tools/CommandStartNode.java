package com.enioka.jqm.tools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = "Start-Node", commandDescription = "Start an existing node identified by name, waiting for CTRL-C to end.")
class CommandStartNode extends CommandBase
{
    @Parameter(names = { "-n", "--node-name" }, description = "Name of the node to start.", required = true)
    private String nodeName;

    @Override
    int doWork()
    {
        try
        {
            JqmEngine engine = new JqmEngine();
            engine.start(nodeName, new EngineCallback());
            engine.join();
            return 0;
        }
        catch (JqmRuntimeException e)
        {
            jqmlogger.error("Error running engine");
            return 111;
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not launch the engine named " + nodeName
                    + ". This may be because no node with this name was declared (with command line option createnode).", e);
            throw new JqmRuntimeException("Could not start the engine", e);
        }
    }
}
