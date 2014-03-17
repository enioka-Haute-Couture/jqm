package com.enioka.jqm.tools;

public class JqmPayloadException extends JqmEngineException
{
    private static final long serialVersionUID = 3909566496696739368L;

    JqmPayloadException(String msg)
    {
        super(msg);
    }

    JqmPayloadException(String msg, Throwable e)
    {
        super(msg, e);
    }

}
