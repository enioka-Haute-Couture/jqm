package com.enioka.jqm.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for storing the definition of Resource Managers.
 */
public class ResourceManager implements Serializable
{
    private static final long serialVersionUID = 7445537437816067909L;

    private Long id = null;
    private String key;
    private boolean enabled = true;
    private String className;
    private Long node = null;
    private Long deploymentParameter = null;

    private Map<String, String> parameterCache = null;

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public Long getId()
    {
        return id;
    }

    /**
     * See {@link #getId()}
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * The key is not necesseraly unique. It is used in the path of input parameter and output value names.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * See {@link #getKey()}
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * The class of the resource manager to create
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * See {@link #getClassName()}
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /**
     * If this is set, this RM is at the node level, i.e. the resources it manages are shared by all queue pollers which use it. If set,
     * {@link #getDeploymentParameterId()} must be null.
     */
    public Long getNodeId()
    {
        return node;
    }

    /**
     * See {@link #getNodeId()}
     */
    public void setNodeId(Long node)
    {
        this.node = node;
    }

    /**
     * If set, the RM is specific to a queue poller - there is no resource sharing with other queue pollers. if set, {@link #getNodeId()}
     * must be null.
     */
    public Long getDeploymentParameterId()
    {
        return deploymentParameter;
    }

    /**
     * See {@link #getDeploymentParameterId()}
     */
    public void setDeploymentParameterId(Long deploymentParameter)
    {
        this.deploymentParameter = deploymentParameter;
    }

    /**
     * A disabled RM always agrees to launch job instances. An enabled RM actually applies its own internal rules to decide if a JI should
     * launch.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * See {@link #isEnabled()}
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * A pre-loaded set of the parameters associated to the RM. May be null if not loaded.
     */
    public Map<String, String> getParameterCache()
    {
        return parameterCache;
    }

    public void addParameter(String key, String value)
    {
        if (parameterCache == null)
        {
            parameterCache = new HashMap<>(1);
        }
        parameterCache.put(key, value);
    }
}
