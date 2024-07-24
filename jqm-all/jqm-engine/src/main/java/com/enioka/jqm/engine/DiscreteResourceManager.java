package com.enioka.jqm.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.JobInstance;
import com.enioka.jqm.model.ResourceManager;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Resource Manager in which the resource is a list of items without order. Each JI may take 0 to n items. Items are attributed nominaly -
 * they are named. An environment variable of all attributed items is made available to the JI for when it runs. <br>
 * <br>
 * Blocks once the resource is exhausted.<br>
 * This RM has no persistence - resources are counted in memory, and considered free on startup.<br>
 * By default it has 10 items named item01 to item10, and JI do NOT use any of them.
 */
public class DiscreteResourceManager extends ResourceManagerBase
{
    private static Logger jqmlogger = LoggerFactory.getLogger(QuantityResourceManager.class);

    private static String PRM_ROOT = "com.enioka.jqm.rm.discrete.";
    private static String PRM_LIST = "list";
    private static String PRM_CONSUMPTION = "consumption";

    /**
     * The tokens, indexed by name. Boolean is true if available, false if booked.
     */
    private Map<String, Token> tokenRepository = new ConcurrentHashMap<>(10);

    private class Token
    {
        private AtomicBoolean free = new AtomicBoolean(true);
        private long jiId = 0L;
    }

    private int defaultConsumption;

    DiscreteResourceManager(ResourceManager rm)
    {
        super(rm);
    }

    @Override
    protected void setDefaultProperties()
    {
        this.currentProperties.put(PRM_ROOT + PRM_LIST, "item01,item02,item03,item04,item05,item06,item07,item08,item09,item10");
        this.currentProperties.put(PRM_ROOT + PRM_CONSUMPTION, "0");
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
        String[] newItems = getStringParameter(PRM_LIST).split(",");

        // Remove items absent from configuration
        List<String> toRemove = new ArrayList<>(); // Cannot directly modify map we iterate on.
        for (String key : this.tokenRepository.keySet())
        {
            boolean found = false;
            for (String newItem : newItems)
            {
                if (newItem.equals(key))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                toRemove.add(key);
            }
        }
        for (String key : toRemove)
        {
            this.tokenRepository.remove(key);
        }

        // Add new items
        for (String newItem : newItems)
        {
            if (!tokenRepository.containsKey(newItem))
            {
                this.tokenRepository.put(newItem, new Token());
            }
        }

        // Helpers
        this.defaultConsumption = getIntegerParameter(PRM_CONSUMPTION);

        // Log
        jqmlogger.info("\tConfigured discrete resource manager [{}] with {} items - currently free {} - taking {} per JI by default",
                this.key, this.tokenRepository.size(), getSlotsAvailable(), this.defaultConsumption);
    }

    @Override
    BookingStatus bookResource(JobInstance ji, DbConn cnx)
    {
        int slots = this.getIntegerParameter(PRM_CONSUMPTION, ji, true);
        if (slots == 0)
        {
            return BookingStatus.BOOKED; // Perf optim.
        }

        Map<String, Token> booked = new HashMap<>();
        for (Map.Entry<String, Token> e : this.tokenRepository.entrySet())
        {
            if (e.getValue().free.compareAndSet(true, false))
            {
                booked.put(e.getKey(), e.getValue());
                e.getValue().jiId = ji.getId();
            }
            if (booked.size() == slots)
            {
                break;
            }
        }

        if (booked.size() != slots)
        {
            // Failure.
            if (booked.size() > 0)
            {
                for (Token e : booked.values())
                {
                    e.free.set(true);
                }
                return BookingStatus.FAILED; // If here there are items available, just not enough.
            }
            return BookingStatus.EXHAUSTED;
        }
        ji.addEnvVar(String.format("JQM_RM_DISCRETE_%s_ITEMS", this.key.toUpperCase()), StringUtils.join(booked.keySet(), ","));
        jqmlogger.debug("Booking {} items for RM {}", booked.size(), this.key);
        return BookingStatus.BOOKED;
    }

    @Override
    void releaseResource(JobInstance ji)
    {
        int released = 0;
        for (Token e : this.tokenRepository.values())
        {
            if (!e.free.get() && e.jiId == ji.getId())
            {
                e.free.set(true);
                released++;
            }
        }

        jqmlogger.debug("Releasing {} items for RM {}", released, this.key);
    }

    @Override
    int getSlotsAvailable()
    {
        int availableItems = 0;
        for (Map.Entry<String, Token> e : this.tokenRepository.entrySet())
        {
            if (e.getValue().free.get())
            {
                availableItems++;
            }
        }
        return availableItems / (this.defaultConsumption > 0 ? this.defaultConsumption : 1);
    }
}
