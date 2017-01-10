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
package com.enioka.jqm.webui.admin.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobDefDto implements Serializable
{
    private static final long serialVersionUID = 4934352148555212325L;

    private Integer id;
    private String description;
    private boolean canBeRestarted = true;
    private String javaClassName;
    private Integer queueId;
    private String applicationName;
    private String application;
    private String module;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private boolean highlander, enabled;
    private String jarPath;
    private Integer reasonableRuntimeLimitMinute;
    private boolean childFirstClassLoader;
    private String hiddenJavaClasses;
    private String specificIsolationContext;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter", type = ParameterDto.class)
    private List<ParameterDto> parameters = new ArrayList<ParameterDto>();

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isCanBeRestarted()
    {
        return canBeRestarted;
    }

    public void setCanBeRestarted(boolean canBeRestarted)
    {
        this.canBeRestarted = canBeRestarted;
    }

    public String getJavaClassName()
    {
        return javaClassName;
    }

    public void setJavaClassName(String javaClassName)
    {
        this.javaClassName = javaClassName;
    }

    public Integer getQueueId()
    {
        return queueId;
    }

    public void setQueueId(Integer queueId)
    {
        this.queueId = queueId;
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    public String getApplication()
    {
        return application;
    }

    public void setApplication(String application)
    {
        this.application = application;
    }

    public String getModule()
    {
        return module;
    }

    public void setModule(String module)
    {
        this.module = module;
    }

    public String getKeyword1()
    {
        return keyword1;
    }

    public void setKeyword1(String keyword1)
    {
        this.keyword1 = keyword1;
    }

    public String getKeyword2()
    {
        return keyword2;
    }

    public void setKeyword2(String keyword2)
    {
        this.keyword2 = keyword2;
    }

    public String getKeyword3()
    {
        return keyword3;
    }

    public void setKeyword3(String keyword3)
    {
        this.keyword3 = keyword3;
    }

    public boolean isHighlander()
    {
        return highlander;
    }

    public void setHighlander(boolean highlander)
    {
        this.highlander = highlander;
    }

    public String getJarPath()
    {
        return jarPath;
    }

    public void setJarPath(String jarPath)
    {
        this.jarPath = jarPath;
    }

    public List<ParameterDto> getParameters()
    {
        return parameters;
    }

    public void setParameters(List<ParameterDto> parameters)
    {
        this.parameters = parameters;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

	public Integer getReasonableRuntimeLimitMinute() 
	{
		return reasonableRuntimeLimitMinute;
	}

	public void setReasonableRuntimeLimitMinute(Integer reasonableRuntimeLimitMinute) 
	{
		this.reasonableRuntimeLimitMinute = reasonableRuntimeLimitMinute;
	}

	public boolean isChildFirstClassLoader()
	{
		return childFirstClassLoader;
	}

	public void setChildFirstClassLoader(boolean childFirstClassLoader)
	{
		this.childFirstClassLoader = childFirstClassLoader;
	}

	public String getHiddenJavaClasses()
	{
		return hiddenJavaClasses;
	}

	public void setHiddenJavaClasses(String hiddenJavaClasses)
	{
		this.hiddenJavaClasses = hiddenJavaClasses;
	}

	public String getSpecificIsolationContext()
	{
		return specificIsolationContext;
	}

	public void setSpecificIsolationContext(String specificIsolationContext)
	{
		this.specificIsolationContext = specificIsolationContext;
	}
}
