package com.enioka.jqm.cli;

import com.beust.jcommander.Parameters;
import com.enioka.admin.MetaService;
import com.enioka.api.admin.RRoleDto;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;

@Parameters(commandNames = "Get-Role", commandDescription = "Fetch all roles defined in central configuration.")
class CommandGetRole extends CommandBase
{
    @Override
    public int doWork()
    {
        jqmlogger.info("# Roles defined");
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            for (RRoleDto role : MetaService.getRoles(cnx))
            {
                jqmlogger.info("{} - {}", role.getName(), role.getDescription());
            }
        }
        return 0;
    }
}
