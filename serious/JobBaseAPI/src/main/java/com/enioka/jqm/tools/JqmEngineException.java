package com.enioka.jqm.tools;

public class JqmEngineException extends Exception
{
	private static final long serialVersionUID = -5834325251715846234L;

	public JqmEngineException(String msg)
	{
		super(msg);
	}

	public JqmEngineException(String msg, Exception e)
	{
		super(msg, e);
	}
}
