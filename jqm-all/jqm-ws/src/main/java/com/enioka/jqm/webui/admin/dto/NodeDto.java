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
import java.util.Calendar;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NodeDto implements Serializable
{
    private static final long serialVersionUID = -3592156944832541035L;

    private Integer id;
    private String name;
    private String dns;
    private Integer port;
    private String outputDirectory, tmpDirectory;
    private String jobRepoDirectory;
    private String rootLogLevel;
    private Calendar lastSeenAlive;
    private Integer jmxRegistryPort;
    private Integer jmxServerPort;
    private Boolean stop = false, enabled = true;
    private Boolean loapApiSimple, loadApiClient, loadApiAdmin;
    private Boolean reportsRunning;

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

    public String getDns()
    {
        return dns;
    }

    public void setDns(String dns)
    {
        this.dns = dns;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public String getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }

    public String getJobRepoDirectory()
    {
        return jobRepoDirectory;
    }

    public void setJobRepoDirectory(String jobRepoDirectory)
    {
        this.jobRepoDirectory = jobRepoDirectory;
    }

    public String getRootLogLevel()
    {
        return rootLogLevel;
    }

    public void setRootLogLevel(String rootLogLevel)
    {
        this.rootLogLevel = rootLogLevel;
    }

    public Calendar getLastSeenAlive()
    {
        return lastSeenAlive;
    }

    public void setLastSeenAlive(Calendar lastSeenAlive)
    {
        this.lastSeenAlive = lastSeenAlive;
    }

    public Integer getJmxRegistryPort()
    {
        return jmxRegistryPort;
    }

    public void setJmxRegistryPort(Integer jmxRegistryPort)
    {
        this.jmxRegistryPort = jmxRegistryPort;
    }

    public Integer getJmxServerPort()
    {
        return jmxServerPort;
    }

    public void setJmxServerPort(Integer jmxServerPort)
    {
        this.jmxServerPort = jmxServerPort;
    }

    public Boolean getStop()
    {
        return stop;
    }

    public void setStop(Boolean stop)
    {
        this.stop = stop;
    }

    public Boolean getLoapApiSimple()
    {
        return loapApiSimple;
    }

    public void setLoapApiSimple(Boolean loapApiSimple)
    {
        this.loapApiSimple = loapApiSimple;
    }

    public Boolean getLoadApiClient()
    {
        return loadApiClient;
    }

    public void setLoadApiClient(Boolean loadApiClient)
    {
        this.loadApiClient = loadApiClient;
    }

    public Boolean getLoadApiAdmin()
    {
        return loadApiAdmin;
    }

    public void setLoadApiAdmin(Boolean loadApiAdmin)
    {
        this.loadApiAdmin = loadApiAdmin;
    }

    public String getTmpDirectory()
    {
        return tmpDirectory;
    }

    public void setTmpDirectory(String tmpDirectory)
    {
        this.tmpDirectory = tmpDirectory;
    }

    public Boolean getReportsRunning()
    {
        return reportsRunning;
    }

    public void setReportsRunning(Boolean reportsRunning)
    {
        this.reportsRunning = reportsRunning;
    }

    public Boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }
}
