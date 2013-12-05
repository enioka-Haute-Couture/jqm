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

package com.enioka.jqm.jpamodel;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Test;

public class TestPersistence
{
	@Test
	public void testSave()
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");

		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		Node n = new Node();
		n.setListeningInterface("localhost");
		n.setPort(1234);
		n.setDlRepo("/Temp/");
		n.setRepo("/tmp");

		em.persist(n);

		em.getTransaction().commit();

		EntityManager em2 = emf.createEntityManager();
		long i = (Long) em2.createQuery("SELECT COUNT(t) from Node t").getSingleResult();

		Assert.assertTrue(i >= 1);
	}

	@Test
	public void testSave2()
	{
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");

		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		Queue q = new Queue();
		q.setDefaultQueue(true);
		q.setDescription("ppp");
		q.setMaxTempInQueue(12);
		q.setMaxTempRunning(13);
		q.setName("super queue");
		em.persist(q);

		JobDef jd = new JobDef();

		jd.setApplication("MARSU");
		jd.setApplicationName("MARSU");
		jd.setCanBeRestarted(false);
		jd.setFilePath("/eeee");
		jd.setHighlander(false);
		jd.setJarPath("/ml");
		jd.setJavaClassName("/mlmlmlmlmlml");
		jd.setMaxTimeRunning(10);
		jd.setModule("MMM");
		jd.setQueue(q);

		em.persist(jd);

		JobDefParameter p1 = new JobDefParameter();
		jd.getParameters().add(p1);
		p1.setKey("mm");
		p1.setValue("pppp");
		em.persist(p1);

		JobDefParameter p2 = new JobDefParameter();
		jd.getParameters().add(p2);
		p2.setKey("mm");
		p2.setValue("pppp");
		em.persist(p2);

		em.getTransaction().commit();

		EntityManager em2 = emf.createEntityManager();
		long i = (Long) em2.createQuery("SELECT COUNT(t) from JobDef t").getSingleResult();
		Assert.assertTrue(i >= 1);

		jd = em2.find(JobDef.class, jd.getId());
		Assert.assertTrue(2 <= jd.getParameters().size());
	}
}
