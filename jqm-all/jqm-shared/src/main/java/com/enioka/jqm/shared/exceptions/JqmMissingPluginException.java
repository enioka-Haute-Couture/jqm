package com.enioka.jqm.shared.exceptions;

public class JqmMissingPluginException extends JqmRuntimeException
{
    public JqmMissingPluginException(String msg)
    {
        super(msg);
    }

    public JqmMissingPluginException(String msg, Throwable e)
    {
        super(msg, e);
    }

    public JqmMissingPluginException(Class<?> clazz)
    {
        super("no plugin found for type " + clazz.getCanonicalName());
    }
}
