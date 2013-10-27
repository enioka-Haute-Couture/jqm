/**
 * Copyright © 2013 enioka. All rights reserved
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

public class Main
{
	/**
	 * Startup method for the packaged JAR
	 * 
	 * @param args
	 *            0 is node name
	 */
	public static void main(String[] args)
	{
		JqmEngine engine = new JqmEngine();
		try
		{
			engine.start(args);
			Thread.sleep(Long.MAX_VALUE);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
