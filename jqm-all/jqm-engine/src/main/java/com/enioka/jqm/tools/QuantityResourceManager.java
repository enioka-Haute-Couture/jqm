package com.enioka.jqm.tools;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.ResourceManager;

/**
 * A Resource Manager which handles a set quantity of a given resource. By default, each run requires one unit of that resource. Blocks once
 * the resource is exhausted.<br>
 * This RM has no persistence - resources are counted in memory, and considered free on startup.<br>
 * By default it has 10 units and each JI consumes one unit. It is therefore very simple, but can still model things like a thread count, a
 * memory share...
 */
class QuantityResourceManager extends ResourceManagerBase
{
    private static Logger jqmlogger = LoggerFactory.getLogger(QuantityResourceManager.class);

    private static String PRM_ROOT = "com.enioka.jqm.rm.quantity.";
    private static String PRM_QUANTITY = "quantity";
    private static String PRM_CONSUMPTION = "consumption";

    private AtomicInteger availableUnits = new AtomicInteger(0);
    private int previousMaxUnits = 0;

    private int defaultConsumption;

    QuantityResourceManager(ResourceManager rm)
    {
        super(rm);
    }

    @Override
    protected void setDefaultProperties()
    {
        this.currentProperties.put(PRM_ROOT + PRM_QUANTITY, "10");
        this.currentProperties.put(PRM_ROOT + PRM_CONSUMPTION, "1");
    }

    @Override
    String getParameterRoot()
    {
        return PRM_ROOT;
    }

    @Override
    void refreshConfiguration(ResourceManager configuration)
    {
        // Read configuration
        super.refreshConfiguration(configuration);

        // Init the resource itself.
        int newMaxUnits = getIntegerParameter(PRM_QUANTITY);
        int delta = newMaxUnits - previousMaxUnits;
        previousMaxUnits = newMaxUnits;
        this.availableUnits.addAndGet(delta);

        // Helpers
        this.defaultConsumption = getIntegerParameter(PRM_CONSUMPTION);

        // Log
        jqmlogger.info("\tConfigured quantity resource manager [{}] with max count {} - currently free {} - taking {} per JI by default",
                this.key, newMaxUnits, this.availableUnits.intValue(), this.defaultConsumption);
    }

    @Override
    BookingStatus bookResource(JobInstance ji, DbConn cnx)
    {
        int slots = this.getIntegerParameterForInstance(PRM_CONSUMPTION, ji);

        if (availableUnits.addAndGet(-slots) < 0)
        {
            // Put resource back.
            availableUnits.addAndGet(slots);

            // Return booking failure, depending on resources still available.
            return availableUnits.get() >= this.defaultConsumption ? BookingStatus.FAILED : BookingStatus.EXHAUSTED;
        }

        // If here, booking has succeeded.
        return BookingStatus.BOOKED;
    }

    @Override
    void releaseResource(JobInstance ji)
    {
        int slots = this.getIntegerParameterForInstance(PRM_CONSUMPTION, ji);
        jqmlogger.debug("Releasing {} slots for RM {}", slots, this.key);
        availableUnits.addAndGet(slots);
    }

    @Override
    int getSlotsAvailable()
    {
        return this.availableUnits.get() / this.defaultConsumption;
    }
}