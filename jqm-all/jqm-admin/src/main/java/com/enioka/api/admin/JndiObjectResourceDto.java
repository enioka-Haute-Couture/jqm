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
    /**
     * Create a new empty DTO.
     */
    public JndiObjectResourceDto()
    {}

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

    /**
     * Get the technical ID.
     *
     * @return the id
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Set the technical ID.
     *
     * @param id
     *            the id to set
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Get the name of this resource.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of this resource.
     *
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Get the authentication type.
     *
     * @return the auth
     */
    public String getAuth()
    {
        return auth;
    }

    /**
     * Set the authentication type.
     *
     * @param auth
     *            the auth to set
     */
    public void setAuth(String auth)
    {
        this.auth = auth;
    }

    /**
     * Get the class type of the resource.
     *
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Set the class type of the resource.
     *
     * @param type
     *            the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Get the factory class name.
     *
     * @return the factory
     */
    public String getFactory()
    {
        return factory;
    }

    /**
     * Set the factory class name.
     *
     * @param factory
     *            the factory to set
     */
    public void setFactory(String factory)
    {
        this.factory = factory;
    }

    /**
     * Get the resource description.
     *
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the resource description.
     *
     * @param description
     *            the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Get whether this resource is a singleton.
     *
     * @return true if singleton
     */
    public Boolean getSingleton()
    {
        return singleton;
    }

    /**
     * Set whether this resource is a singleton.
     *
     * @param singleton
     *            true if singleton
     */
    public void setSingleton(Boolean singleton)
    {
        this.singleton = singleton;
    }

    /**
     * Internal setter for the parameters map.
     *
     * @param parameters
     *            the parameters to set
     */
    void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Add a configuration parameter.
     *
     * @param key
     *            parameter name
     * @param value
     *            parameter value
     */
    public void addParameter(String key, String value)
    {
        this.parameters.put(key, value);
    }

    /**
     * Remove a configuration parameter.
     *
     * @param key
     *            parameter name to remove
     */
    public void removeParameter(String key)
    {
        this.parameters.remove(key);
    }

    /**
     * Get all configuration parameters.
     *
     * @return the parameters map
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * Get the template name used for this resource.
     *
     * @return the template
     */
    public String getTemplate()
    {
        return template;
    }

    /**
     * Set the template name used for this resource.
     *
     * @param template
     *            the template to set
     */
    public void setTemplate(String template)
    {
        this.template = template;
    }
}
