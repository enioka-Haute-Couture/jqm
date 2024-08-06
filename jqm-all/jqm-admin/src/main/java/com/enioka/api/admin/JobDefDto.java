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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * A job definition is the template of the job instances (the actual launches). It fully defines what to run (the class to load, the default
 * job instance parameters...) and how to run it (the execution context).
 *
 */
@XmlRootElement
public class JobDefDto implements Serializable
{
    private static final long serialVersionUID = 4934352148555212325L;

    private Long id;
    private String description;
    private boolean canBeRestarted = true;
    private String javaClassName;
    private Long queueId;
    private String applicationName;
    private String application;
    private String module;
    private String keyword1;
    private String keyword2;
    private String keyword3;
    private boolean highlander, enabled;
    private String jarPath;
    private String pathType;
    private Integer reasonableRuntimeLimitMinute;
    private Long classLoaderId;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private Map<String, String> parameters = new HashMap<>();

    private List<ScheduledJob> schedules = new ArrayList<>();

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
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

    public Long getQueueId()
    {
        return queueId;
    }

    public void setQueueId(Long queueId)
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

    public String getPathType()
    {
        return pathType;
    }

    public void setPathType(String pathType)
    {
        this.pathType = pathType;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters)
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

    public Long getClassLoaderId()
    {
        return this.classLoaderId;
    }

    public List<ScheduledJob> getSchedules()
    {
        return schedules;
    }

    public void setSchedules(List<ScheduledJob> schedules)
    {
        this.schedules = schedules;
    }

    public void setClassLoaderId(Long classLoaderId)
    {
        this.classLoaderId = classLoaderId;
    }

    public JobDefDto addSchedule(ScheduledJob sj)
    {
        this.schedules.add(sj);
        return this;
    }
}
