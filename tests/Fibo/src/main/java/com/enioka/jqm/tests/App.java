package com.enioka.jqm.tests;

import com.enioka.jqm.api.Dispatcher;
import com.enioka.jqm.api.JobBase;
import com.enioka.jqm.api.JobDefinition;

public class App extends JobBase
{

	@Override
	public void start()
	{
		System.out.println("PARAMETRE FIBO 2: " + this.parameters.get("p2"));

		JobDefinition j = new JobDefinition("Fibo", "MAG");

		j.addParameter("p1", this.parameters.get("p2"));
		j.addParameter("p2", (Integer.parseInt(this.parameters.get("p1")) + Integer.parseInt(this.parameters.get("p2")) + ""));
		System.out.println("AVANT ENQUEUE");

		if (Integer.parseInt(this.parameters.get("p1")) <= 100)
			Dispatcher.enQueue(j);
		System.out.println("QUIT FIBO");
	}
}
