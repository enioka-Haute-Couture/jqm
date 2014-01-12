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

import javax.persistence.EntityManager;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;

/**
 * Starter class & parameter parsing
 * 
 */
public class Main
{
    private static Logger jqmlogger = Logger.getLogger(Main.class);

    /**
     * Startup method for the packaged JAR
     * 
     * @param args
     *            0 is node name
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args)
    {
        Option o00 = OptionBuilder.withArgName("nodeName").hasArg().withDescription("name of the JQM node to start").isRequired()
                .create("startnode");
        Option o01 = OptionBuilder.withDescription("display help").withLongOpt("help").create("h");
        Option o11 = OptionBuilder.withArgName("applicationname").hasArg().withDescription("name of the application to launch")
                .isRequired().create("enqueue");
        Option o21 = OptionBuilder.withArgName("xmlpath").hasArg().withDescription("path of the XML configuration file to import")
                .isRequired().create("importjobdef");
        Option o31 = OptionBuilder.withArgName("xmlpath").hasArg().withDescription("export all queue definitions into an XML file")
                .isRequired().create("exportallqueues");
        OptionBuilder.withArgName("xmlpath").hasArg().withDescription("export some queue definitions into an XML file").isRequired()
                .create("exportqueuefile");
        OptionBuilder.withArgName("queues").hasArg().withDescription("queues to export").withValueSeparator(',').isRequired()
                .create("queue");
        Option o51 = OptionBuilder.withArgName("xmlpath").hasArg().withDescription("import all queue definitions from an XML file")
                .isRequired().create("importqueuefile");
        Option o61 = OptionBuilder.withArgName("nodeName").hasArg()
                .withDescription("create a JQM node of this name (init the database if needed").isRequired().create("createnode");

        Options options = new Options();
        OptionGroup og1 = new OptionGroup();
        og1.setRequired(true);
        og1.addOption(o00);
        og1.addOption(o01);
        og1.addOption(o11);
        og1.addOption(o21);
        og1.addOption(o31);
        og1.addOption(o51);
        og1.addOption(o61);
        options.addOptionGroup(og1);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);

        try
        {
            CommandLineParser parser = new BasicParser();
            CommandLine line = parser.parse(options, args);

            // Enqueue
            if (line.getOptionValue(o11.getOpt()) != null)
            {
                enqueue(line.getOptionValue(o11.getOpt()));
            }
            // Import XML
            else if (line.getOptionValue(o21.getOpt()) != null)
            {
                importJobDef(line.getOptionValue(o21.getOpt()));
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
                createEngine(line.getOptionValue(o61.getOpt()));
            }
            // Help
            else if (line.hasOption(o01.getOpt()))
            {
                formatter.printHelp("java -jar jqm-engine.jar", options, true);
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
        JobDefinition job = new JobDefinition(applicationName, "TestUser");
        Dispatcher.enQueue(job);
    }

    private static void importJobDef(String xmlpath)
    {
        try
        {
            EntityManager em = Helpers.getNewEm();
            if (em.createQuery("SELECT q FROM Queue q WHERE q.defaultQueue = true").getResultList().size() != 1)
            {
                jqmlogger
                        .fatal("Cannot import a Job Definition when there are no queues defined. Create at least an engine first to create one");
                em.close();
                return;
            }
            em.close();

            XmlParser parser = new XmlParser();
            parser.parse(xmlpath);
        }
        catch (Exception e)
        {
            jqmlogger.fatal(e);
            return;
        }
    }

    private static void startEngine(String nodeName)
    {
        try
        {
            JqmEngine engine = new JqmEngine();
            jqmlogger.info("Starting engine node " + nodeName);
            engine.start(nodeName);
        }
        catch (Exception e)
        {
            jqmlogger.fatal("Could not launch the engine - have you created the node? (this also creates tables in the database)", e);
        }
    }

    private static void createEngine(String nodeName)
    {
        try
        {
            Helpers.allowCreateSchema();
            JqmEngine engine = new JqmEngine();
            jqmlogger.info("Creating engine node " + nodeName);
            engine.checkAndUpdateNode(nodeName);
        }
        catch (Exception e)
        {
            jqmlogger.fatal("Could not create the engine", e);
        }
    }

    private static void exportAllQueues(String xmlPath)
    {
        QueueXmlExporter qxe = new QueueXmlExporter();
        qxe.exportAll(xmlPath);
    }

    private static void importQueues(String xmlPath)
    {
        try
        {
            QueueXmlParser parser = new QueueXmlParser();
            parser.parse(xmlPath);
        }
        catch (Exception ex)
        {
            jqmlogger.fatal("Could not parse and import the file", ex);
        }
    }

}
