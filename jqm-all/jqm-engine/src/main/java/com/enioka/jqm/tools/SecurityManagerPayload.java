package com.enioka.jqm.tools;

import java.security.Permission;

@SuppressWarnings("rawtypes")
public class SecurityManagerPayload extends SecurityManager
{
	/**
	 * Default is: everything is allowed.
	 */
	@Override
	public void checkPermission(Permission perm)
	{

	}

	@Override
	public void checkExit(int status)
	{
		Class stack[] = getClassContext();
		if (stack.length > 3 && stack[3].getClassLoader() instanceof JarClassLoader)
		{
			throw new SecurityException("JQM payloads cannot call System.exit() - this would stop JQM itself!");
		}
	}
}
