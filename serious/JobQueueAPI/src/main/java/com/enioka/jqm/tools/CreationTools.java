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

import com.enioka.jqm.jpamodel.DatabaseProp;
import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.ExecParameter;
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

public class CreationTools
{
	public static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");

	public CreationTools()
	{
	}

	// ------------------ JOBDEFINITION ------------------------

	public static JobDef initJobDefinition(final String javaClassName, final String filePath, final Queue queue, final EntityManager em)
	{
		final JobDef j = new JobDef();
		final EntityTransaction transac = em.getTransaction();
		transac.begin();

		j.setJavaClassName(javaClassName);
		j.setFilePath(filePath);
		j.setQueue(queue);

		em.persist(j);
		transac.commit();

		return j;
	}

	public static JobDef createJobDef(final boolean canBeRestarted, final String javaClassName, final List<JobDefParameter> jps, final String filePath, final String jp,
			final Queue queue, final Integer maxTimeRunning, final String applicationName, final String application, final String module,
			final String other1, final String other2, final String other3, final boolean highlander, final EntityManager em)
	{
		final JobDef j = new JobDef();
		final EntityTransaction transac = em.getTransaction();
		transac.begin();

		// ------------------

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

	public static DeploymentParameter initDeploymentParameter(final Node node, final Integer nbThread, final EntityManager em)
	{
		final DeploymentParameter dp = new DeploymentParameter();
		final EntityTransaction transac = em.getTransaction();
		transac.begin();

		dp.setNode(node);
		dp.setNbThread(nbThread);

		em.persist(dp);
		transac.commit();
		return dp;
	}

	public static DeploymentParameter createDeploymentParameter(final Integer classId, final Node node, final Integer nbThread, final Integer pollingInterval,
			final Queue qVip, final EntityManager em)
	{
		final DeploymentParameter dp = new DeploymentParameter();
		final EntityTransaction transac = em.getTransaction();
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

	// ------------------ EXECPARAMETER ------------------------

	public static ExecParameter createExecParameter(final String key, final String value, final JobInstance jobInstance, final EntityManager em)
	{
		final ExecParameter e = new ExecParameter();
		final EntityTransaction transac = em.getTransaction();
		transac.begin();

		e.setKey(key);
		e.setValue(value);
		e.setJobInstance(jobInstance);

		em.persist(e);
		transac.commit();
		return e;
	}

	// ------------------ HISTORY ------------------------------

	public static History initHistory(final Integer returnedValue, final List<Message> messages, final JobInstance jobInstance,
			final List<JobHistoryParameter> jhp, final EntityManager em)
	{
		final History h = new History();

		h.setReturnedValue(returnedValue);
		h.setMessages(messages);
		h.setJobInstance(jobInstance);
		h.setParameters(jhp);

		em.persist(h);
		return h;
	}

	public static History createhistory(final Integer returnedValue, final Calendar jobDate, final Integer JobDefId, final Integer sessionId, final Queue queue, final String msg, final List<Message> messages,
			final JobInstance jobInstance, final Calendar enqueueDate, final Calendar executionDate, final Calendar endDate, final String userName, final Node node, final List<JobHistoryParameter> jhp,
			final EntityManager em)
	{
		final History h = new History();

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

	public static JobInstance createJobInstance(final JobDef jd, final List<JobParameter> jps, final String user, final Integer sessionID, final String state,
			final Integer position, final Queue queue, final Node node, final EntityManager em)
	{
		final JobInstance j = new JobInstance();

		j.setJd(jd);
		j.setParameters(jps);
		j.setSessionID(sessionID);
		j.setUserName(user);
		j.setState(state);
		j.setPosition(position);
		j.setQueue(queue);
		j.setNode(node);

		em.persist(j);

		return j;
	}

	// ------------------ JOBPARAMETER -------------------------

	public static JobParameter createJobParameter(final String key, final String value, final EntityManager em)
	{
		final JobParameter j = new JobParameter();

		j.setKey(key);
		j.setValue(value);

		em.persist(j);
		return j;
	}

	public static JobDefParameter createJobDefParameter(final String key, final String value, final EntityManager em)
	{
		final JobDefParameter j = new JobDefParameter();

		j.setKey(key);
		j.setValue(value);

		em.persist(j);
		return j;
	}

	// ------------------ NODE ---------------------------------

	public static Node createNode(final String listeningInterface, final Integer port, final String dlRepo, final String repo, final EntityManager em)
	{
		final Node n = new Node();
		final EntityTransaction transac = em.getTransaction();
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

	public static Queue initQueue(final String name, final String description, final Integer maxTempInQueue, final Integer maxTempRunning, final EntityManager em)
	{
		final Queue q = new Queue();
		final EntityTransaction transac = em.getTransaction();
		transac.begin();

		q.setName(name);
		q.setDescription(description);
		q.setMaxTempInQueue(maxTempInQueue);
		q.setMaxTempRunning(maxTempRunning);

		em.persist(q);
		transac.commit();

		return q;
	}

	public static Queue createQueue(final String name, final String description, final Integer maxTempInQueue, final Integer maxTempRunning, final boolean defaultQueue,
			final EntityManager em)
	{
		final Queue q = new Queue();
		final EntityTransaction transac = em.getTransaction();
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

	public static DatabaseProp createDatabaseProp(final String name, final String driver, final String url, final String user, final String pwd, final EntityManager em)
	{
		final DatabaseProp h = new DatabaseProp();

		h.setName(name);
		h.setDriver(driver);
		h.setUrl(url);
		h.setUserName(user);
		h.setPwd(pwd);

		em.persist(h);
		return h;
	}

	// ------------------ JNDI FOR JMS & co --------------------------------
	public static JndiObjectResource createJndiObjectResource(final EntityManager em, final String jndiAlias, final String className, final String factoryClass,
			final String description, final HashMap<String, String> parameters)
	{
		final JndiObjectResource res = new JndiObjectResource();
		res.setAuth(null);
		res.setDescription(description);
		res.setFactory(factoryClass);
		res.setName(jndiAlias);
		res.setType(className);
		em.persist(res);

		for (final String parameterName : parameters.keySet())
		{
			final JndiObjectResourceParameter prm = new JndiObjectResourceParameter();
			prm.setKey(parameterName);
			prm.setValue(parameters.get(parameterName));
			em.persist(prm);
			res.getParameters().add(prm);
			prm.setResource(res);
		}

		return res;
	}

	public static JndiObjectResource createJndiQcfMQSeries(final EntityManager em, final String jndiAlias, final String description, final String hostname,
			final String queueManagerName, final Integer port, final String channel)
	{
		return createJndiQcfMQSeries(em, jndiAlias, description, hostname, queueManagerName, port, channel, null);
	}

	public static JndiObjectResource createJndiQcfMQSeries(final EntityManager em, final String jndiAlias, final String description, final String hostname,
			final String queueManagerName, final Integer port, final String channel, final HashMap<String, String> optionalParameters)
	{
		final HashMap<String, String> prms = new HashMap<String, String>();
		prms.put("HOST", hostname);
		prms.put("PORT", port.toString());
		prms.put("CHAN", channel);
		prms.put("QMGR", queueManagerName);
		if (optionalParameters != null)
			prms.putAll(optionalParameters);

		return createJndiObjectResource(em, jndiAlias, "com.ibm.mq.jms.MQQueueConnectionFactory",
				"com.ibm.mq.jms.MQQueueConnectionFactoryFactory", description, prms);
	}

	public static JndiObjectResource createJndiQueueMQSeries(final EntityManager em, final String jndiAlias, final String description, final String queueName,
			final HashMap<String, String> optionalParameters)
	{
		final HashMap<String, String> prms = new HashMap<String, String>();
		prms.put("QU", queueName);
		if (optionalParameters != null)
			prms.putAll(optionalParameters);

		return createJndiObjectResource(em, jndiAlias, "com.ibm.mq.jms.MQQueue", "com.ibm.mq.jms.MQQueueFactory", description, prms);
	}

	public static JndiObjectResource createJndiQcfActiveMQ(final EntityManager em, final String jndiAlias, final String description, final String Url,
			final HashMap<String, String> optionalParameters)
	{
		final HashMap<String, String> prms = new HashMap<String, String>();
		prms.put("brokerURL", Url);
		if (optionalParameters != null)
			prms.putAll(optionalParameters);

		return createJndiObjectResource(em, jndiAlias, "org.apache.activemq.ActiveMQConnectionFactory",
				"org.apache.activemq.jndi.JNDIReferenceFactory", description, prms);
	}

	public static JndiObjectResource createJndiQueueActiveMQ(final EntityManager em, final String jndiAlias, final String description, final String queueName,
			final HashMap<String, String> optionalParameters)
	{
		final HashMap<String, String> prms = new HashMap<String, String>();
		prms.put("physicalName", queueName);
		if (optionalParameters != null)
			prms.putAll(optionalParameters);

		return createJndiObjectResource(em, jndiAlias, "org.apache.activemq.command.ActiveMQQueue",
				"org.apache.activemq.jndi.JNDIReferenceFactory", description, prms);
	}

	// ------------------ CLOSE ENTITYs ------------------------

	public static void close(final EntityManager em)
	{
		em.close();
		emf.close();
	}
}
