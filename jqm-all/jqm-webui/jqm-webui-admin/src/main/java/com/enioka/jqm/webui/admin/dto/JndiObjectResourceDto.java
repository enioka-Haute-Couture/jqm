package com.enioka.jqm.webui.admin.dto;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JndiObjectResourceDto implements Serializable
{
    private static final long serialVersionUID = -8561440788483188421L;

    private Integer id;
    private String name;
    private String auth = "CONTAINER";
    private String type;
    private String factory;
    private String description;
    private Boolean singleton = false;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter", type = JndiObjectResourcePrmDto.class)
    private List<JndiObjectResourcePrmDto> parameters;

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

    public String getAuth()
    {
        return auth;
    }

    public void setAuth(String auth)
    {
        this.auth = auth;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getFactory()
    {
        return factory;
    }

    public void setFactory(String factory)
    {
        this.factory = factory;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Boolean getSingleton()
    {
        return singleton;
    }

    public void setSingleton(Boolean singleton)
    {
        this.singleton = singleton;
    }

    public void setParameters(List<JndiObjectResourcePrmDto> parameters)
    {
        this.parameters = parameters;
    }

    public List<JndiObjectResourcePrmDto> getParameters()
    {
        return parameters;
    }
}
