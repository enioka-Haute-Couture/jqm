package com.enioka.jqm.tools;

/**
 * The generic JQM exception class.
 * 
 */
public class JqmKillException extends RuntimeException
{
	private static final long serialVersionUID = -2937310125732117976L;

	public JqmKillException(String msg)
	{
		super(msg);
	}

	public JqmKillException(String msg, Exception e)
	{
		super(msg, e);
	}
}
