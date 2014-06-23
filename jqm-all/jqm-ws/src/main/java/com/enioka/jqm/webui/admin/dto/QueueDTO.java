package com.enioka.jqm.webui.admin.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QueueDTO implements Serializable
{
    private static final long serialVersionUID = 4677043929807285233L;

    private Integer id;
    private String name;
    private String description;
    private boolean defaultQueue;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isDefaultQueue()
    {
        return defaultQueue;
    }

    public void setDefaultQueue(boolean defaultQueue)
    {
        this.defaultQueue = defaultQueue;
    }
}
