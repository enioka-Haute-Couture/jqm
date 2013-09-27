package com.enioka.jqm.api;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

		JobInstance ji = CreationTools.createJobInstance(job, "MAG", 42, "SUBMITTED", 1);
		return ji.getId();
	}

	public static void delJobInQueue(int idJob) {
	}

	public static void cancelJobInQueue(int idJob) {

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

		ArrayList<JobInstance> jobs = new ArrayList<JobInstance>();

		return jobs;
	}

	public static List<JobInstance> getJobs() {

		ArrayList<JobInstance> jobs = new ArrayList<JobInstance>();

		return jobs;
	}

	public static List<Queue> getQueues() {

		ArrayList<Queue> queues = new ArrayList<Queue>();

		return queues;
	}

	public static void changeQueue(int id_job, int idQueue) {


	}

	public static void changeQueue(int id_job, Queue queue) {


	}
}
