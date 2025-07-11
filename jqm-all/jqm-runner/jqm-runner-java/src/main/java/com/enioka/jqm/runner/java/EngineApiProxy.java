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
package com.enioka.jqm.runner.java;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.enioka.jqm.api.JobManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation behind the proxy described in the <code>JobManager</code> interface inside the jqm-api artifact.
 */
public class EngineApiProxy implements InvocationHandler
{
    private static Logger jqmlogger = LoggerFactory.getLogger(EngineApiProxy.class);

    private JobManager api;

    public EngineApiProxy(JobManager api)
    {
        this.api = api;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String methodName = method.getName();
        Class<?>[] classes = method.getParameterTypes();
        jqmlogger.trace("An engine API method was called: " + methodName + " with nb arguments: " + classes.length);

        this.api.yield();

        if (classes.length == 0)
        {
            if ("jobApplicationId".equals(methodName))
            {
                return this.api.jobApplicationId();
            }
            else if ("parentID".equals(methodName))
            {
                return this.api.parentID();
            }
            else if ("jobInstanceID".equals(methodName))
            {
                return this.api.jobInstanceID();
            }
            else if ("canBeRestarted".equals(methodName))
            {
                return this.api.canBeRestarted();
            }
            else if ("applicationName".equals(methodName))
            {
                return this.api.applicationName();
            }
            else if ("sessionID".equals(methodName))
            {
                return this.api.sessionID();
            }
            else if ("application".equals(methodName))
            {
                return this.api.application();
            }
            else if ("module".equals(methodName))
            {
                return this.api.module();
            }
            else if ("keyword1".equals(methodName))
            {
                return this.api.keyword1();
            }
            else if ("keyword2".equals(methodName))
            {
                return this.api.keyword2();
            }
            else if ("keyword3".equals(methodName))
            {
                return this.api.keyword3();
            }
            else if ("definitionKeyword1".equals(methodName))
            {
                return this.api.definitionKeyword1();
            }
            else if ("definitionKeyword2".equals(methodName))
            {
                return this.api.definitionKeyword2();
            }
            else if ("definitionKeyword3".equals(methodName))
            {
                return this.api.definitionKeyword3();
            }
            else if ("userName".equals(methodName))
            {
                return this.api.userName();
            }
            else if ("parameters".equals(methodName))
            {
                return this.api.parameters();
            }
            else if ("defaultConnect".equals(methodName))
            {
                return this.api.defaultConnect();
            }
            else if ("getDefaultConnection".equals(methodName))
            {
                return this.api.getDefaultConnection();
            }
            else if ("getWorkDir".equals(methodName))
            {
                return this.api.getWorkDir();
            }
            else if ("yield".equals(methodName))
            {
                return null; // already done.
            }
            else if ("waitChildren".equals(methodName))
            {
                this.api.waitChildren();
                return null;
            }
        }
        else if ("sendMsg".equals(methodName) && classes.length == 1 && classes[0] == String.class)
        {
            this.api.sendMsg((String) args[0]);
            return null;
        }
        else if ("sendProgress".equals(methodName) && classes.length == 1 && classes[0] == Integer.class)
        {
            this.api.sendProgress((Integer) args[0]);
            return null;
        }
        else if ("enqueue".equals(methodName) && classes.length == 10 && classes[0] == String.class)
        {
            return this.api.enqueue((String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4],
                    (String) args[5], (String) args[6], (String) args[7], (String) args[8], (Map<String, String>) args[9]);
        }
        else if ("enqueueSync".equals(methodName) && classes.length == 10 && classes[0] == String.class)
        {
            return this.api.enqueueSync((String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4],
                    (String) args[5], (String) args[6], (String) args[7], (String) args[8], (Map<String, String>) args[9]);
        }
        else if ("addDeliverable".equals(methodName) && classes.length == 2 && classes[0] == String.class && classes[1] == String.class)
        {
            return this.api.addDeliverable((String) args[0], (String) args[1]);
        }
        else if ("waitChild".equals(methodName) && classes.length == 1 && (args[0] instanceof Long))
        {
            this.api.waitChild((Long) args[0]);
            return null;
        }
        else if ("hasEnded".equals(methodName) && classes.length == 1 && (args[0] instanceof Long))
        {
            return this.api.hasEnded((Long) args[0]);
        }
        else if ("hasSucceeded".equals(methodName) && classes.length == 1 && (args[0] instanceof Long))
        {
            return this.api.hasSucceeded((Long) args[0]);
        }
        else if ("hasFailed".equals(methodName) && classes.length == 1 && (args[0] instanceof Long))
        {
            return this.api.hasFailed((Long) args[0]);
        }

        throw new NoSuchMethodException(methodName);
    }
}
