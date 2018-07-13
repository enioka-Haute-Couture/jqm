/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.enioka.admin.MetaService;
import com.enioka.api.admin.NodeDto;
import com.enioka.api.admin.QueueMappingDto;
import com.enioka.jqm.api.JobInstance;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.jdbc.Db;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;

/**
 * Starter class & parameter parsing
 * 
 */
public class Main
{
    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private static Logger jqmlogger = Logger.getLogger(Main.class);
    private static JqmEngine engine;

    private Main()
    {
        // Static class
    }

    /**
     * Windows service entry point for service start
     * 
     * @param args
     */
    static void start(String[] args)
    {
        jqmlogger.info("Service start");
        main(args);
    }

    /**
     * Windows service entry point for service stop
     * 
     * @param args
     */
    static void stop(String[] args)
    {
        jqmlogger.info("Service stop");
        engine.stop();
    }

    /**
     * Startup method for the packaged JAR
     * 
     * @param args
     *            0 is node name
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args)
    {
        CommonService.setLogFileName("cli");
        Option o00 = OptionBuilder.withArgName("nodeName").hasArg().withDescription("name of the JQM node to start").isRequired()
                .create("startnode");
        Option o01 = OptionBuilder.withDescription("display help").withLongOpt("help").create("h");
        Option o11 = OptionBuilder.withArgName("applicationname").hasArg().withDescription("name of the application to launch").isRequired()
                .create("enqueue");
        Option o21 = OptionBuilder.withArgName("xmlpath").hasArg().withDescription("path of the XML configuration file to import")
                .isRequired().create("importjobdef");
        Option o22 = OptionBuilder.withArgName("xmlpath").hasArg().withDescription("Export all jobs definition into an XML file")
                .isRequired().create("exportjobdef");
        Option o31 = OptionBuilder.withArgName("xmlpath").hasArg().withDescription("export all queue definitions into an XML file")
                .isRequired().create("exportallqueues");
        OptionBuilder.withArgName("xmlpath").hasArg().withDescription("export some queue definitions into an XML file").isRequired()
                .create("exportqueuefile");
        OptionBuilder.withArgName("queues").hasArg().withDescription("queues to export").withValueSeparator(',').isRequired()
                .create("queue");
        Option o51 = OptionBuilder.withArgName("xmlpath").hasArg().withDescription("import all queue definitions from an XML file")
                .isRequired().create("importqueuefile");
        Option o61 = OptionBuilder.withArgName("nodeName").hasArg()
                .withDescription("creates a JQM node of this name, or updates it if it exists. Implies -u.").isRequired()
                .create("createnode");
        Option o62 = OptionBuilder.withArgName("port").hasArg().withDescription("Specify the port used by the newly created node.")
                .isRequired().create("port");
        Option o71 = OptionBuilder.withDescription("display JQM engine version").withLongOpt("version").create("v");
        Option o81 = OptionBuilder.withDescription("upgrade JQM database").withLongOpt("upgrade").create("u");
        Option o91 = OptionBuilder.withArgName("jobInstanceId").hasArg().withDescription("get job instance status by ID").isRequired()
                .withLongOpt("getstatus").create("g");
        Option o101 = OptionBuilder.withArgName("password").hasArg().withDescription("creates or resets root admin account password")
                .isRequired().withLongOpt("root").create("r");
        Option o111 = OptionBuilder.withArgName("option").hasArg()
                .withDescription("ws handling. Possible values are: enable, disable, ssl, nossl, internalpki, externalapi").isRequired()
                .withLongOpt("gui").create("w");
        Option o121 = OptionBuilder.withArgName("id[,logfilepath]").hasArg().withDescription("single launch mode").isRequired()
                .withLongOpt("gui").create("s");
        Option o131 = OptionBuilder.withArgName("resourcefile").hasArg()
                .withDescription("resource parameter file to use. Default is resources.xml").withLongOpt("resources").create("p");
        Option o141 = OptionBuilder.withArgName("login,password,role1,role2,...").hasArgs(Option.UNLIMITED_VALUES).withValueSeparator(',')
                .withDescription("Create or update a JQM account. Roles must exist beforehand.").create("U");
        Option o151 = OptionBuilder.withArgName("configXmlFile").hasArg().withDescription("Import this file. JQM internal use only.")
                .withLongOpt("configXmlFile").create("c");
        Option o161 = OptionBuilder.withArgName("apply-node-template").hasArg()
                .withDescription("Copy all queue polling parameters from one node to the another node. Syntax is templatenode,targetnode")
                .withLongOpt("apply-node-template").create("t");
        Option o171 = OptionBuilder
                .withDescription("Returns node count")
                .create("nodecount");

        Options options = new Options();
        OptionGroup og1 = new OptionGroup();
        og1.setRequired(true);
        og1.addOption(o00);
        og1.addOption(o01);
        og1.addOption(o11);
        og1.addOption(o21);
        og1.addOption(o22);
        og1.addOption(o31);
        og1.addOption(o51);
        og1.addOption(o61);
        og1.addOption(o71);
        og1.addOption(o81);
        og1.addOption(o91);
        og1.addOption(o101);
        og1.addOption(o111);
        og1.addOption(o121);
        og1.addOption(o141);
        og1.addOption(o151);
        og1.addOption(o161);
        og1.addOption(o171);
        options.addOptionGroup(og1);
        OptionGroup og2 = new OptionGroup();
        og2.addOption(o131);
        options.addOptionGroup(og2);
        OptionGroup og3 = new OptionGroup();
        og3.addOption(o62);
        options.addOptionGroup(og3);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);

        try
        {
            // Parse arguments
            CommandLineParser parser = new BasicParser();
            CommandLine line = parser.parse(options, args);

            // Other db connection?
            if (line.getOptionValue(o131.getOpt()) != null)
            {
                jqmlogger.info("Using resource XML file " + line.getOptionValue(o131.getOpt()));
                Helpers.resourceFile = line.getOptionValue(o131.getOpt());
            }

            // Set db connection
            Helpers.registerJndiIfNeeded();

            // Specific port
            int port = 0;
            if (line.getOptionValue(o62.getOpt()) != null)
            {
                jqmlogger.info("Using specific port " + line.getOptionValue(o62.getOpt()));
                port = Integer.parseInt(line.getOptionValue(o62.getOpt()));
            }

            // Enqueue
            if (line.getOptionValue(o11.getOpt()) != null)
            {
                enqueue(line.getOptionValue(o11.getOpt()));
            }
            // Get status
            if (line.getOptionValue(o91.getOpt()) != null)
            {
                getStatus(Integer.parseInt(line.getOptionValue(o91.getOpt())));
            }
            // Import XML
            else if (line.getOptionValue(o21.getOpt()) != null)
            {
                importJobDef(line.getOptionValue(o21.getOpt()));
            }
            // Export job def XML
            else if (line.getOptionValue(o22.getOpt()) != null)
            {
                exportJobDef(line.getOptionValue(o22.getOpt()));
            }
            // Start engine
            else if (line.getOptionValue(o00.getOpt()) != null)
            {
                startEngine(line.getOptionValue(o00.getOpt()));
            }
            // Export all Queues
            else if (line.getOptionValue(o31.getOpt()) != null)
            {
                exportAllQueues(line.getOptionValue(o31.getOpt()));
            }
            // Import queues
            else if (line.getOptionValue(o51.getOpt()) != null)
            {
                importQueues(line.getOptionValue(o51.getOpt()));
            }
            // Create node
            else if (line.getOptionValue(o61.getOpt()) != null)
            {
                createEngine(line.getOptionValue(o61.getOpt()), port);
            }
            // Upgrade
            else if (line.hasOption(o81.getOpt()))
            {
                upgrade();
            }
            // Help
            else if (line.hasOption(o01.getOpt()))
            {
                formatter.printHelp("java -jar jqm-engine.jar", options, true);
            }
            // Version
            else if (line.hasOption(o71.getOpt()))
            {
                jqmlogger.info("Engine version: " + Helpers.getMavenVersion());
            }
            // Root password
            else if (line.hasOption(o101.getOpt()))
            {
                root(line.getOptionValue(o101.getOpt()));
            }
            // Web options
            else if (line.hasOption(o111.getOpt()))
            {
                ws(line.getOptionValue(o111.getOpt()));
            }
            // Web options
            else if (line.hasOption(o121.getOpt()))
            {
                single(line.getOptionValue(o121.getOpt()));
            }
            // User handling
            else if (line.hasOption(o141.getOpt()))
            {
                user(line.getOptionValues(o141.getOpt()));
            }
            // Configuration import
            else if (line.hasOption(o151.getOpt()))
            {
                importConfiguration(line.getOptionValue(o151.getOpt()));
            }
            // Node re-templating
            else if (line.hasOption(o161.getOpt()))
            {
                applyTemplate(line.getOptionValue(o161.getOpt()).split(",")[0], line.getOptionValue(o161.getOpt()).split(",")[1]);
            }
            // Node count
            else if (line.hasOption(o171.getOpt()))
            {
                count();
            }
        }
        catch (ParseException exp)
        {
            jqmlogger.fatal("Could not read command line: " + exp.getMessage());
            formatter.printHelp("java -jar jqm-engine.jar", options, true);
            return;
        }
    }

    private static void enqueue(String applicationName)
    {
        jqmlogger.info("Will enqueue application named " + applicationName + " without parameter overloads");
        jqmlogger.info("Request ID is: " + JqmClientFactory.getClient().enqueue(applicationName, "CommandLineUser"));
    }

    private static void getStatus(int id)
    {
        jqmlogger.info("Status is: " + JqmClientFactory.getClient().getJob(id).getState());
    }

    private static void importJobDef(String xmlpath)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            try
            {
                cnx.runSelectSingle("q_select_default", Integer.class);
            }
            catch (NoResultException e)
            {
                jqmlogger.fatal(
                        "Cannot import a Job Definition when there is no default queue defined. To solve, creating an engine will recreate it.");
                return;
            }

            // The parameter is a list of deployment descriptor files OR of directory which may contain descriptor files (XML).
            List<String> expandedPathes = new ArrayList<String>();
            for (String path : xmlpath.split(","))
            {
                File f = new File(path);
                if (!f.exists())
                {
                    jqmlogger.fatal("File does not exist: " + path);
                    return;
                }

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
        }
        catch (Exception e)
        {
            throw new JqmRuntimeException("Could not import file", e);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    public static void exportJobDef(String xmlPath)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            XmlJobDefExporter.export(xmlPath, cnx);
        }
        catch (Exception ex)
        {
            throw new JqmRuntimeException("Could not create the export file", ex);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    public static JqmEngineOperations startEngine(String nodeName)
    {
        try
        {
            engine = new JqmEngine();
            engine.start(nodeName, new EngineCallback());
            return engine;
        }
        catch (JqmRuntimeException e)
        {
            jqmlogger.fatal(e.getMessage());
            return null;
        }
        catch (Exception e)
        {
            jqmlogger.fatal("Could not launch the engine named " + nodeName
                    + ". This may be because no node with this name was declared (with command line option createnode).", e);
            throw new JqmRuntimeException("Could not start the engine", e);
        }
    }

    private static void createEngine(String nodeName, int port)
    {
        DbConn cnx = null;
        try
        {
            jqmlogger.info("Creating engine node " + nodeName);
            cnx = Helpers.getNewDbSession();
            Helpers.updateConfiguration(cnx);
            Helpers.updateNodeConfiguration(nodeName, cnx, port);
        }
        catch (Exception e)
        {
            throw new JqmRuntimeException("Could not create the engine", e);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private static void upgrade()
    {
        DbConn cnx = null;
        try
        {
            if (!Helpers.isDbInitialized())
            {
                Properties p = Db.loadProperties();
                p.setProperty("com.enioka.jqm.jdbc.allowSchemaUpdate", "true");
                Db db = new Db(p);
                Helpers.setDb(db);
            }
            cnx = Helpers.getNewDbSession();
            Helpers.updateConfiguration(cnx);
            cnx.commit();
            jqmlogger.info("Upgrade done");
            jqmlogger.info("Existing nodes: " + MetaService.getNodes(cnx).size());
        }
        catch (Exception e)
        {
            throw new JqmRuntimeException("Could not upgrade", e);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private static void exportAllQueues(String xmlPath)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            XmlQueueExporter.export(xmlPath, cnx);
        }
        catch (Exception ex)
        {
            throw new JqmRuntimeException("Could not create the export file", ex);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private static void importQueues(String xmlPath)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            XmlQueueParser.parse(xmlPath, cnx);
        }
        catch (Exception ex)
        {
            throw new JqmRuntimeException("Could not parse and import the file", ex);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private static void applyTemplate(String templateNode, String targetNode)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();

            // Throws exception if nodes not found.
            NodeDto template = MetaService.getNode(cnx, templateNode);
            NodeDto target = MetaService.getNode(cnx, targetNode);

            // Apply deployments parameters
            ArrayList<QueueMappingDto> mappings = new ArrayList<QueueMappingDto>(MetaService.getQueueMappings(cnx));
            List<QueueMappingDto> toRemove = new ArrayList<QueueMappingDto>(10);
            List<QueueMappingDto> toAdd = new ArrayList<QueueMappingDto>(10);
            for (QueueMappingDto mapping : mappings)
            {
                if (mapping.getNodeId().equals(template.getId()))
                {
                    QueueMappingDto r = new QueueMappingDto();
                    r.setEnabled(mapping.getEnabled());
                    r.setNbThread(mapping.getNbThread());
                    r.setNodeId(target.getId());
                    r.setNodeName(target.getName());
                    r.setPollingInterval(mapping.getPollingInterval());
                    r.setQueueId(mapping.getQueueId());
                    r.setQueueName(mapping.getQueueName());
                    toAdd.add(r);
                }
                if (mapping.getNodeId().equals(target.getId()))
                {
                    toRemove.add(mapping);
                }
            }

            mappings.addAll(toAdd);
            mappings.removeAll(toRemove);
            MetaService.syncQueueMappings(cnx, mappings);

            // Basic properties
            target.setEnabled(template.getEnabled());
            target.setJmxRegistryPort(template.getJmxRegistryPort());
            target.setJmxServerPort(template.getJmxServerPort());
            target.setJobRepoDirectory(template.getJobRepoDirectory());
            target.setLoadApiAdmin(template.getLoadApiAdmin());
            target.setLoadApiClient(template.getLoadApiClient());
            target.setLoapApiSimple(template.getLoapApiSimple());
            target.setOutputDirectory(template.getOutputDirectory());
            target.setDns(template.getDns());
            target.setPort(template.getPort());
            target.setRootLogLevel(template.getRootLogLevel());
            target.setTmpDirectory(template.getTmpDirectory());
            MetaService.upsertNode(cnx, target);

            // Done - meta service does not commit
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private static void root(String password)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            Helpers.createRoleIfMissing(cnx, "administrator", "all permissions without exception", "*:*");
            Helpers.createUserIfMissing(cnx, "root", password, "all powerful user", "administrator");
            MetaService.changeUserPassword(cnx, "root", password);
            cnx.commit();
        }
        catch (Exception ex)
        {
            jqmlogger.fatal("Could not reset the root account", ex);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private static void ws(String option)
    {
        if ("enable".equals(option))
        {
            DbConn cnx = Helpers.getNewDbSession();
            Helpers.setSingleParam("disableWsApi", "false", cnx);
            Helpers.setSingleParam("enableWsApiSsl", "false", cnx);
            Helpers.setSingleParam("enableWsApiAuth", "true", cnx);
            Helpers.setSingleParam("disableWsApiSimple", "false", cnx);
            Helpers.setSingleParam("disableWsApiClient", "false", cnx);
            Helpers.setSingleParam("disableWsApiAdmin", "false", cnx);
            Helpers.setSingleParam("enableInternalPki", "true", cnx);

            cnx.runUpdate("node_update_all_enable_ws");
            cnx.commit();
            cnx.close();
        }
        else if ("disable".equals(option))
        {
            DbConn cnx = Helpers.getNewDbSession();
            cnx.runUpdate("node_update_all_disable_ws");
            cnx.commit();
            cnx.close();
        }
        if ("ssl".equals(option))
        {
            DbConn cnx = Helpers.getNewDbSession();
            Helpers.setSingleParam("enableWsApiSsl", "true", cnx);
            Helpers.setSingleParam("enableWsApiAuth", "true", cnx);
            cnx.close();
        }
        if ("nossl".equals(option))
        {
            DbConn cnx = Helpers.getNewDbSession();
            Helpers.setSingleParam("enableWsApiSsl", "false", cnx);
            cnx.close();
        }
        if ("internalpki".equals(option))
        {
            DbConn cnx = Helpers.getNewDbSession();
            Helpers.setSingleParam("enableInternalPki", "true", cnx);
            cnx.close();
        }
        if ("externalpki".equals(option))
        {
            DbConn cnx = Helpers.getNewDbSession();
            Helpers.setSingleParam("enableInternalPki", "false", cnx);
            cnx.close();
        }
    }

    private static void user(String[] options)
    {
        if (options.length < 3)
        {
            throw new IllegalArgumentException("-U option requires one login, one password and at least one role (in this order)");
        }

        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();

            String[] roles = new String[options.length - 2];
            for (int i = 2; i < options.length; i++)
            {
                roles[i - 2] = options[i];
            }

            Helpers.createUserIfMissing(cnx, options[0], options[1], "created through CLI", roles);
            MetaService.changeUserPassword(cnx, options[0], options[1]);
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private static void single(String option)
    {
        int id = Integer.parseInt(option);
        JobInstance res = JqmSingleRunner.run(id);
        jqmlogger.info(res.getState());
    }

    private static void importConfiguration(String xmlpath)
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();

            String[] pathes = xmlpath.split(",");
            for (String path : pathes)
            {
                XmlConfigurationParser.parse(path, cnx);
            }
        }
        catch (Exception e)
        {
            throw new JqmRuntimeException("Could not import file", e);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private static void count()
    {
        DbConn cnx = null;
        try
        {
            cnx = Helpers.getNewDbSession();
            jqmlogger.info("Existing nodes: " + MetaService.getNodes(cnx).size());
        }
        catch (Exception e)
        {
            throw new JqmRuntimeException("Could not fetch node count", e);
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }
}
