package com.enioka.jqm.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.xml.XmlQueueExporter;

@Parameters(commandNames = "Export-Queue", commandDescription = "Export all queue definitions inside central configuration as a single file.")
class CommandExportQueue extends CommandBase
{
    @Parameter(names = { "-f", "--file" }, description = "Name of the new file.", required = true)
    private String xmlPath;

    @Override
    public int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            XmlQueueExporter.export(xmlPath, cnx);
            return 0;
        }
        catch (Exception ex)
        {
            jqmlogger.error("Could not create the export file", ex);
            return 106;
        }
    }
}
