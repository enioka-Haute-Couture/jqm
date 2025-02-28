package com.enioka.api.admin;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Data Transfer Object for Class Loader Handler.
 */
@XmlRootElement
public class ClHandlerDto implements Serializable
{

    private static final long serialVersionUID = 1234567890L;

    private Long id;
    private String eventType;
    private String className;
    private String classLoader;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getClassLoader()
    {
        return classLoader;
    }

    public void setClassLoader(String classLoader)
    {
        this.classLoader = classLoader;
    }
}
