package com.enioka.jqm.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.admin.MetaService;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.repository.UserManagementRepository;

@Parameters(commandNames = "Reset-Root", commandDescription = "Create or reset the 'root' account password and permissions.")
class CommandResetRoot extends CommandBase
{
    @Parameter(names = { "-p", "--password" }, description = "New password.", required = true)
    private String password;

    @Override
    public int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            UserManagementRepository.createRoleIfMissing(cnx, "administrator", "all permissions without exception", "*:*");
            UserManagementRepository.createUserIfMissing(cnx, "root", password, "all powerful user", "administrator");
            MetaService.changeUserPassword(cnx, "root", password);
            cnx.commit();
            return 0;
        }
        catch (Exception ex)
        {
            jqmlogger.error("Could not reset the root account", ex);
            return 108;
        }
    }
}
