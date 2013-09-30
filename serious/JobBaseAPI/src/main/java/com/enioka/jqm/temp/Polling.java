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

package com.enioka.jqm.temp;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.jpamodel.JobInstance;

public class Polling
{
	ArrayList<JobInstance> job = null;
	public Polling()
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
		EntityManager em = emf.createEntityManager();

		ArrayList<JobInstance> q = (ArrayList<JobInstance>) em.createQuery("SELECT j FROM JobInstance j, JobDefinition jd" +
				" WHERE j.jd.queue.name = :q ORDER BY j.position", JobInstance.class).setParameter("q", "VIPQueue").getResultList();
		job = q;
	}

	public ArrayList<JobInstance> getJob()
	{
		return job;
	}

}
