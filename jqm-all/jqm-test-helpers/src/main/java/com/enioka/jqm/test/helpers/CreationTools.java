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
package com.enioka.jqm.test.helpers;

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

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;
import org.hsqldb.Server;

import com.enioka.jqm.jpamodel.DeploymentParameter;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JndiObjectResourceParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDef.PathType;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.Node;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.RPermission;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.jpamodel.RUser;
import com.enioka.jqm.jpamodel.RuntimeParameter;
import com.enioka.jqm.jpamodel.State;

/**
 * 
 */
public class CreationTools
{
    private static Logger jqmlogger = Logger.getLogger(CreationTools.class);
    public static Server s;

    private CreationTools()
    {}

    public static void reinitHsqldbServer() throws InterruptedException, FileNotFoundException
    {
        stopHsqldbServer();
        jqmlogger.debug("Starting HSQLDB");
        s = new Server();
        s.setDatabaseName(0, "testdbengine");
        s.setDatabasePath(0, "mem:testdbengine");
        s.setLogWriter(null);
        s.setSilent(true);
        s.start();

        while (s.getState() != 1)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        jqmlogger.debug("HSQLDB is now running");
    }

    public static void stopHsqldbServer()
    {
        if (s != null)
        {
            jqmlogger.debug("Stopping HSQLDB");
            s.signalCloseAllServerConnections();
            s.shutdown();
            s.stop();

            while (s.getState() != 16)
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            s = null;
            jqmlogger.debug("HSQLDB is now stopped");
        }
    }

    // ------------------ GLOBALPARAMETER ------------------------

    public static GlobalParameter createGlobalParameter(String key, String value, EntityManager em)
    {
        GlobalParameter gp = new GlobalParameter();
        EntityTransaction transac = em.getTransaction();
        transac.begin();

        gp.setKey(key);
        gp.setValue(value);

        em.persist(gp);
        transac.commit();
        return gp;
    }

    // ------------------ JOBDEFINITION ------------------------

    public static JobDef initJobDefinition(String javaClassName, Queue queue, EntityManager em)
    {
        JobDef j = new JobDef();
        EntityTransaction transac = em.getTransaction();
        transac.begin();

        j.setJavaClassName(javaClassName);
        j.setQueue(queue);

        em.persist(j);
        transac.commit();

        return j;
    }

