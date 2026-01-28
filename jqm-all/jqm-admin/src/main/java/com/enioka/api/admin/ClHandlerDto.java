package com.enioka.api.admin;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Data Transfer Object for Class Loader Handler.
 */
@XmlRootElement
public class ClHandlerDto implements Serializable
{
    /**
     * Create a new empty DTO.
     */
    public ClHandlerDto(){}

    private static final long serialVersionUID = 1234567890L;

    private Long id;
    private String eventType;
    private String className;
    private String classLoader;

    /**
     * Get the ID of the handler.
     * @return the ID.
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Set the ID of the handler.
     * @param id the ID to set
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Get the event type handled by this handler.
     * @return the event type
     */
    public String getEventType()
    {
        return eventType;
    }

    /**
     * Set the event type handled by this handler.
     * @param eventType the event type to set
     */
    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    /**
     * Get the class name of the handler.
     * @return the class name
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * Set the class name of the handler.
     * @param className the class name to set
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /**
     * Get the class loader this handler belongs to.
     * @return the class loader
     */
    public String getClassLoader()
    {
        return classLoader;
    }

    /**
     * Set the class loader this handler belongs to.
     * @param classLoader the class loader to set
     */
    public void setClassLoader(String classLoader)
    {
        this.classLoader = classLoader;
    }
}
