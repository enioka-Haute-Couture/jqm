package com.enioka.jqm.api;

/**
 * The generic JQM exception class.
 * 
 */
public class JqmException extends RuntimeException
{
	private static final long serialVersionUID = -2937310125732117976L;

	public JqmException(String msg)
	{
		super(msg);
	}

	public JqmException(String msg, Exception e)
	{
		super(msg, e);
	}
}
