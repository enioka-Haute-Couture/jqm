package com.enioka.jqm.runner.api;

import java.util.Map;

import com.enioka.jqm.jdbc.DbConn;

/**
 * A way to communicate between a runner and its caller.
 */
public interface JobRunnerCallback
{
    /**
     * Should the runner use JMX?
     *
     * @return boolean
     */
    public boolean isJmxEnabled();

    /**
     * Generates the name the runner should use as a JMX bean if it registers a bean.
     *
     * @return String
     */
    public String getJmxBeanName();

    /**
     * Uses the standard client API to mark the JI as killed. Does nothing more.
     */
    public void killThroughClientApi();

    /**
     * Fetches a more or less accurate current run time of the running JI.
     *
     * @return Long
     */
    public Long getRunTimeSeconds();

    /**
     * This CL contains /ext and has the bootstrap CL as parent.
     *
     * @return ClassLoader
     */
    public ClassLoader getExtensionClassloader();

    /**
     * When running in JPMS mode, this is the module layer containing the extensions.
     *
     * @return ModuleLayer
     */
    public ModuleLayer getExtensionModuleLayer();

    /**
     * This is the normal CL of the engine. It should never be visible to payloads.
     *
     * @return ClassLoader
     */
    public ClassLoader getEngineClassloader();

    /**
     * A login/password able to use the web APIs.
     *
     * @param cnx
     *            db connection
     * @return login/password pair
     */
    public Map.Entry<String, String> getWebApiUser(DbConn cnx);

    /**
     * How to contact the WS.
     *
     * @param cnx
     *            db connection
     * @return url String
     */
    public String getWebApiLocalUrl(DbConn cnx);
}
