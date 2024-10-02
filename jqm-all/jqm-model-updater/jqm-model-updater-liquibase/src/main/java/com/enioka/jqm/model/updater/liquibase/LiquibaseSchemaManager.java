package com.enioka.jqm.model.updater.liquibase;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.model.updater.DbSchemaManager;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.StatusCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.report.UpdateReportParameters;
import liquibase.resource.ClassLoaderResourceAccessor;

/**
 * Encapsulates the logic to update the database schema using Liquibase.
 */
@MetaInfServices(DbSchemaManager.class)
public class LiquibaseSchemaManager implements DbSchemaManager
{
    private static Logger jqmlogger = LoggerFactory.getLogger(LiquibaseSchemaManager.class);
    private static final String LIQUIBASE_CHANGELOG = "/liquibase-xml/0-changelog-root.xml";

    @Override
    public int updateSchema(Connection connection)
    {
        jqmlogger.debug("Applying liquibase changes");
        return updateLiquibaseCommand(connection, "update", report -> report.getChangesetInfo().getChangesetCount());
    }

    @Override
    public String getUpdateSchemaSql(Connection connection)
    {
        jqmlogger.debug("Getting liquibase changes in SQL form");
        return updateLiquibaseCommand(connection, "updateSql", report -> report.getChangesetInfo().getChangesetInfoList().stream()
                .flatMap(cgil -> cgil.getGeneratedSql().stream()).collect(Collectors.joining("\n ")));
    }

    private <T> T updateLiquibaseCommand(Connection connection, String commandName, Function<UpdateReportParameters, T> resultMapper)
    {
        try (var db =  getLiquibaseDb(connection))
        {
            var commandScope = new CommandScope(commandName) //
                    .addArgumentValue("database", db) //
                    .addArgumentValue("changelogFile", LIQUIBASE_CHANGELOG);


            var result = commandScope.execute();

            var report = (UpdateReportParameters) result.getResult("updateReport");

            if (!report.getSuccess())
            {
                throw new RuntimeException("Liquibase update failed");
            }
            jqmlogger.info("Database schema is now up to date");
            return resultMapper.apply(report);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not execute liquibase update", e);
        }
    }

    private Database getLiquibaseDb(Connection connection)
    {
        try
        {
            return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        }
        catch (DatabaseException e)
        {
            throw new RuntimeException("Could not create liquibase database", e);
        }
    }

    public boolean isUpToDate(Connection connection)
    {
        try (var db =  getLiquibaseDb(connection))
        {
            var commandScope = new CommandScope("status") //
                    .addArgumentValue("database", db).addArgumentValue("changelogFile", LIQUIBASE_CHANGELOG);

            commandScope.execute();
            var u = (StatusCommandStep) commandScope.getCommand().getPipeline().get(commandScope.getCommand().getPipeline().size() - 1);
            List<ChangeSet> changeSets = new ArrayList<>();
            try
            {
                changeSets = u.listUnrunChangeSets(null, null, (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class),
                        (Database) commandScope.getDependency(Database.class));
            }
            catch (Exception e)
            {
                throw new RuntimeException("Could not list unrun changesets", e);
            }
            return changeSets.isEmpty();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not list unrun changesets", e);
        }
    }
}
