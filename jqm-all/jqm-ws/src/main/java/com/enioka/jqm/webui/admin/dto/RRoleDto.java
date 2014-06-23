package com.enioka.jqm.webui.admin.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RRoleDto implements Serializable
{
    private static final long serialVersionUID = -3226778067729024400L;

    private Integer id;
    private String name;
    private String description;

    private List<String> permissions = new ArrayList<String>();

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

    public List<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(List<String> permissions)
    {
        this.permissions = permissions;
    }
}
