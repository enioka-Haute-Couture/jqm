package com.enioka.jqm.webui.admin.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JndiObjectResourcePrmDto implements Serializable
{
    private static final long serialVersionUID = -8561440788482185421L;

    private Integer id;
    private String key, value;

    public JndiObjectResourcePrmDto()
    {
        // Java bean convention
    }

    public JndiObjectResourcePrmDto(Integer id, String key, String value)
    {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
