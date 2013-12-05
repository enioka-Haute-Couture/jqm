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

import com.enioka.jqm.api.JobBase;

public class App extends JobBase
{
	@Override
	public void start()
	{
		for (int i = 0; i <= 5000; i++)
		{
			// System.out.println(i);
			if (i == 500 || i == 1000 || i == 3500 || i == 5000)
			{
				sendProgress(i);
				System.out.println("Progress: " + i);
			}
			try
			{
				Thread.sleep(1);
			} catch (InterruptedException e)
			{
				// Nothing
			}
		}
	}
}