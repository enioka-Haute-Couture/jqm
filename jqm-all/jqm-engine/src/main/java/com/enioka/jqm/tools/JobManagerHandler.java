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
package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.api.JqmClient;
import com.enioka.jqm.api.JqmClientFactory;
import com.enioka.jqm.api.Query;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Message;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.State;

/**
 * This is the implementation behind the proxy described in the <code>JobManager</code> interface inside the jqm-api artifact.
 */
class JobManagerHandler implements InvocationHandler
{
    private static Logger jqmlogger = Logger.getLogger(JobManagerHandler.class);

    private JobInstance ji;
    private JobDef jd = null;
    private Properties p = null;
    private Map<String, String> params = null;
    private String defaultCon = null, application = null, sessionId = null;
    private Node node = null;
    private Calendar lastPeek = null;

    JobManagerHandler(JobInstance ji, Map<String, String> prms)
    {
        p = new Properties();
        p.put("emf", Helpers.getDb());

        DbConn cnx = Helpers.getNewDbSession();
        this.ji = ji;
        params = prms;

        defaultCon = GlobalParameter.getParameter(cnx, "defaultConnection", null);

        this.jd = this.ji.getJD();
        this.application = this.jd.getApplication();
        this.sessionId = this.ji.getSessionID();
        this.node = this.ji.getNode();
        this.node.getDlRepo();
        cnx.close();
    }

