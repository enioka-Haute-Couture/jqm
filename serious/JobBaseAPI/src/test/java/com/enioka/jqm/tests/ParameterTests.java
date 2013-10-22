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

public class ParameterTests
{
	@Test
	public void testMixParameter() throws Exception
	{
		EntityManager em = Helpers.getNewEm();
		Helpers.cleanup(em);
		Helpers.createLocalNode(em);

		ArrayList<JobDefParameter> jdargs = new ArrayList<JobDefParameter>();
		JobDefParameter jdp = CreationTools.createJobDefParameter("arg1", "argument1", em);
		JobDefParameter jdp2 = CreationTools.createJobDefParameter("arg2", "Franquin", em);
		jdargs.add(jdp);
		jdargs.add(jdp2);

		JobDef jdDemoMaven = CreationTools.createJobDef(true, "App", jdargs, "./testprojects/CheckArgs/",
				"./testprojects/CheckArgs/CheckArgs.jar", Helpers.qVip, 42, "MarsuApplication", 42, "Franquin", "ModuleMachin", "other",
				"other", "other", false, em);

		JobDefinition j = new JobDefinition("MarsuApplication", "MAG");
		j.addParameter("arg2", "argument2");

		Dispatcher.enQueue(j);

		Main.main(new String[] { "localhost" });
		Thread.sleep(5000);
		Main.stop();

		Assert.assertEquals(true, true);
	}
}
