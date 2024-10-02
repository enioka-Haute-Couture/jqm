package com.enioka.jqm.cli;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.enioka.jqm.cli.api.CommandBase;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.GlobalParameter;

@Parameters(commandNames = "Set-WebConfiguration", commandDescription = "Changes the way JQM exposes its web services and GUI.")
class CommandSetWebConfiguration extends CommandBase
{
    @Parameter(names = { "-c",
            "--change" }, description = "(ENABLE_HTTP_GUI|DISABLE_ALL|ENABLE_TLS|DISABLE_TLS|ENABLE_INTERNAL_PKI|DISABLE_INTERNAL_PKI|ENABLE_AUTHENTICATION|DISABLE_AUTHENTICATION)", required = true, converter = EnumConverter.class)
    private Action action;

    @Override
    public int doWork()
    {
        try (DbConn cnx = DbManager.getDb().getConn())
        {
            switch (action)
            {
            case ENABLE_HTTP_GUI:
                GlobalParameter.setParameter(cnx, "disableWsApi", "false");
                GlobalParameter.setParameter(cnx, "enableWsApiSsl", "false");
                GlobalParameter.setParameter(cnx, "enableWsApiAuth", "true");
                GlobalParameter.setParameter(cnx, "disableWsApiSimple", "false");
                GlobalParameter.setParameter(cnx, "disableWsApiClient", "false");
                GlobalParameter.setParameter(cnx, "disableWsApiAdmin", "false");
                GlobalParameter.setParameter(cnx, "enableInternalPki", "true");

                cnx.runUpdate("node_update_all_enable_ws");
                break;
            case DISABLE_ALL:
                GlobalParameter.setParameter(cnx, "disableWsApi", "true");
                cnx.runUpdate("node_update_all_disable_ws");
                break;
            case ENABLE_TLS:
                GlobalParameter.setParameter(cnx, "enableWsApiSsl", "true");
                break;
            case DISABLE_TLS:
                GlobalParameter.setParameter(cnx, "enableWsApiSsl", "false");
                break;
            case ENABLE_INTERNAL_PKI:
                GlobalParameter.setParameter(cnx, "enableInternalPki", "true");
                break;
            case DISABLE_INTERNAL_PKI:
                GlobalParameter.setParameter(cnx, "enableInternalPki", "false");
                break;
            case ENABLE_AUTHENTICATION:
                GlobalParameter.setParameter(cnx, "enableWsApiAuth", "true");
                break;
            case DISABLE_AUTHENTICATION:
                GlobalParameter.setParameter(cnx, "enableWsApiAuth", "false");
                break;
            }
            cnx.commit();
        }
        return 0;
    }

    enum Action {
        /**
         * Changes all configuration options needed to enable the web UI on HTTP (not HTTPS).
         */
        ENABLE_HTTP_GUI,
        /**
         * Disable everything web related - JQM will no open any socket for web-related operations.
         */
        DISABLE_ALL,
        /**
         * Ask JQM to use HTTP and not HTTPS on all its web operations.
         */
        ENABLE_TLS,
        /**
         * Ask JQM to use HTTPS and not HTTP on all its web operations.
         */
        DISABLE_TLS,
        /**
         * Use a JQM-provided PKI for all certificate needs.
         */
        ENABLE_INTERNAL_PKI,
        /**
         * Use an external PKI for all certificate needs. It must be configured if using TLS.
         */
        DISABLE_INTERNAL_PKI, ENABLE_AUTHENTICATION, DISABLE_AUTHENTICATION
    }
}

class EnumConverter implements IStringConverter<CommandSetWebConfiguration.Action>
{
    @Override
    public CommandSetWebConfiguration.Action convert(String value)
    {
        CommandSetWebConfiguration.Action convertedValue = CommandSetWebConfiguration.Action.valueOf(value);

        if (convertedValue == null)
        {
            throw new ParameterException("Value " + value + "can not be converted to Action. ");
        }
        return convertedValue;
    }
}
