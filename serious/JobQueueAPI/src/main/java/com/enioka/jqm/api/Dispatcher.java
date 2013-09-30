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

package com.enioka.jqm.api;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.JobDefinition;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.tools.CreationTools;

/**
 *
 * @author pierre.coppee
 */
public class Dispatcher {

	public static int enQueue(JobDefinition job) {
//		"SELECT DISTINCT (j) FROM JobInstance j WHERE EXISTS (" +
//				"SELECT MAX (j.position) FROM JobInstance j WHERE j.state = :state"
		Integer p = CreationTools.em.createQuery("SELECT MAX (j.position) FROM JobInstance j, JobDefinition jd " +
				"WHERE j.jd.queue.name = :queue", Integer.class).setParameter("queue", (job.getQueue().getName())).getSingleResult();
		System.out.println("POSITION: " + p);
		JobInstance ji = CreationTools.createJobInstance(job, "MAG", 42, "SUBMITTED", (p == null) ? 1 : p + 1);
		return ji.getId();
	}

	public static void delJobInQueue(int idJob) {

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query q = CreationTools.em.createQuery("DELETE FROM JobInstance j WHERE j.id = :idJob").setParameter("idJob", idJob);
		int res = q.executeUpdate();

		if (res != 1)
			System.err.println("delJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	public static void cancelJobInQueue(int idJob) {

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query q = CreationTools.em.createQuery("UPDATE JobInstance j SET j.state = 'CANCELLED' WHERE j.id = :idJob").setParameter("idJob", idJob);
		int res = q.executeUpdate();

		if (res != 1)
			System.err.println("delJobInQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();

	}

	public static void stopJob(int idJob) {

	}

	public static void killJob(int idJob) {

	}

	public static void restartCrashedJob(int idJob) {

	}

	public static int restartJob(int idJob) {

		return 0;
	}

	public static void setPosition(int idJob, int position) {


		List<JobInstance> q = CreationTools.em.createQuery("SELECT j FROM JobInstance j WHERE j.state = :s " +
				"ORDER BY j.position", JobInstance.class).setParameter("s", "SUBMITTED").getResultList();

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query query = CreationTools.em.createQuery("UPDATE JobInstance j SET j.position = :pos WHERE " +
				"j.id = (SELECT ji.id FROM JobInstance ji WHERE ji.id = :idJob)").setParameter("idJob", idJob).setParameter("pos", position);

		int result = query.executeUpdate();

		for (int i = 0; i < q.size(); i++)
		{
			if (q.get(i).getId() == idJob)
				continue;
			else if (i + 1 == position)
			{
				Query queryEg = CreationTools.em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job").setParameter("i",
						position + 2).setParameter("job", q.get(i).getId());
				int res = queryEg.executeUpdate();
				i++;
			}
			else
			{
				Query qq = CreationTools.em.createQuery("UPDATE JobInstance j SET j.position = :i WHERE j.id = :job").setParameter("i",
						i + 1).setParameter("job", q.get(i).getId());
				int res = qq.executeUpdate();

			}
		}

		transac.commit();
	}

	public static List<InputStream> getDeliverables(int idJob) {

		ArrayList<InputStream> streams = new ArrayList<InputStream>();

		return streams;
	}

	public static List<Deliverable> getAllDeliverables(int idJob) {

		ArrayList<Deliverable> deliverables = new ArrayList<Deliverable>();

		return deliverables;
	}

	public static InputStream getOneDeliverable(Deliverable deliverable) {

		return null;
	}

	public static List<Deliverable> getUserDeliverables(String user) {

		return null;
	}

	public static List<String> getMsg(int idJob) {

		ArrayList<String> msgs = new ArrayList<String>();

		return msgs;
	}

	public static List<JobInstance> getUserJobs(String user) {

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) CreationTools.em.createQuery("SELECT j FROM JobInstance j WHERE j.user = :u", JobInstance.class).setParameter("u", user).getResultList();

		return jobs;
	}

	public static List<JobInstance> getJobs() {

		ArrayList<JobInstance> jobs = (ArrayList<JobInstance>) CreationTools.em.createQuery("SELECT j FROM JobInstance j", JobInstance.class).getResultList();

		return jobs;
	}

	public static List<Queue> getQueues() {

		ArrayList<Queue> queues = (ArrayList<Queue>) CreationTools.em.createQuery("SELECT j FROM Queue j", Queue.class).getResultList();

		return queues;
	}

	public static void changeQueue(int idJob, int idQueue) {

		JobDefinition jd = CreationTools.em.createQuery("SELECT j.jd FROM JobInstance j, JobDefinition jd WHERE j.id = :idJob", JobDefinition.class).setParameter("idJob",
						idJob).getSingleResult();

		Queue q = CreationTools.em.createQuery("SELECT Queue FROM Queue queue " +
				"WHERE queue.id = :q", Queue.class).setParameter("q", idQueue).getSingleResult();

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query query = CreationTools.em.createQuery("UPDATE JobDefinition j SET j.queue = :q WHERE j = :jd").setParameter("q", q).setParameter("jd", jd);
		int result = query.executeUpdate();

		if (result != 1)
			System.err.println("changeQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();
	}

	public static void changeQueue(int idJob, Queue queue) {

		JobDefinition jd = CreationTools.em.createQuery("SELECT j.jd FROM JobInstance j, JobDefinition jd WHERE j.id = :idJob", JobDefinition.class).setParameter("idJob",
				idJob).getSingleResult();

		Queue q = CreationTools.em.createQuery("SELECT Queue FROM Queue queue " +
				"WHERE queue.id = :q", Queue.class).setParameter("q", queue.getId()).getSingleResult();

		EntityTransaction transac = CreationTools.em.getTransaction();
		transac.begin();

		Query query = CreationTools.em.createQuery("UPDATE JobDefinition j SET j.queue = :q WHERE j = :jd").setParameter("q", q).setParameter("jd", jd);
		int result = query.executeUpdate();

		if (result != 1)
			System.err.println("changeQueueError: Job ID or Queue ID doesn't exists.");

		transac.commit();
	}
}
