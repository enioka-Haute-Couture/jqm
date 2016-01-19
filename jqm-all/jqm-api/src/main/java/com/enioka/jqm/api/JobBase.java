/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
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

package com.enioka.jqm.api;

import java.io.IOException;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * @deprecated Use {@link JobManager} instead.
 */
public class JobBase
{
    protected JobManager jm;

    public void start()
    {

    }

    public void stop()
    {

    }

    public DataSource getDefaultConnection() throws NamingException
    {
        return jm.getDefaultConnection();
    }

    public void addDeliverable(String path, String fileLabel) throws IOException
    {
        jm.addDeliverable(path, fileLabel);
    }

    public void sendMsg(String msg)
    {
        jm.sendMsg(msg);
    }

    public void sendProgress(Integer progress)
    {
        jm.sendProgress(progress);
    }

    public int enQueue(String applicationName, String user, String mail, String sessionID, String application, String module,
            String keyword1, String keyword2, String keyword3, Map<String, String> parameters)
    {
        return jm.enqueue(applicationName, user, mail, sessionID, application, module, keyword1, keyword2, keyword3, parameters);
    }

    public int enQueueSynchronously(String applicationName, String user, String mail, String sessionID, String application, String module,
            String keyword1, String keyword2, String keyword3, Map<String, String> parameters)
    {
        return jm.enqueueSync(applicationName, user, mail, sessionID, application, module, keyword1, keyword2, keyword3, parameters);
    }

    // ---------

    public Integer getParentID()
    {
        return jm.parentID();
    }

    public boolean canBeRestarted()
    {
        return jm.canBeRestarted();
    }

    public String getApplicationName()
    {
        return jm.applicationName();
    }

    public String getSessionID()
    {
        return jm.sessionID();
    }

    public String getApplication()
    {
        return jm.application();
    }

    public String getModule()
    {
        return jm.module();
    }

    public String getkeyword1()
    {
        return jm.keyword1();
    }

    public String getKeyword2()
    {
        return jm.keyword2();
    }

    public String getKeyword3()
    {
        return jm.keyword3();
    }
    
    public String getDefinitionKeyword1()
    {
        return jm.definitionKeyword1();
    }
    
    public String getDefinitionKeyword2()
    {
        return jm.definitionKeyword2();
    }
    
    public String getDefinitionKeyword3()
    {
        return jm.definitionKeyword3();
    }

    public Map<String, String> getParameters()
    {
        return jm.parameters();
    }

    public String getDefaultConnect()
    {
        return jm.defaultConnect();
    }

    public Integer getJobInstanceId()
    {
        return jm.jobInstanceID();
    }
}
