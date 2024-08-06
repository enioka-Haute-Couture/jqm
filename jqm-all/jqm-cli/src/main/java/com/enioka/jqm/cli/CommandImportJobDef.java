package com.enioka.jqm.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.xml.XmlJobDefParser;

import org.apache.commons.io.FileUtils;

@Parameters(commandNames = "Import-JobDef", commandDescription = "Import one or multiple deployment descriptors.")
class CommandImportJobDef extends CommandBase
{
    @Parameter(names = { "-f",
            "--files" }, description = "Deployment descriptor XML file, or files, or directory.", required = true, variableArity = true, validateWith = ValidatorFileCanRead.class)
    private List<String> xmlPathes = new ArrayList<>();

    @Override
    public int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            try
            {
                cnx.runSelectSingle("q_select_default", Long.class);
            }
            catch (NoResultException e)
            {
                jqmlogger.error(
                        "Cannot import a Job Definition when there is no default queue defined. To solve, creating an engine will recreate it.");
                return 103;
            }

            // The parameter is a list of deployment descriptor files OR of directory which may contain descriptor files (XML).
            List<String> expandedPathes = new ArrayList<>();
            for (String path : xmlPathes)
            {
                File f = new File(path);

                if (f.isDirectory())
                {
                    for (File xmlFile : FileUtils.listFiles(f, new String[] { "xml" }, true))
                    {
                        expandedPathes.add(xmlFile.getPath());
                    }
                }
                else
                {
                    expandedPathes.add(path);
                }
            }

            // Parse each file.
            for (String path : expandedPathes)
            {
                XmlJobDefParser.parse(path, cnx);
            }
            return 0;
        }
        catch (Exception e)
        {
            jqmlogger.error("Could not import file", e);
            return 104;
        }
    }
}
