package com.enioka.jqm.api;

public class JqmApiException extends RuntimeException
{
	private static final long serialVersionUID = -2937910125732117976L;

	public JqmApiException(String msg)
	{
		super(msg);
	}

	public JqmApiException(String msg, Exception e)
	{
		super(msg, e);
	}
}
