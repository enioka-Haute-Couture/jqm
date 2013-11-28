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

		p.put("p1", this.parameters.get("p2"));
		p.put("p2", (Integer.parseInt(this.parameters.get("p1")) + Integer.parseInt(this.parameters.get("p2")) + ""));
		System.out.println("BEFORE ENQUEUE");

		if (Integer.parseInt(this.parameters.get("p1")) <= 100)
		{
			enQueue("Fibo", "Dark Vador", null, null, null, null, null, null, null, this.jobInstanceID, null, p);
		}
		System.out.println("QUIT FIBO");
	}
}