    private JqmClient getJqmClient()
    {
        return JqmClientFactory.getClient("uncached", p, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        ClassLoader initial = null;
        String methodName = method.getName();
        Class<?>[] classes = method.getParameterTypes();
        jqmlogger.trace("An engine API method was called: " + methodName + " with nb arguments: " + classes.length);
        try
        {
            initial = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            shouldKill();

            if (classes.length == 0)
            {
                if ("jobApplicationId".equals(methodName))
                {
                    return jd.getId();
                }
                else if ("parentID".equals(methodName))
                {
                    return ji.getParentId();
                }
                else if ("jobInstanceID".equals(methodName))
                {
                    return ji.getId();
                }
                else if ("canBeRestarted".equals(methodName))
                {
                    return jd.isCanBeRestarted();
                }
                else if ("applicationName".equals(methodName))
                {
                    return jd.getApplicationName();
                }
                else if ("sessionID".equals(methodName))
                {
                    return ji.getSessionID();
                }
                else if ("application".equals(methodName))
                {
                    return application;
                }
                else if ("module".equals(methodName))
                {
                    return jd.getModule();
                }
                else if ("keyword1".equals(methodName))
                {
                    return ji.getKeyword1();
                }
                else if ("keyword2".equals(methodName))
                {
                    return ji.getKeyword2();
                }
                else if ("keyword3".equals(methodName))
                {
                    return ji.getKeyword3();
                }
                else if ("definitionKeyword1".equals(methodName))
                {
                    return jd.getKeyword1();
                }
                else if ("definitionKeyword2".equals(methodName))
                {
                    return jd.getKeyword2();
                }
                else if ("definitionKeyword3".equals(methodName))
                {
                    return jd.getKeyword3();
                }
                else if ("userName".equals(methodName))
                {
                    return ji.getUserName();
                }
                else if ("parameters".equals(methodName))
                {
                    return params;
                }
                else if ("defaultConnect".equals(methodName))
                {
                    return this.defaultCon;
                }
                else if ("getDefaultConnection".equals(methodName))
                {
                    return this.getDefaultConnection();
                }
                else if ("getWorkDir".equals(methodName))
                {
                    return getWorkDir();
                }
                else if ("yield".equals(methodName))
                {
                    return null;
                }
                else if ("waitChildren".equals(methodName))
                {
                    waitChildren();
                    return null;
                }
            }
            else if ("sendMsg".equals(methodName) && classes.length == 1 && classes[0] == String.class)
            {
                sendMsg((String) args[0]);
                return null;
            }
            else if ("sendProgress".equals(methodName) && classes.length == 1 && classes[0] == Integer.class)
            {
                sendProgress((Integer) args[0]);
                return null;
            }
            else if ("enqueue".equals(methodName) && classes.length == 10 && classes[0] == String.class)
            {
                return enqueue((String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4], (String) args[5],
                        (String) args[6], (String) args[7], (String) args[8], (Map<String, String>) args[9]);
            }
            else if ("enqueueSync".equals(methodName) && classes.length == 10 && classes[0] == String.class)
            {
                return enqueueSync((String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4],
                        (String) args[5], (String) args[6], (String) args[7], (String) args[8], (Map<String, String>) args[9]);
            }
            else if ("addDeliverable".equals(methodName) && classes.length == 2 && classes[0] == String.class && classes[1] == String.class)
            {
                return addDeliverable((String) args[0], (String) args[1]);
            }
            else if ("waitChild".equals(methodName) && classes.length == 1 && (args[0] instanceof Integer))
            {
                waitChild((Integer) args[0]);
                return null;
            }
            else if ("hasEnded".equals(methodName) && classes.length == 1 && (args[0] instanceof Integer))
            {
                return hasEnded((Integer) args[0]);
            }
            else if ("hasSucceeded".equals(methodName) && classes.length == 1 && (args[0] instanceof Integer))
            {
                return hasSucceeded((Integer) args[0]);
            }
            else if ("hasFailed".equals(methodName) && classes.length == 1 && (args[0] instanceof Integer))
            {
                return hasFailed((Integer) args[0]);
            }

            throw new NoSuchMethodException(methodName);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(initial);
        }
    }

    private void shouldKill()
    {
        // Throttle: only peek once every 1 second.
        if (lastPeek != null && Calendar.getInstance().getTimeInMillis() - lastPeek.getTimeInMillis() < 1000L)
        {
            return;
        }

        DbConn cnx = Helpers.getNewDbSession();
        try
        {
            State s = State.valueOf(cnx.runSelectSingle("ji_select_state_by_id", String.class, ji.getId()));
            jqmlogger.trace("Analysis: should JI " + ji.getId() + " get killed? New status is " + s);
            if (s.equals(State.KILLED))
            {
                jqmlogger.info("Job will be killed at the request of a user");
                Thread.currentThread().interrupt();
                throw new JqmKillException("This job" + "(ID: " + ji.getId() + ")" + " has been killed by a user");
            }
        }
        finally
        {
            Helpers.closeQuietly(cnx);
            lastPeek = Calendar.getInstance();
        }
    }

    /**
     * Create a {@link com.enioka.jqm.jpamodel.Message} with the given message. The {@link com.enioka.jqm.jpamodel.History} to link to is
     * deduced from the context.
     * 
     * @param msg
     * @throws JqmKillException
     */
    private void sendMsg(String msg)
    {
        DbConn cnx = Helpers.getNewDbSession();

        try
        {
            Message.create(cnx, msg, ji.getId());
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    /**
     * Update the {@link com.enioka.jqm.jpamodel.History} with the given progress data.
     * 
     * @param msg
     * @throws JqmKillException
     */
    private void sendProgress(Integer msg)
    {
        DbConn cnx = Helpers.getNewDbSession();
        try
        {
            cnx.runUpdate("jj_update_progress_by_id", msg, ji.getId());
            cnx.commit();
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private Integer enqueue(String applicationName, String user, String mail, String sessionId, String application, String module,
            String keyword1, String keyword2, String keyword3, Map<String, String> parameters)
    {
        JobRequest jr = new JobRequest(applicationName, user, mail);
        jr.setApplicationName(applicationName);
        jr.setUser(user == null ? ji.getUserName() : user);
        jr.setEmail(mail);
        jr.setSessionID(sessionId == null ? this.sessionId : sessionId);
        jr.setApplication(application == null ? jd.getApplication() : application);
        jr.setModule(module == null ? jd.getModule() : module);
        jr.setKeyword1(keyword1);
        jr.setKeyword2(keyword2);
        jr.setKeyword3(keyword3);
        jr.setParentID(this.ji.getId());
        if (parameters != null)
        {
            jr.setParameters(parameters);
        }

        return getJqmClient().enqueue(jr);
    }

    private int enqueueSync(String applicationName, String user, String mail, String sessionId, String application, String module,
            String keyword1, String keyword2, String keyword3, Map<String, String> parameters)
    {
        int i = enqueue(applicationName, user, mail, sessionId, application, module, keyword1, keyword2, keyword3, parameters);
        waitChild(i);
        return i;
    }

    private void waitChild(int id)
    {
        JqmClient c = getJqmClient();
        Query q = Query.create().setQueryHistoryInstances(false).setQueryLiveInstances(true).setJobInstanceId(id);

        while (!c.getJobs(q).isEmpty())
        {
            try
            {
                Thread.sleep(1000);
                shouldKill();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void waitChildren()
    {
        JqmClient c = getJqmClient();
        Query q = Query.create().setQueryHistoryInstances(false).setQueryLiveInstances(true).setParentId(ji.getId());

        while (!c.getJobs(q).isEmpty())
        {
            try
            {
                Thread.sleep(1000);
                shouldKill();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Integer addDeliverable(String path, String fileLabel) throws IOException
    {
        DbConn cnx = Helpers.getNewDbSession();
        try
        {
            String outputRoot = this.ji.getNode().getDlRepo();
            String ext = FilenameUtils.getExtension(path);
            String relDestPath = "" + ji.getJD().getApplicationName() + "/" + ji.getId() + "/" + UUID.randomUUID() + "." + ext;
            String absDestPath = FilenameUtils.concat(outputRoot, relDestPath);
            String fileName = FilenameUtils.getName(path);

            jqmlogger.debug("A deliverable is added. Stored as " + absDestPath + ". Initial name: " + fileName);
            FileUtils.moveFile(new File(path), new File(absDestPath));
            cnx.commit();
            int res = Helpers.createDeliverable(relDestPath, fileName, fileLabel, this.ji.getId(), cnx);
            cnx.commit();
            return res;
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private File getWorkDir()
    {
        File f = new File(FilenameUtils.concat(node.getTmpDirectory(), "" + this.ji.getId()));
        if (!f.isDirectory())
        {
            try
            {
                FileUtils.forceMkdir(f);
            }
            catch (Exception e)
            {
                throw new JqmRuntimeException("Could not create work directory", e);
            }
        }
        return f;
    }

    private DataSource getDefaultConnection() throws NamingException
    {
        Object dso = NamingManager.getInitialContext(null).lookup(this.defaultCon);
        DataSource q = (DataSource) dso;

        return q;
    }

    private boolean hasEnded(int jobId)
    {
        DbConn cnx = Helpers.getNewDbSession();
        try
        {
            cnx.runSelectSingle("ji_select_state_by_id", String.class, jobId);
            return false;
        }
        catch (NoResultException e)
        {
            return true;
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private Boolean hasSucceeded(int requestId)
    {
        DbConn cnx = Helpers.getNewDbSession();
        try
        {
            State s = State.valueOf(cnx.runSelectSingle("history_select_state_by_id", String.class, requestId));
            return s.equals(State.ENDED);
        }
        catch (NoResultException e)
        {
            return null;
        }
        finally
        {
            Helpers.closeQuietly(cnx);
        }
    }

    private Boolean hasFailed(int requestId)
    {
        Boolean b = hasSucceeded(requestId);
        if (b == null)
        {
            return b;
        }
        else
        {
            return !b;
        }
    }
}
