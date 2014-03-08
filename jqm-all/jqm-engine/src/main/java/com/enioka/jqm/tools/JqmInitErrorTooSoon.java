package com.enioka.jqm.tools;

/**
 * An error denotes something fatal and should never be caught.
 * 
 * @author Marc-Antoine
 * 
 */
class JqmInitErrorTooSoon extends JqmInitError
{
    private static final long serialVersionUID = -5993404045975869943L;

    JqmInitErrorTooSoon(String msg)
    {
        super(msg);
    }

    JqmInitErrorTooSoon(String msg, Exception e)
    {
        super(msg, e);
    }
}
