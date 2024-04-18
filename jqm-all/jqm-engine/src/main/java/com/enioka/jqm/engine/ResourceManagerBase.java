package com.enioka.jqm.engine;

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.ResourceManager;

/**
 * Base class for all resource managers (RM). RMs are responsible for deciding if a given {@link com.enioka.jqm.model.State#SUBMITTED}
 * {@link com.enioka.jqm.model.JobInstance} can actually run according to rules which are specific to the RM.<br>
 * <br>
 * All methods must be thread-safe.
 */
public abstract class ResourceManagerBase
{
    /**
     * The configuration object associated with this RM. Note its values can be overloaded in many places.
     */
    protected ResourceManager definition;

    /**
     * Properties after loading default properties and parameters from the RM's own definition.
     */
    protected Map<String, String> currentProperties;

    /**
     * The key used to identity this specific instance of a RM class. May be used in parameter keys, and is used in the output values keys.
     */
    protected String key;

    /**
     * Main constructor.
     *
     * @param rm
     *            the configuration to use to instanciate the RM.
     */
    ResourceManagerBase(ResourceManager rm)
    {
        this.definition = rm;
    }

    /**
     * The main method of a RM: decide if a job can run. This should try to book the resources. {@link JobInstance} parameters specific to
     * this RM (if any) should be removed by this method. Once this method has run, booked resources should not be available to anyone else
     * until either:
     * <ul>
     * <li>released at the end of the run - {@link #releaseResource(JobInstance)} - this is the nominal case</li>
     * <li>released if the engine later decides not to run the JI for some reason (a different resource manager may refuse for exemple) -
     * {@link #rollbackResourceBooking(JobInstance, DbConn)}</li>
     * <li>released if the engine decides to run the job instance, but the Resource Manager wishes to release the resource at once because
     * they are only useful during polling, with no need to withold them during the job instance run.
     * {@link #commitResourceBooking(JobInstance, DbConn)}</li>
     * <ul>
     *
     * @param ji
     * @return
     */
    abstract BookingStatus bookResource(JobInstance ji, DbConn cnx);

    /**
     * Allow the RM to take operations to cancel any resource reservation and the like. <br>
     * Called when the JobInstance has finished running.<br>
     * Default implementation does nothing.
     *
     * @param ji
     */
    void releaseResource(JobInstance ji)
    {}

    /**
     * Called when the engine has decided to actually run the job instance.<br>
     * Either this or {@link #rollbackResourceBooking(JobInstance, DbConn)} are called when the engine has made a decision.<br>
     * Default implementation does nothing. This method should never fail. Risky operations are done in bookResource.
     */
    void commitResourceBooking(JobInstance ji, DbConn cnx)
    {}

    /**
     * Called when the engine has decided to actually *not* run the job instance.<br>
     * Either this or {@link #commitResourceBooking(JobInstance, DbConn)} are called when the engine has made a decision.<br>
     * Default implementation calls {@link #releaseResource(JobInstance)} (which does nothing by default).<br>
     * This method should never fail. Risky operations are done in bookResource.
     */
    void rollbackResourceBooking(JobInstance ji, DbConn cnx)
    {
        this.releaseResource(ji);
    }

    /**
     * According to this resource manager, how many job instances could now be launched? This should be a very fast approximation with
     * reasonable hypothesis (which may use parameters). Do not implement if not compatible with this type of resource.
     *
     * @return
     */
    int getSlotsAvailable()
    {
        return Integer.MAX_VALUE;
    }

    /**
     * The prefix for all parameter names. Default is empty.
     *
     * @return
     */
    String getParameterRoot()
    {
        return "";
    }

    protected void setDefaultProperties()
    {}

    /**
     * Called by the engine when it has decided configuration has changed. Care should be taken to ensure continuity of operation despite
     * configuration change - resource amount currently in used should not be reset for example.<br>
     * Note that the RM key can change, but never the RM ID.<br>
     * <br>
     * This will often be overloaded - but calling the base implementation may still be useful as it handles configuration precedence.
     */
    void refreshConfiguration(ResourceManager configuration)
    {
        this.definition = configuration;
        this.key = configuration.getKey().toLowerCase();
        this.currentProperties = new HashMap<>();

        // Add hard-coded defaults to properties
        setDefaultProperties();

        // Add values from new configuration to properties
        this.currentProperties.putAll(configuration.getParameterCache());
    }

    /**
     * Apply precedence rules for parameter value resolution: JI parameter using RM key > JI parameter using generic RM name without key >
     * JD > RM > defaults
     *
     * @param key
     *            the end of the parameter key. getParameterRoot()[.key] is automatically prefixed.
     * @param ji
     *            the analysed job instance
     * @param pop
     *            if true, parameter will be removed from the JI prm list
     * @return
     */
    protected String getStringParameter(String key, JobInstance ji, boolean pop)
    {
        // First try: parameter name with specific RM key.
        String res = ji.getPrms().get(getParameterRoot() + this.key + "." + key);
        if (res != null && pop)
        {
            ji.getPrms().remove(getParameterRoot() + this.key + "." + key);
        }

        // Second try: parameter name without specific key (used by all RM of this type)
        if (res == null)
        {
            res = ji.getPrms().get(getParameterRoot() + key);
            if (res != null && pop)
            {
                ji.getPrms().remove(getParameterRoot() + key);
            }
        }

        // Third try: just use value from RM configuration (which may be a hard-coded default).
        if (res == null)
        {
            res = this.currentProperties.get(getParameterRoot() + key);
        }

        // Go. Third try is by convention always supposed to succeed - all keys should have a default value.
        return res;
    }

    protected String getStringParameter(String key, JobInstance ji)
    {
        return getStringParameter(key, ji, false);
    }

    protected Integer getIntegerParameter(String key, JobInstance ji)
    {
        return getIntegerParameter(key, ji, false);
    }

    protected Integer getIntegerParameter(String key, JobInstance ji, boolean pop)
    {
        return Integer.parseInt(getStringParameter(key, ji, pop));
    }

    protected String getStringParameter(String key)
    {
        return this.currentProperties.get(getParameterRoot() + key);
    }

    protected Integer getIntegerParameter(String key)
    {
        return Integer.parseInt(getStringParameter(key));
    }

    /**
     * The result of a booking request.
     */
    enum BookingStatus {
        /**
         * The necessary resources have been booked - they are not available to anyone else until released.
         */
        BOOKED,
        /**
         * The necessary resources are not available. Furthermore, immediate resources for any amount of resource will likely fail and the
         * next elements in queue should not be evaluated during this polling cycle.
         */
        EXHAUSTED,
        /**
         * The necessary resources are not available. However, there are still a few units available - the current JI was simply too greedy.
         * Next items in queue may be evaluated.
         */
        FAILED,
    }
}
