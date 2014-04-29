package com.enioka.jqm.tools;

/**
 * Handler of POSIX-like TERM/INT signals: gracefully shut down.
 */
class SignalHandler extends Thread
{
    private JqmEngine e = null;

    SignalHandler(JqmEngine e)
    {
        this.e = e;
    }

    @Override
    public void run()
    {
        e.stop();
    }

}
