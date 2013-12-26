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

package com.enioka.jqm.tools;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.apache.log4j.Logger;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;

public class Link
{
	private Logger jqmlogger = Logger.getLogger(Link.class);
	private ClassLoader old = null;
	private EntityManager em = null;
	private Integer id = null;
	private JobInstance ji;
	private History h;

	public Link(ClassLoader old, Integer id, EntityManager em)
	{
		this.old = old;
		this.em = em;
		this.id = id;
		ji = em.find(JobInstance.class, id);
		h = em.createQuery("SELECT h FROM History h WHERE h.jobInstanceId = :i", History.class).setParameter("i", this.id)
				.getSingleResult();
		// Load the libs if not done already
		em.refresh(ji);
	};

	public void sendMsg(String msg) throws JqmKillException
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(old);
		shouldKill();

		em.getTransaction().begin();
		Message mssg = new Message();
		mssg.setHistory(h);
		mssg.setTextMessage(msg);
		em.persist(mssg);
		em.getTransaction().commit();

		Thread.currentThread().setContextClassLoader(cl);
	}

	public void sendProgress(Integer msg) throws JqmKillException
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(old);
		shouldKill();

		em.getTransaction().begin();
		em.refresh(ji, LockModeType.PESSIMISTIC_WRITE);
		ji.setProgress(msg);
		h.setProgress(msg);
		em.getTransaction().commit();

		jqmlogger.debug("Actual progression: " + ji.getProgress());
		Thread.currentThread().setContextClassLoader(cl);
	}

	private void shouldKill() throws JqmKillException
	{
		em.refresh(ji);
		jqmlogger.debug("Analysis: should JI " + ji.getId() + " get killed? Status is " + ji.getState());
		if (ji.getState().equals("KILLED"))
		{
			jqmlogger.debug("Link: Job will be KILLED");
			Thread.currentThread().interrupt();
			throw new JqmKillException("This job" + "(ID: " + ji.getId() + ")" + " has been killed by a user");
		}
	}

	public int enQueue(String applicationName, String user, String mail, String sessionId, String application, String module,
			String keyword1, String keyword2, String keyword3, Integer parentId, Integer canBeRestart, Map<String, String> parameters)
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(old);

		JobDefinition jd = new JobDefinition(applicationName, user, mail);
		jd.setApplicationName(applicationName);
		jd.setUser(user);
		jd.setEmail(mail);
		jd.setSessionID(sessionId);
		jd.setApplication(application);
		jd.setModule(module);
		jd.setKeyword1(keyword1);
		jd.setKeyword2(keyword2);
		jd.setKeyword3(keyword3);
		jd.setParentID(parentId);
		jd.setParameters(parameters);

		int id = Dispatcher.enQueue(jd);

		Thread.currentThread().setContextClassLoader(cl);
		return id;
	}

	public int enQueueSynchronously(String applicationName, String user, String mail, String sessionId, String application, String module,
			String keyword1, String keyword2, String keyword3, Integer parentId, Integer canBeRestart, Map<String, String> parameters)
	{
		int i = enQueue(applicationName, user, mail, sessionId, application, module, keyword1, keyword2, keyword3, parentId, canBeRestart, parameters);

		while (true)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				jqmlogger.debug(e);
			}
			String status = em.createQuery("SELECT h.status FROM History h WHERE h.jobInstanceId = :id", String.class).setParameter("id", i).getSingleResult();

			if (status.equals("ENDED") || status.equals("CRASHED"))
			{
				break;
			}
		}

		return i;
	}
}
