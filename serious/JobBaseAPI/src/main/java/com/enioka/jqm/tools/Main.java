/**
 * Copyright �� 2013 enioka. All rights reserved
 * Authors: Pierre COPPEE (pierre.coppee@enioka.com)
 * Contributors : Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
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

		if (args.length != 1 && args.length != 3)
		{
			jqmlogger.fatal("The command line is incorrect");
			return;
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
						e.printStackTrace();
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
		}

		try
		{
			jqmlogger.info("Starting engine node " + args[0]);
			engine.start(args);
			Thread.sleep(Long.MAX_VALUE);

		} catch (Exception e)
		{
			jqmlogger.fatal("Could not launch the engine", e);
		}
	}
}
