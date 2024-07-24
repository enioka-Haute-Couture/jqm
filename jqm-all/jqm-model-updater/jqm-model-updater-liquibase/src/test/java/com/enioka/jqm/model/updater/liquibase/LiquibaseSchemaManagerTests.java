package com.enioka.jqm.model.updater.liquibase;

import java.sql.SQLException;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.Scope;
import liquibase.command.CommandFactory;

public class LiquibaseSchemaManagerTests
{
    private static Logger jqmlogger = LoggerFactory.getLogger(LiquibaseSchemaManagerTests.class);

    /**
     * This test is a helper displaying how to use which commands with Liquibase embedded.
     */
    @Test
    public void testListCommands()
    {
        var commandFactory = Scope.getCurrentScope().getSingleton(CommandFactory.class);
        var commands = commandFactory.getCommands(false);

        for (var command : commands)
        {
            jqmlogger.info("Command detected: {}", command.getName()[0]);
            if (command.getShortDescription() != null)
            {
                jqmlogger.info("\t{}", command.getShortDescription());
            }
            if (command.getLongDescription() != null)
            {
                jqmlogger.info("\t{}", command.getLongDescription());
            }

            jqmlogger.info("\tArguments:");
            for (var arg : command.getArguments().entrySet())
            {
                jqmlogger.info("\t\t{} - {} - default {} ({})", arg.getKey(), arg.getValue().getDescription(),
                        arg.getValue().getDefaultValue(), arg.getValue().getDefaultValueDescription());
            }
            jqmlogger.info("");
        }
    }

    @Test
    public void testUpdate() throws SQLException
    {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:testdbengine");

        var liquibaseHelper = new LiquibaseSchemaManager();

        // Just get the SQL
        var sql = liquibaseHelper.getUpdateSchemaSql(ds.getConnection());

        // Check status
        Assert.assertFalse(liquibaseHelper.isUpToDate(ds.getConnection()));

        // Apply it
        var changeSetCount1 = liquibaseHelper.updateSchema(ds.getConnection());

        // Check status
        Assert.assertTrue(liquibaseHelper.isUpToDate(ds.getConnection()));

        // Check it is idempotent
        jqmlogger.info("SECOND RUN");
        var changeSetCount2 = liquibaseHelper.updateSchema(ds.getConnection());

        Assert.assertTrue(sql.contains("CREATE TABLE PUBLIC.NODE"));
        Assert.assertEquals(1, changeSetCount1);
        Assert.assertEquals(0, changeSetCount2);

        // Is it possible to use a newly created table?
        var cnx = ds.getConnection();
        var s = cnx.createStatement();
        var rs = s.executeQuery("INSERT INTO NODE(ID, NAME, DNS, PORT, LOAD_API_ADMIN, ENABLED, REPO_DELIVERABLE, REPO_JOB_DEF, STOP) "
                + "VALUES(12, 'test', 'localhost', 1234, true, true, 'test', 'test', false)");
        cnx.commit();
        rs.close();

        rs = s.executeQuery("select count(1) from node");
        Assert.assertTrue(rs.next());
        Assert.assertEquals(1, rs.getInt(1));
    }
}
