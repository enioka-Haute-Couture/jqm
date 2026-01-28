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
    /**
     * Create a new empty DTO.
     */
    public JobDefDto(){}

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

    /**
     * Get the technical ID.
     * @return the ID
     */
    public Long getId()
    {
        return id;
    }

    /**
     * Set the technical ID.
     * @param id the ID to set
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Get the description of the job definition.
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the description of the job definition.
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Check if job instances from this definition can be restarted after a failure.
     * @return true if restartable
     */
    public boolean isCanBeRestarted()
    {
        return canBeRestarted;
    }

    /**
     * Set if job instances from this definition can be restarted after a failure.
     * @param canBeRestarted true if restartable
     */
    public void setCanBeRestarted(boolean canBeRestarted)
    {
        this.canBeRestarted = canBeRestarted;
    }

    /**
     * Get the name of the Java class to execute.
     * @return the class name
     */
    public String getJavaClassName()
    {
        return javaClassName;
    }

    /**
     * Set the name of the Java class to execute.
     * @param javaClassName the class name to set
     */
    public void setJavaClassName(String javaClassName)
    {
        this.javaClassName = javaClassName;
    }

    /**
     * Get the ID of the default queue for this job.
     * @return the queue ID
     */
    public Long getQueueId()
    {
        return queueId;
    }

    /**
     * Set the ID of the default queue for this job.
     * @param queueId the queue ID to set
     */
    public void setQueueId(Long queueId)
    {
        this.queueId = queueId;
    }

    /**
     * Get the application name.
     * @return the application name
     */
    public String getApplicationName()
    {
        return applicationName;
    }

    /**
     * Set the application name.
     * @param applicationName the name to set
     */
    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    /**
     * Get the application.
     * @return the application
     */
    public String getApplication()
    {
        return application;
    }

    /**
     * Set the application.
     * @param application the application to set
     */
    public void setApplication(String application)
    {
        this.application = application;
    }

    /**
     * Get the module name.
     * @return the module
     */
    public String getModule()
    {
        return module;
    }

    /**
     * Set the module name.
     * @param module the module to set
     */
    public void setModule(String module)
    {
        this.module = module;
    }

    /**
     * Get the first metadata keyword.
     * @return keyword1
     */
    public String getKeyword1()
    {
        return keyword1;
    }

    /**
     * Set the first metadata keyword.
     * @param keyword1 keyword to set
     */
    public void setKeyword1(String keyword1)
    {
        this.keyword1 = keyword1;
    }

    /**
     * Get the second metadata keyword.
     * @return keyword2
     */
    public String getKeyword2()
    {
        return keyword2;
    }

    /**
     * Set the second metadata keyword.
     * @param keyword2 keyword to set
     */
    public void setKeyword2(String keyword2)
    {
        this.keyword2 = keyword2;
    }

    /**
     * Get the third metadata keyword.
     * @return keyword3
     */
    public String getKeyword3()
    {
        return keyword3;
    }

    /**
     * Set the third metadata keyword.
     * @param keyword3 keyword to set
     */
    public void setKeyword3(String keyword3)
    {
        this.keyword3 = keyword3;
    }

    /**
     * Check if "Highlander" mode is enabled (only one instance running at a time for this definition).
     * @return true if highlander
     */
    public boolean isHighlander()
    {
        return highlander;
    }

    /**
     * Set if "Highlander" mode is enabled.
     * @param highlander true if highlander
     */
    public void setHighlander(boolean highlander)
    {
        this.highlander = highlander;
    }

    /**
     * Get the path to the JAR file containing the class.
     * @return the jar path
     */
    public String getJarPath()
    {
        return jarPath;
    }

    /**
     * Set the path to the JAR file containing the class.
     * @param jarPath the path to set
     */
    public void setJarPath(String jarPath)
    {
        this.jarPath = jarPath;
    }

    /**
     * Get the type of the path.
     * @return the path type
     */
    public String getPathType()
    {
        return pathType;
    }

    /**
     * Set the type of the path.
     * @param pathType the type to set
     */
    public void setPathType(String pathType)
    {
        this.pathType = pathType;
    }

    /**
     * Get the default parameters for this job definition.
     * @return the parameters map
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * Set the default parameters for this job definition.
     * @param parameters the map to set
     */
    public void setParameters(Map<String, String> parameters)
    {
        this.parameters = parameters;
    }

    /**
     * Check if this job definition is enabled and can be launched.
     * @return true if enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Set if this job definition is enabled.
     * @param enabled true if enabled
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Get the maximum expected runtime in minutes before a warning is triggered.
     * @return the limit in minutes
     */
    public Integer getReasonableRuntimeLimitMinute()
    {
        return reasonableRuntimeLimitMinute;
    }

    /**
     * Set the maximum expected runtime in minutes before a warning is triggered.
     * @param reasonableRuntimeLimitMinute the limit to set
     */
    public void setReasonableRuntimeLimitMinute(Integer reasonableRuntimeLimitMinute)
    {
        this.reasonableRuntimeLimitMinute = reasonableRuntimeLimitMinute;
    }

    /**
     * Get the ID of the classloader to use for this job.
     * @return the classloader ID
     */
    public Long getClassLoaderId()
    {
        return this.classLoaderId;
    }

    /**
     * Get the list of schedules associated with this job.
     * @return the list of schedules
     */
    public List<ScheduledJob> getSchedules()
    {
        return schedules;
    }

    /**
     * Set the list of schedules associated with this job.
     * @param schedules the list to set
     */
    public void setSchedules(List<ScheduledJob> schedules)
    {
        this.schedules = schedules;
    }

    /**
     * Set the ID of the classloader to use for this job.
     * @param classLoaderId the ID to set
     */
    public void setClassLoaderId(Long classLoaderId)
    {
        this.classLoaderId = classLoaderId;
    }

    /**
     * Add a schedule to this job definition.
     * @param sj the schedule to add
     * @return the current DTO for chaining
     */
    public JobDefDto addSchedule(ScheduledJob sj)
    {
        this.schedules.add(sj);
        return this;
    }
}
