package com.enioka.jqm.cli.bootstrap;

/**
 * Interface implemented by the CLI parsing service. Used as a pivot between the system class loader and the host class loader (usually the
 * one created by jqm-service).
 */
public interface CommandLine
{
    /**
     * Parse command and execute it.
     *
     * @param args
     *            CLI arguments
     * @return return code
     */
    public int runServiceCommand(String[] args);

    /**
     * If an engine is running, kill it.
     */
    public void stopIfRunning();
}
