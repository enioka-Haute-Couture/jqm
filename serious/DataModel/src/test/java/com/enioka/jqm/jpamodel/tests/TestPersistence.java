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

package com.enioka.jqm.jpamodel.tests;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.junit.Test;

import com.enioka.jqm.jpamodel.Node;

public class TestPersistence {

	@Test
	public void testSave() {
		final EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("jobqueue-api-pu");

		final EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();

		final Node n = new Node();
		n.setListeningInterface("localhost");
		n.setPort(1234);
		n.setDlRepo("/Temp/");
		n.setRepo("/Temp2/");

		em.persist(n);

		em.getTransaction().commit();

		final EntityManager em2 = emf.createEntityManager();
		final long i = (Long) em2.createQuery("SELECT COUNT(t) from Node t")
				.getSingleResult();

		Assert.assertEquals(1, i);
	}
}
