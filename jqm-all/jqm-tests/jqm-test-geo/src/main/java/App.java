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

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.api.JobBase;

public class App extends JobBase
{

	@Override
	public void start()
	{
		System.out.println("BEGINING GEOJOB: " + this.parameters.get("nbJob"));
		Map<String, String> p = new HashMap<String, String>();
		p.put("nbJob", ((Integer.parseInt(this.parameters.get("nbJob")) + 1) + ""));

		if (Integer.parseInt(this.parameters.get("nbJob")) >= 5)
		{
			System.out.println("END OF GEO - reached 5th generation");
			return;
		}

		sendMsg("launching first job");
		System.out.println("LAUNCHING FIRST JOB");
		enQueue("Geo", "Dark Vador", null, null, null, null, null, null, null, null, null, p);
		sendMsg("launching second job");
		System.out.println("LAUNCHING SECOND JOB");
		enQueue("Geo", "Dark Vador", null, null, null, null, null, null, null, null, null, p);
		System.out.println("ENDING GEOJOB");
	}
}
