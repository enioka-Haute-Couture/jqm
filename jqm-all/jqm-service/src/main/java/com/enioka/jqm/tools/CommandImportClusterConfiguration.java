package com.enioka.jqm.tools;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.jdbc.DbConn;

@Parameters(commandNames = "Import-ClusterConfiguration", commandDescription = "Import cluster configuration, i.e. everything concerning infrastructure.")
class CommandImportClusterConfiguration extends CommandBase
{
    @Parameter(names = { "-f",
            "--file" }, description = "Configuration XML file.", required = true, validateWith = ValidatorFileCanRead.class)
    private String xmlPath;

    @Override
    int doWork()
    {
        try (DbConn cnx = Helpers.getNewDbSession())
        {
            XmlConfigurationParser.parse(xmlPath, cnx);
            return 0;
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not import file", e);
            return 110;
        }
    }
}
