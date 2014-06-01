package com.enioka.jqm.webui.admin.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PemissionsBagDto
{
    @XmlElementWrapper(name = "permissions")
    @XmlElement(name = "permission", type = String.class)
    public List<String> permissions;
}
