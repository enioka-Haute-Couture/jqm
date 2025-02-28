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

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isChildFirst()
    {
        return childFirst;
    }

    public void setChildFirst(boolean childFirst)
    {
        this.childFirst = childFirst;
    }

    public String getHiddenClasses()
    {
        return hiddenClasses;
    }

    public void setHiddenClasses(String hiddenClasses)
    {
        this.hiddenClasses = hiddenClasses;
    }

    public boolean isTracingEnabled()
    {
        return tracingEnabled;
    }

    public void setTracingEnabled(boolean tracingEnabled)
    {
        this.tracingEnabled = tracingEnabled;
    }

    public boolean isPersistent()
    {
        return persistent;
    }

    public void setPersistent(boolean persistent)
    {
        this.persistent = persistent;
    }

    public String getAllowedRunners()
    {
        return allowedRunners;
    }

    public void setAllowedRunners(String allowedRunners)
    {
        this.allowedRunners = allowedRunners;
    }

    public List<ClHandlerDto> getHandlers()
    {
        return handlers;
    }

    public void setHandlers(List<ClHandlerDto> handlers)
    {
        this.handlers = handlers;
    }
}
