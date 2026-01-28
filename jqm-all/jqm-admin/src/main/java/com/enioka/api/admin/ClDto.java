package com.enioka.api.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Data Transfer Object for Class Loader.
 */
@XmlRootElement
public class ClDto implements Serializable
{

    private static final long serialVersionUID = 1234567890L;

    /**
     * Create a new empty DTO.
     */
    public ClDto(){}

    private Long id;
    private String name;
    private boolean childFirst;
    private String hiddenClasses;
    private boolean tracingEnabled;
    private boolean persistent;
    private String allowedRunners;

    @XmlElementWrapper(name = "handlers")
    @XmlElement(name = "handler")
    private List<ClHandlerDto> handlers = new ArrayList<>();

    /**
     * The id of the classloader.
     * @return the ID
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Set the id of the classloader.
     * @param id the ID to set
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * The name of the classloader.
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the classloader.
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Whether this classloader is child-first or not.
     * @return true if child-first, false otherwise
     */
    public boolean isChildFirst()
    {
        return childFirst;
    }

    /**
     * Set whether this classloader is child-first or not.
     * @param childFirst true if child-first, false otherwise
     */
    public void setChildFirst(boolean childFirst)
    {
        this.childFirst = childFirst;
    }

    /**
     * Get the hidden classes for this classloader.
     * @return the hidden classes
     */
    public String getHiddenClasses()
    {
        return hiddenClasses;
    }

    /**
     * Set the hidden classes for this classloader.
     * @param hiddenClasses the hidden classes to set
     */
    public void setHiddenClasses(String hiddenClasses)
    {
        this.hiddenClasses = hiddenClasses;
    }

    /**
     * Whether tracing is enabled for this classloader.
     * @return true if tracing is enabled, false otherwise
     */
    public boolean isTracingEnabled()
    {
        return tracingEnabled;
    }

    /**
     * Set whether tracing is enabled for this classloader.
     * @param tracingEnabled true if tracing is enabled, false otherwise
     */
    public void setTracingEnabled(boolean tracingEnabled)
    {
        this.tracingEnabled = tracingEnabled;
    }

    /**
     * Whether this classloader is persistent or not.
     * @return true if persistent, false otherwise
     */
    public boolean isPersistent()
    {
        return persistent;
    }

    /**
     * Set whether this classloader is persistent or not.
     * @param persistent true if persistent, false otherwise
     */
    public void setPersistent(boolean persistent)
    {
        this.persistent = persistent;
    }

    /**
     * Get the allowed runners for this classloader.
     * @return the allowed runners
     */
    public String getAllowedRunners()
    {
        return allowedRunners;
    }

    /**
     * Set the allowed runners for this classloader.
     * @param allowedRunners the allowed runners to set
     */
    public void setAllowedRunners(String allowedRunners)
    {
        this.allowedRunners = allowedRunners;
    }

    /**
     * Get the handlers for this classloader.
     * @return the handlers
     */
    public List<ClHandlerDto> getHandlers()
    {
        return handlers;
    }

    /**
     * Set the handlers for this classloader.
     * @param handlers the handlers to set
     */
    public void setHandlers(List<ClHandlerDto> handlers)
    {
        this.handlers = handlers;
    }
}
