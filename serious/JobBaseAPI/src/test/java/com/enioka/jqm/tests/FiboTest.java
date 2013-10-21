
package com.enioka.jqm.tests;

import java.util.ArrayList;

import javax.persistence.EntityManager;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobDefinition;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.tools.CreationTools;
import com.enioka.jqm.tools.Main;

public class FiboTest {

	@Test
	public void testFibo() throws Exception {

		EntityManager em = Helpers.getNewEm();
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg", "POUPETTE", em);
		jdargs.add(jdp);

		@SuppressWarnings("unused")
		JobDef jd = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/Fibo/", "./testprojects/Fibo/Fibo.jar", Helpers.qVip, 42, "Fibo",
		        42, "Franquin", "ModuleMachin", "other1", "other2", "other3", false, em);

		JobDefinition form = new JobDefinition("Fibo", "MAG");
		form.addParameter("p1", "1");
		form.addParameter("p2", "2");
		Dispatcher.enQueue(form);

		// Start the engine
		Main.main(new String[]
		{ "localhost" });

		Thread.sleep(10000);
		Main.stop();

		long i = (Long) em.createQuery("SELECT COUNT(h) FROM History h").getSingleResult();
		Assert.assertTrue(i > 2);
	}
}
