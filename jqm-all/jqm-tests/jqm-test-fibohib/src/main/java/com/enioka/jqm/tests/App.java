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

package com.enioka.jqm.tests;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobBase;
import com.enioka.jqm.api.JobDefinition;

public class App extends JobBase
{

	@Override
	public void start()
	{
		System.out.println("PARAMETRE FIBO 2: " + this.parameters.get("p2"));

		JobDefinition jd = new JobDefinition("FiboHib", "Jean Paul");

		jd.addParameter("p1", this.parameters.get("p2"));
		jd.addParameter("p2", (Integer.parseInt(this.parameters.get("p1")) + Integer.parseInt(this.parameters.get("p2")) + ""));
		System.out.println("BEFORE ENQUEUE");

		if (Integer.parseInt(this.parameters.get("p1")) <= 100)
		{
			Dispatcher.enQueue(jd);
		}
		System.out.println("QUIT FIBO");
	}
}
