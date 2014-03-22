package com.enioka.jqm.tools;

/**
 * Handler of POSIX-like TERM/INT signals: gracefully shut down.
 */
public class SignalHandler extends Thread
{
    private JqmEngine e = null;

    public SignalHandler(JqmEngine e)
    {
        this.e = e;
    }

    @Override
    public void run()
    {
        e.stop();
    }

}
