package com.enioka.jqm.cli;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.enioka.admin.MetaService;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.repository.UserManagementRepository;

@Parameters(commandNames = "Reset-User", commandDescription = "Create or reset a user account.")
class CommandResetUser extends CommandBase
{
    @Parameter(names = { "-r", "--roles" }, description = "Roles of the user.", required = true, variableArity = true)
    private List<String> roles = new ArrayList<>();

    @Parameter(names = { "l", "--login" }, description = "User login.", required = true)
    private String login;

    @Parameter(names = { "-p", "--password" }, description = "User password.", required = true)
    private String password;

    @Override
    public int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            UserManagementRepository.createUserIfMissing(cnx, login, password, "created through CLI",
                    roles.toArray(new String[roles.size()]));
            MetaService.changeUserPassword(cnx, login, password);
            cnx.commit();
            return 0;
        }
    }
}
