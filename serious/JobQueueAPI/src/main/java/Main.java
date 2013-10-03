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

import javax.persistence.EntityTransaction;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.jpamodel.JobDefinition;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;

public class Main
{

	static Queue qVip = null;
	static Queue qNormal = null;
	static Queue qSlow = null;

	static JobDefinition jd = null;

	static JobDefinition jdDemoMaven = null;

	static JobDefinition jdDemo = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM Message").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM History").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM JobInstance").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM JobParameter").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM JobDefinition").executeUpdate();
		transac.commit();

		transac = CreationTools.em.getTransaction();
		transac.begin();

		CreationTools.em.createQuery("DELETE FROM Queue").executeUpdate();

		transac.commit();

		qVip = CreationTools.initQueue("VIPQueue", "Queue for the winners", 42 , 100);
		qNormal = CreationTools.initQueue("NormalQueue", "Queue for the ordinary job", 7 , 100);
		qSlow = CreationTools.initQueue("SlowQueue", "Queue for the bad guys", 0 , 100);

		jd = CreationTools.createJobDefinition(true, "MarsuClassName", "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/", qVip,
				42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true);

		jdDemoMaven = CreationTools.createJobDefinition(true, "DemoMavenClassName", "/Users/pico/Dropbox/projets/enioka/tests/DateTimeMaven/", qNormal,
				42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true);

		jdDemo = CreationTools.createJobDefinition(true, "DemoClassName", "/Users/pico/Dropbox/projets/enioka/tests/Demo/", qSlow,
				42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other", "other", "other", true);

		Dispatcher.enQueue(jdDemoMaven);
		Dispatcher.enQueue(jdDemo);
		Dispatcher.enQueue(jd);
		Dispatcher.enQueue(jd);
		Dispatcher.enQueue(jd);
		//Dispatcher.changeQueue(238, 347);
		//Dispatcher.setPosition(88, 1);
//		for (JobInstance i : Dispatcher.getUserJobs("MAG"))
//		{
//			System.out.println("Jobs " + i.getId());
//		}
		//Dispatcher.delJobInQueue(70);
		//Dispatcher.enQueue(jd);
		CreationTools.close();
	}

}
