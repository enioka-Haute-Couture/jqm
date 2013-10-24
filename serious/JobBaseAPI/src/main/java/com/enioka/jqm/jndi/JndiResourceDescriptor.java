package com.enioka.jqm.jndi;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class JndiResourceDescriptor extends Reference
{
	private static final long serialVersionUID = 3348684996519775949L;

	public JndiResourceDescriptor(String resourceClass, String description, String scope, String auth, boolean singleton, String factory,
			String factoryLocation)
	{
		super(resourceClass, factory, factoryLocation);
		StringRefAddr refAddr = null;
		if (description != null)
		{
			refAddr = new StringRefAddr("description", description);
			add(refAddr);
		}
		if (scope != null)
		{
			refAddr = new StringRefAddr("scope", scope);
			add(refAddr);
		}
		if (auth != null)
		{
			refAddr = new StringRefAddr("auth", auth);
			add(refAddr);
		}
		// singleton is a boolean so slightly different handling
		refAddr = new StringRefAddr("singleton", Boolean.toString(singleton));
		add(refAddr);
	}
}
