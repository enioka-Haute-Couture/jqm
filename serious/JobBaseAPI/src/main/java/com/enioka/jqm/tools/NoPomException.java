package com.enioka.jqm.tools;


public class NoPomException extends Exception
{
	private static final long serialVersionUID = -4667925633105269270L;

	public NoPomException(String msg)
	{
		super(msg);
	}

	public NoPomException(String msg, Exception e)
	{
		super(msg, e);
	}
}
