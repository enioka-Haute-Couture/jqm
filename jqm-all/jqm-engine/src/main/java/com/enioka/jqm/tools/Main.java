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

import java.util.ArrayList;

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
	public static void main(String[] args)
	{
		JqmEngine engine = new JqmEngine();

		if (args.length == 1 && args[0].equals("-help"))
		{
			printUsage();
			return;
		}

		if (args.length >= 4 && args[1].equals("-exportQueue"))
		{
			if (args.length == 4) // exportAll or export
			{
				if (!args[2].isEmpty())
				{
					if (args[3].equals("-all"))
					{
						try
						{
							engine.checkAndUpdateNode(args[0]);
							QueueXmlExporter qxe = new QueueXmlExporter(args[0]);
							qxe.exportAll(args[2]);
							return;
						} catch (Exception e)
						{
							jqmlogger.fatal(e);
							return;
						}
					}
					else
					{
						try
						{
							engine.checkAndUpdateNode(args[0]);
							QueueXmlExporter qxe = new QueueXmlExporter(args[0]);
							qxe.export(args[2], args[3]);
							return;
						} catch (Exception e)
						{
							jqmlogger.fatal(e);
							return;
						}
					}
				}
			}
			else if (args.length > 4) // exportSeveral
			{
				try
				{
					int i = 3;
					engine.checkAndUpdateNode(args[0]);
					ArrayList<String> qs = new ArrayList<String>();

					while (!args[i].isEmpty())
					{
						qs.add(args[i]);
						i++;
					}

					QueueXmlExporter qxe = new QueueXmlExporter(args[0]);
					qxe.exportSeveral(args[2], qs);
					return;
				} catch (Exception e)
				{
					jqmlogger.fatal(e);
					return;
				}
			}
			else
			{
				jqmlogger.fatal("The command line is incorrect.");
				printUsage();
				return;
			}
		}
		if (args.length >= 3)
		{
			if (args[1].equals("-xml"))
			{
				if (!args[2].isEmpty())
				{
					try
					{
						engine.checkAndUpdateNode(args[0]);
						XmlParser parser = new XmlParser();
						parser.parse(args[2]);
						return;
					} catch (Exception e)
					{
						jqmlogger.fatal(e);
						return;
					}

				}
			}
			else if (args[1].equals("-enqueue"))
			{
				if (!args[2].isEmpty())
				{
					JobDefinition job = new JobDefinition(args[2], "TestUser");
					Dispatcher.enQueue(job);
					return;
				}
			}
			else if (args[1].equals("-importQueue"))
			{
				if (!args[2].isEmpty())
				{
					try
					{
						engine.checkAndUpdateNode(args[0]);
						QueueXmlParser parser = new QueueXmlParser();
						parser.parse(args[2]);
						return;
					} catch (Exception e)
					{
						jqmlogger.fatal(e);
						return;
					}
				}
			}
			else
			{
				jqmlogger.fatal("The command line is incorrect.");
				printUsage();
				return;
			}
		}
		else if (args.length == 1)
		{
			try
			{
				jqmlogger.info("Starting engine node " + args[0]);
				engine.start(args);
			} catch (Exception e)
			{
				jqmlogger.fatal("Could not launch the engine", e);
				return;
			}
		}
	}

	private static void printUsage()
	{
		System.out.println("Usage: ");
		System.out.println("--> To create a job definition");
		System.out.println("    [node] -xml [path/to/the/file.xml]");
		System.out.println("--> To enqueue an existing job");
		System.out.println("    [node] -enqueue [job name]");
		System.out.println("--> To import a queue configuration");
		System.out.println("    [node] -importQueue [path/to/the/file.xml]");
		System.out.println("--> To export a queue configuration");
		System.out.println("    [node] -exportQueue [filename_out.xml] [queue name]");
		System.out.println("--> To export several queue configurations");
		System.out.println("    [node] -exportQueue [filename_out.xml] [queue names ...]");
		System.out.println("--> To export all the queue configurations");
		System.out.println("    " +
				"[node] -exportQueue [filename_out.xml] -all");
	}
}
