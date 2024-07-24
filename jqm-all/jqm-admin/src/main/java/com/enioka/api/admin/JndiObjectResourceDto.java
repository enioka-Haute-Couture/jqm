/**
 * Copyright © 2013 enioka. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.enioka.api.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Describes a resource which can be retrieved by the running job instances through the JNDI API.
 */
@XmlRootElement
public class JndiObjectResourceDto implements Serializable
{
    private static final long serialVersionUID = -8561440788483188421L;

    private Long id;
    private String name;
    private String auth = "CONTAINER";
    private String type;
    private String factory;
    private String description;
    private Boolean singleton = false;
    private String template = null;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private Map<String, String> parameters = new HashMap<>(10);

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

    void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    public void addParameter(String key, String value)
    {
        this.parameters.put(key, value);
    }

    public void removeParameter(String key)
    {
        this.parameters.remove(key);
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }
}
