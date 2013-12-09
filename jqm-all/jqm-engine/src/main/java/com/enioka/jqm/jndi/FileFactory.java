package com.enioka.jqm.jndi;

import java.io.File;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

public class FileFactory implements ObjectFactory
{
	public FileFactory()
	{

	}

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
	{
		JndiResourceDescriptor resource = (JndiResourceDescriptor) obj;
		if (resource.get("PATH") != null)
		{
			String path = (String) resource.get("PATH").getContent();
			return new File(path);
		}
		else
		{
			throw new NamingException("Resource does not have a valid PATH parameter");
		}
	}
}
