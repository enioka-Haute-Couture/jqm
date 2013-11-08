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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobHistoryParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.JobParameter;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;

/**
 * This class will soon become private. It is not part of the API.
 * @author Marc-Antoine
 *
 */
public class CreationTools
{
	private static Logger jqmlogger = Logger.getLogger(CreationTools.class);
	public static EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");

	private CreationTools()
	{
	}

	// ------------------ GLOBALPARAMETER ------------------------

	public static GlobalParameter createGlobalParameter(String key, String value, EntityManager em)
	{
		GlobalParameter gp = new GlobalParameter();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		gp.setKey(key);
		gp.setValue(value);

		em.persist(gp);
		transac.commit();
		return gp;
	}

	// ------------------ JOBDEFINITION ------------------------

	public static JobDef initJobDefinition(String javaClassName, String filePath, Queue queue, EntityManager em)
	{
		JobDef j = new JobDef();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		j.setJavaClassName(javaClassName);
		j.setFilePath(filePath);
		j.setQueue(queue);

		em.persist(j);
		transac.commit();

		return j;
	}

	public static JobDef createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String filePath, String jp,
			Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String other1, String other2,
			String other3, boolean highlander, EntityManager em)
	{
		JobDef j = new JobDef();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		// ------------------

		j.setDescription(descripton);
		j.setCanBeRestarted(canBeRestarted);
		j.setJavaClassName(javaClassName);
		j.setParameters(jps);
		j.setFilePath(filePath);
		j.setQueue(queue);
		j.setMaxTimeRunning(maxTimeRunning);
		j.setApplicationName(applicationName);
		j.setApplication(application);
		j.setModule(module);
		j.setOther1(other1);
		j.setOther2(other2);
		j.setOther3(other3);
		j.setHighlander(highlander);
		j.setJarPath(jp);

		em.persist(j);
		transac.commit();

		return j;
	}

	// ------------------ DEPLOYMENTPARAMETER ------------------

	public static DeploymentParameter initDeploymentParameter(Node node, Integer nbThread, EntityManager em)
	{
		DeploymentParameter dp = new DeploymentParameter();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		dp.setNode(node);
		dp.setNbThread(nbThread);

		em.persist(dp);
		transac.commit();
		return dp;
	}

	public static DeploymentParameter createDeploymentParameter(Integer classId, Node node, Integer nbThread, Integer pollingInterval,
			Queue qVip, EntityManager em)
	{
		DeploymentParameter dp = new DeploymentParameter();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		dp.setClassId(classId);
		dp.setNode(node);
		dp.setNbThread(nbThread);
		dp.setPollingInterval(pollingInterval);
		dp.setQueue(qVip);

		em.persist(dp);
		transac.commit();
		return dp;
	}

	// ------------------ HISTORY ------------------------------

	public static History initHistory(Integer returnedValue, List<Message> messages, JobInstance jobInstance,
			List<JobHistoryParameter> jhp, EntityManager em)
	{
		History h = new History();

		h.setReturnedValue(returnedValue);
		h.setMessages(messages);
		h.setJobInstance(jobInstance);
		h.setParameters(jhp);

		em.persist(h);
		return h;
	}

	public static History createhistory(Integer returnedValue, Calendar jobDate, Integer JobDefId, Integer sessionId, Queue queue,
			String msg, List<Message> messages, JobInstance jobInstance, Calendar enqueueDate, Calendar executionDate, Calendar endDate,
			String userName, Node node, List<JobHistoryParameter> jhp, EntityManager em)
	{
		History h = new History();

		h.setReturnedValue(returnedValue);
		h.setJobDate(jobDate);
		h.setJobDefId(JobDefId);
		h.setSessionId(sessionId);
		h.setQueue(queue);
		h.setMsg(msg);
		h.setMessages(messages);
		h.setJobInstance(jobInstance);
		h.setEnqueueDate(enqueueDate);
		h.setExecutionDate(executionDate);
		h.setEndDate(endDate);
		h.setUserName(userName);
		h.setNode(node);
		h.setParameters(jhp);

		em.persist(h);
		return h;
	}

	// ------------------ JOBINSTANCE --------------------------

	public static JobInstance createJobInstance(JobDef jd, List<JobParameter> jps, String user, Integer sessionID, String state,
			Integer position, Queue queue, Node node, EntityManager em)
	{
		JobInstance j = new JobInstance();
		jqmlogger.debug("Creating JobInstance with " + jps.size() + " parameters");
		for (JobParameter jp : jps)
		{
			jqmlogger.debug("Parameter: " + jp.getKey() + " - " + jp.getValue());
		}
		j.setJd(jd);
		j.setSessionID(sessionID);
		j.setUserName(user);
		j.setState(state);
		j.setPosition(position);
		j.setQueue(queue);
		j.setNode(node);

		em.persist(j);
		j.setParameters(jps);

		return j;
	}

	// ------------------ JOBPARAMETER -------------------------

	public static JobParameter createJobParameter(String key, String value, EntityManager em)
	{
		JobParameter j = new JobParameter();

		j.setKey(key);
		j.setValue(value);

		em.persist(j);
		return j;
	}

	public static JobDefParameter createJobDefParameter(String key, String value, EntityManager em)
	{
		JobDefParameter j = new JobDefParameter();

		j.setKey(key);
		j.setValue(value);

		em.persist(j);
		return j;
	}

	// ------------------ NODE ---------------------------------

	public static Node createNode(String listeningInterface, Integer port, String dlRepo, String repo, EntityManager em)
	{
		Node n = new Node();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		n.setListeningInterface(listeningInterface);
		n.setPort(port);
		n.setDlRepo(dlRepo);
		n.setRepo(repo);

		em.persist(n);
		transac.commit();
		return n;
	}

	// ------------------ QUEUE --------------------------------

	public static Queue initQueue(String name, String description, Integer maxTempInQueue, Integer maxTempRunning, EntityManager em)
	{
		Queue q = new Queue();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		q.setName(name);
		q.setDescription(description);
		q.setMaxTempInQueue(maxTempInQueue);
		q.setMaxTempRunning(maxTempRunning);

		em.persist(q);
		transac.commit();

		return q;
	}

	public static Queue createQueue(String name, String description, Integer maxTempInQueue, Integer maxTempRunning, boolean defaultQueue,
			EntityManager em)
	{
		Queue q = new Queue();
		EntityTransaction transac = em.getTransaction();
		transac.begin();

		q.setName(name);
		q.setDescription(description);
		q.setMaxTempInQueue(maxTempInQueue);
		q.setMaxTempRunning(maxTempRunning);
		q.setDefaultQueue(defaultQueue);

		em.persist(q);
		transac.commit();
		return q;
	}

	// ------------------ DATABASEPROP --------------------------------

	public static DatabaseProp createDatabaseProp(String name, String driver, String url, String user, String pwd, EntityManager em)
	{
		DatabaseProp h = new DatabaseProp();

		h.setName(name);
		h.setDriver(driver);
		h.setUrl(url);
		h.setUserName(user);
		h.setPwd(pwd);

		em.persist(h);
		return h;
	}

	// ------------------ JNDI FOR JMS & co --------------------------------
	public static JndiObjectResource createJndiObjectResource(EntityManager em, String jndiAlias, String className, String factoryClass,
			String description, HashMap<String, String> parameters)
	{
		JndiObjectResource res = new JndiObjectResource();
		res.setAuth(null);
		res.setDescription(description);
		res.setFactory(factoryClass);
		res.setName(jndiAlias);
		res.setType(className);
		em.persist(res);

		for (String parameterName : parameters.keySet())
		{
			JndiObjectResourceParameter prm = new JndiObjectResourceParameter();
			prm.setKey(parameterName);
			prm.setValue(parameters.get(parameterName));
			em.persist(prm);
			res.getParameters().add(prm);
			prm.setResource(res);
		}

		return res;
	}

	public static JndiObjectResource createJndiQcfMQSeries(EntityManager em, String jndiAlias, String description, String hostname,
			String queueManagerName, Integer port, String channel)
	{
		return createJndiQcfMQSeries(em, jndiAlias, description, hostname, queueManagerName, port, channel, null);
	}

	public static JndiObjectResource createJndiQcfMQSeries(EntityManager em, String jndiAlias, String description, String hostname,
			String queueManagerName, Integer port, String channel, HashMap<String, String> optionalParameters)
	{
		HashMap<String, String> prms = new HashMap<String, String>();
		prms.put("HOST", hostname);
		prms.put("PORT", port.toString());
		prms.put("CHAN", channel);
		prms.put("QMGR", queueManagerName);
		prms.put("TRAN", "1"); // 0 = bindings, 1 = CLIENT, 2 = DIRECT, 4 = DIRECTHTTP
		if (optionalParameters != null)
		{
			prms.putAll(optionalParameters);
		}

		return createJndiObjectResource(em, jndiAlias, "com.ibm.mq.jms.MQQueueConnectionFactory",
				"com.ibm.mq.jms.MQQueueConnectionFactoryFactory", description, prms);
	}

	public static JndiObjectResource createJndiQueueMQSeries(EntityManager em, String jndiAlias, String description, String queueName,
			HashMap<String, String> optionalParameters)
	{
		HashMap<String, String> prms = new HashMap<String, String>();
		prms.put("QU", queueName);
		if (optionalParameters != null)
		{
			prms.putAll(optionalParameters);
		}

		return createJndiObjectResource(em, jndiAlias, "com.ibm.mq.jms.MQQueue", "com.ibm.mq.jms.MQQueueFactory", description, prms);
	}

	public static JndiObjectResource createJndiQcfActiveMQ(EntityManager em, String jndiAlias, String description, String Url,
			HashMap<String, String> optionalParameters)
	{
		HashMap<String, String> prms = new HashMap<String, String>();
		prms.put("brokerURL", Url);
		if (optionalParameters != null)
		{
			prms.putAll(optionalParameters);
		}

		return createJndiObjectResource(em, jndiAlias, "org.apache.activemq.ActiveMQConnectionFactory",
				"org.apache.activemq.jndi.JNDIReferenceFactory", description, prms);
	}

	public static JndiObjectResource createJndiQueueActiveMQ(EntityManager em, String jndiAlias, String description, String queueName,
			HashMap<String, String> optionalParameters)
	{
		HashMap<String, String> prms = new HashMap<String, String>();
		prms.put("physicalName", queueName);
		if (optionalParameters != null)
		{
			prms.putAll(optionalParameters);
		}

		return createJndiObjectResource(em, jndiAlias, "org.apache.activemq.command.ActiveMQQueue",
				"org.apache.activemq.jndi.JNDIReferenceFactory", description, prms);
	}

	// ------------------ CLOSE ENTITYs ------------------------

	public static void close(EntityManager em)
	{
		em.close();
		emf.close();
	}
}
