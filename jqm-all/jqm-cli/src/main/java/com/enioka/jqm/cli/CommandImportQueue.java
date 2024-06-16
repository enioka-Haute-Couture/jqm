package com.enioka.jqm.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.xml.XmlQueueParser;

@Parameters(commandNames = "Import-Queue", commandDescription = "Import one or multiple queue mapping files.")
class CommandImportQueue extends CommandBase
{
    @Parameter(names = { "-f",
            "--file" }, description = "Queue mapping XML file.", required = true, validateWith = ValidatorFileCanRead.class)
    private String xmlPath;

    @Override
    public int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            XmlQueueParser.parse(xmlPath, cnx);
            return 0;
        }
        catch (Exception ex)
        {
            jqmlogger.error("Could not parse and import the file", ex);
            return 107;
        }
    }
}
