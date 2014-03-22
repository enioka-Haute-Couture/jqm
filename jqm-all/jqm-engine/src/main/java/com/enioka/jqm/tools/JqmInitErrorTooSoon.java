package com.enioka.jqm.tools;

/**
 * Thrown when an engine tries to start with a node name associated to a recent lastseenalive date.
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
