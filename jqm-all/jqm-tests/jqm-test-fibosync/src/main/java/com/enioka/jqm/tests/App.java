package com.enioka.jqm.tests;

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.api.JobBase;


public class App extends JobBase
{
	@Override
	public void start()
	{
		System.out.println("PARAMETRE FIBO 2: " + this.parameters.get("p2"));

		Map<String, String> p = new HashMap<String, String>();
		p.put("p1", "1");
		p.put("p2", "2");

		int ii = enQueueSynchronously("Fibo", "Luke", null, null, null, null, null, null, null, null, null, p);

		System.out.println("FIBO FINISHED. ID of the job: " + ii);

		System.out.println("666");
	}
}