    public static JobDef createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, EntityManager em)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, jps, jp, queue, maxTimeRunning, applicationName, application, module,
                keyword1, keyword2, keyword3, highlander, em, null, false, null, false, PathType.FS);
    }

    public static JobDef createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, EntityManager em, String specificIsolationContext)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, jps, jp, queue, maxTimeRunning, applicationName, application, module,
                keyword1, keyword2, keyword3, highlander, em, specificIsolationContext, false, null, false, PathType.FS);
    }

    public static JobDef createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, EntityManager em, String specificIsolationContext,
            boolean childFirstClassLoader)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, jps, jp, queue, maxTimeRunning, applicationName, application, module,
                keyword1, keyword2, keyword3, highlander, em, specificIsolationContext, childFirstClassLoader, null, false, PathType.FS);
    }

    public static JobDef createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, EntityManager em, String specificIsolationContext,
            boolean childFirstClassLoader, String hiddenJavaClasses)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, jps, jp, queue, maxTimeRunning, applicationName, application, module,
                keyword1, keyword2, keyword3, highlander, em, specificIsolationContext, childFirstClassLoader, hiddenJavaClasses, false,
                PathType.FS);
    }

    public static JobDef createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, EntityManager em, String specificIsolationContext,
            boolean childFirstClassLoader, String hiddenJavaClasses, boolean classLoaderTracing, PathType pathType)
    {
        JobDef j = new JobDef();
        EntityTransaction transac = em.getTransaction();
        transac.begin();

        // ------------------

        j.setDescription(descripton);
        j.setCanBeRestarted(canBeRestarted);
        j.setJavaClassName(javaClassName);
        j.setParameters(jps);
        j.setQueue(queue);
        j.setMaxTimeRunning(maxTimeRunning);
        j.setApplicationName(applicationName);
        j.setApplication(application);
        j.setModule(module);
        j.setKeyword1(keyword1);
        j.setKeyword2(keyword2);
        j.setKeyword3(keyword3);
        j.setHighlander(highlander);
        j.setJarPath(jp);
        j.setPathType(pathType);
        j.setSpecificIsolationContext(specificIsolationContext);
        j.setChildFirstClassLoader(childFirstClassLoader);
        j.setHiddenJavaClasses(hiddenJavaClasses);
        j.setClassLoaderTracing(classLoaderTracing);

        em.persist(j);
        transac.commit();

        return j;
    }

    // ------------------ DEPLOYMENTPARAMETER ------------------

    public static DeploymentParameter initDeploymentParameter(Node node, Integer nbThread, EntityManager em)
    {
        DeploymentParameter dp = new DeploymentParameter();
        EntityTransaction transac = em.getTransaction();
        transac.begin();

        dp.setNode(node);
        dp.setNbThread(nbThread);

        em.persist(dp);
        transac.commit();
        return dp;
    }

    public static DeploymentParameter createDeploymentParameter(Node node, Integer nbThread, Integer pollingInterval, Queue qVip,
            EntityManager em)
    {
        DeploymentParameter dp = new DeploymentParameter();
        EntityTransaction transac = em.getTransaction();
        transac.begin();

        dp.setNode(node);
        dp.setNbThread(nbThread);
        dp.setPollingInterval(pollingInterval);
        dp.setQueue(qVip);

        em.persist(dp);
        transac.commit();
        return dp;
    }

    // ------------------ JOBINSTANCE --------------------------

    public static JobInstance createJobInstance(JobDef jd, List<RuntimeParameter> jps, String user, String sessionID, State state,
            Integer position, Queue queue, Node node, EntityManager em)
    {
        JobInstance j = new JobInstance();
        jqmlogger.debug("Creating JobInstance with " + jps.size() + " parameters");

        j.setJd(jd);
        j.setSessionID(sessionID);
        j.setUserName(user);
        j.setState(state);
        j.setQueue(queue);
        j.setNode(node);

        em.persist(j);
        j.setInternalPosition(position);

        for (RuntimeParameter jp : jps)
        {
            jqmlogger.debug("Parameter: " + jp.getKey() + " - " + jp.getValue());
            jp.setJi(j.getId());
            em.persist(jp);
        }

        return j;
    }

    // ------------------ RuntimeParameter -------------------------

    public static RuntimeParameter createJobParameter(String key, String value, EntityManager em)
    {
        RuntimeParameter j = new RuntimeParameter();

        j.setKey(key);
        j.setValue(value);

        em.persist(j);
        return j;
    }

    public static JobDefParameter createJobDefParameter(String key, String value, EntityManager em)
    {
        JobDefParameter j = new JobDefParameter();

        j.setKey(key);
        j.setValue(value);

        em.persist(j);
        return j;
    }

    // ------------------ NODE ---------------------------------

    public static Node createNode(String nodeName, Integer port, String dlRepo, String repo, String tmpDir, EntityManager em)
    {
        Node n = new Node();
        EntityTransaction transac = em.getTransaction();
        transac.begin();

        n.setName(nodeName);
        n.setPort(port);
        n.setDlRepo(dlRepo);
        n.setRepo(repo);
        n.setTmpDirectory(tmpDir);
        try
        {
            n.setDns(InetAddress.getLocalHost().getHostName());
            InetAddress[] adresses = InetAddress.getAllByName(n.getDns());
            for (InetAddress s : adresses)
            {
                if (s.isLoopbackAddress())
                {
                    n.setDns("localhost"); // force true loopback in this case. We may have a hostname that resolves on a public interface
                                           // with a loopback adress...
                }
            }
        }
        catch (UnknownHostException e)
        {
            n.setDns("localhost");
        }
        em.persist(n);
        transac.commit();
        return n;
    }

    // ------------------ QUEUE --------------------------------

    public static Queue initQueue(String name, String description, Integer timeToLive, EntityManager em)
    {
        return initQueue(name, description, timeToLive, em, false);
    }

    public static Queue initQueue(String name, String description, Integer timeToLive, EntityManager em, boolean defaultQueue)
    {
        Queue q = new Queue();
        EntityTransaction transac = em.getTransaction();
        transac.begin();

        q.setName(name);
        q.setDescription(description);
        q.setTimeToLive(timeToLive);
        q.setDefaultQueue(defaultQueue);

        em.persist(q);
        transac.commit();

        return q;
    }

    // ------------------ DATABASEPROP --------------------------------

    public static JndiObjectResource createDatabaseProp(String name, String driver, String url, String user, String pwd, EntityManager em,
            String validationQuery, HashMap<String, String> parameters)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("testWhileIdle", "true");
        prms.put("testOnBorrow", "false");
        prms.put("testOnReturn", "true");
        prms.put("maxActive", "100");
        prms.put("minIdle", "1");
        prms.put("maxWait", "30000");
        prms.put("initialSize", "2");
        prms.put("jmxEnabled", "false");
        prms.put("username", user);
        prms.put("password", pwd);
        prms.put("driverClassName", driver);
        prms.put("url", url);
        prms.put("validationQuery", validationQuery);
        prms.put("testWhileIdle", "true");
        prms.put("testOnBorrow", "true");

        return createJndiObjectResource(em, name, "javax.sql.DataSource", "org.apache.tomcat.jdbc.pool.DataSourceFactory",
                "connection for " + user, true, prms);
    }

    // ------------------ DATABASEPROP --------------------------------

    public static JndiObjectResource createMailSession(EntityManager em, String name, String hostname, int port, boolean useTls,
            String username, String password)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("smtpServerHost", hostname);
        prms.put("smtpServerPort", String.valueOf(port));
        prms.put("smtpUser", username);
        prms.put("smtpPassword", password);
        prms.put("useTls", String.valueOf(useTls));

        return createJndiObjectResource(em, name, "javax.mail.Session", "com.enioka.jqm.providers.MailSessionFactory",
                "mail SMTP server used for sending notification mails", true, prms);
    }

    // ------------------ JNDI FOR JMS & co --------------------------------
    public static JndiObjectResource createJndiObjectResource(EntityManager em, String jndiAlias, String className, String factoryClass,
            String description, boolean singleton, HashMap<String, String> parameters)
    {
        em.getTransaction().begin();
        JndiObjectResource res = new JndiObjectResource();
        res.setAuth(null);
        res.setDescription(description);
        res.setFactory(factoryClass);
        res.setName(jndiAlias);
        res.setType(className);
        res.setSingleton(singleton);
        em.persist(res);

        for (String parameterName : parameters.keySet())
        {
            JndiObjectResourceParameter prm = new JndiObjectResourceParameter();
            prm.setKey(parameterName);
            prm.setValue(parameters.get(parameterName));
            em.persist(prm);
            res.getParameters().add(prm);
            prm.setResource(res);
        }
        em.getTransaction().commit();

        return res;
    }

    public static JndiObjectResource createJndiQcfMQSeries(EntityManager em, String jndiAlias, String description, String hostname,
            String queueManagerName, Integer port, String channel)
    {
        return createJndiQcfMQSeries(em, jndiAlias, description, hostname, queueManagerName, port, channel, null);
    }

    public static JndiObjectResource createJndiQcfMQSeries(EntityManager em, String jndiAlias, String description, String hostname,
            String queueManagerName, Integer port, String channel, HashMap<String, String> optionalParameters)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("HOST", hostname);
        prms.put("PORT", port.toString());
        prms.put("CHAN", channel);
        prms.put("QMGR", queueManagerName);
        prms.put("TRAN", "1"); // 0 = bindings, 1 = CLIENT, 2 = DIRECT, 4 = DIRECTHTTP
        if (optionalParameters != null)
        {
            prms.putAll(optionalParameters);
        }

        return createJndiObjectResource(em, jndiAlias, "com.ibm.mq.jms.MQQueueConnectionFactory",
                "com.ibm.mq.jms.MQQueueConnectionFactoryFactory", description, false, prms);
    }

    public static JndiObjectResource createJndiQueueMQSeries(EntityManager em, String jndiAlias, String description, String queueName,
            HashMap<String, String> optionalParameters)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("QU", queueName);
        if (optionalParameters != null)
        {
            prms.putAll(optionalParameters);
        }

        return createJndiObjectResource(em, jndiAlias, "com.ibm.mq.jms.MQQueue", "com.ibm.mq.jms.MQQueueFactory", description, false, prms);
    }

    public static JndiObjectResource createJndiQcfActiveMQ(EntityManager em, String jndiAlias, String description, String Url,
            HashMap<String, String> optionalParameters)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("brokerURL", Url);
        if (optionalParameters != null)
        {
            prms.putAll(optionalParameters);
        }

        return createJndiObjectResource(em, jndiAlias, "org.apache.activemq.ActiveMQConnectionFactory",
                "org.apache.activemq.jndi.JNDIReferenceFactory", description, false, prms);
    }

    public static JndiObjectResource createJndiQueueActiveMQ(EntityManager em, String jndiAlias, String description, String queueName,
            HashMap<String, String> optionalParameters)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("physicalName", queueName);
        if (optionalParameters != null)
        {
            prms.putAll(optionalParameters);
        }

        return createJndiObjectResource(em, jndiAlias, "org.apache.activemq.command.ActiveMQQueue",
                "org.apache.activemq.jndi.JNDIReferenceFactory", description, false, prms);
    }

    public static JndiObjectResource createJndiFile(EntityManager em, String jndiAlias, String description, String path)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("PATH", path);
        return createJndiObjectResource(em, jndiAlias, "java.io.File.File", "com.enioka.jqm.providers.FileFactory", description, true,
                prms);
    }

    public static JndiObjectResource createJndiUrl(EntityManager em, String jndiAlias, String description, String url)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("URL", url);
        return createJndiObjectResource(em, jndiAlias, "java.io.URL", "com.enioka.jqm.providers.UrlFactory", description, true, prms);
    }

    public static RRole createRole(EntityManager em, String roleName, String description, String... permissions)
    {
        em.getTransaction().begin();
        RRole r = new RRole();
        r.setName(roleName);
        r.setDescription(description);
        em.persist(r);

        for (String s : permissions)
        {
            RPermission p = new RPermission();
            p.setName(s);
            p.setRole(r);
            r.getPermissions().add(p);
        }
        em.getTransaction().commit();
        return r;
    }

    public static RUser createUser(EntityManager em, String login, String password, RRole... roles)
    {
        em.getTransaction().begin();
        RUser u = new RUser();
        u.setLogin(login);
        u.setPassword(password);
        TestHelpers.encodePassword(u);
        em.persist(u);

        for (RRole r : roles)
        {
            u.getRoles().add(r);
            r.getUsers().add(u);
        }
        em.getTransaction().commit();
        return u;
    }

}
