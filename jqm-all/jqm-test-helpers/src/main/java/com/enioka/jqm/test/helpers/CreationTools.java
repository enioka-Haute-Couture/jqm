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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;
import org.hsqldb.Server;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jpamodel.JndiObjectResource;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDef.PathType;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.Queue;
import com.enioka.jqm.jpamodel.RRole;
import com.enioka.jqm.jpamodel.RUser;

/**
 * A set of static methods which help creating test data for automated tests.<br>
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
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

    // ------------------ JOB DEFINITION ------------------------

    // TODO: simplify all these overloads (test impact)

    public static int createJobDef(String description, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Integer queueId, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx)
    {
        return createJobDef(description, canBeRestarted, javaClassName, jps, jp, queueId, maxTimeRunning, applicationName, application,
                module, keyword1, keyword2, keyword3, highlander, cnx, null, false, null, false, PathType.FS);
    }

    public static int createJobDef(String description, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx)
    {
        return createJobDef(description, canBeRestarted, javaClassName, jps, jp, queue.getId(), maxTimeRunning, applicationName,
                application, module, keyword1, keyword2, keyword3, highlander, cnx, null, false, null, false, PathType.FS);
    }

    public static int createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx, String specificIsolationContext)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, jps, jp, queue.getId(), maxTimeRunning, applicationName, application,
                module, keyword1, keyword2, keyword3, highlander, cnx, specificIsolationContext, false, null, false, PathType.FS);
    }

    public static int createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx, String specificIsolationContext,
            boolean childFirstClassLoader)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, jps, jp, queue.getId(), maxTimeRunning, applicationName, application,
                module, keyword1, keyword2, keyword3, highlander, cnx, specificIsolationContext, childFirstClassLoader, null, false,
                PathType.FS);
    }

    public static int createJobDef(String descripton, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx, String specificIsolationContext,
            boolean childFirstClassLoader, String hiddenJavaClasses)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, jps, jp, queue.getId(), maxTimeRunning, applicationName, application,
                module, keyword1, keyword2, keyword3, highlander, cnx, specificIsolationContext, childFirstClassLoader, hiddenJavaClasses,
                false, PathType.FS);
    }

    public static int createJobDef(String description, boolean canBeRestarted, String javaClassName, List<JobDefParameter> jps, String jp,
            Integer queueId, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx, String specificIsolationContext,
            boolean childFirstClassLoader, String hiddenJavaClasses, boolean classLoaderTracing, PathType pathType)
    {
        // TODO: change the prm list by a string map.

        Map<String, String> prms = new HashMap<String, String>();
        if (jps != null)
        {
            for (JobDefParameter p : jps)
            {
                prms.put(p.getKey(), p.getValue());
            }
        }

        int i = JobDef.create(cnx, description, javaClassName, prms, jp, queueId, maxTimeRunning, applicationName, application, module,
                keyword1, keyword2, keyword3, highlander, specificIsolationContext, childFirstClassLoader, hiddenJavaClasses,
                classLoaderTracing, pathType);
        cnx.commit();
        return i;
    }

    // ------------------ DATABASEPROP --------------------------------

    public static void createDatabaseProp(String name, String driver, String url, String user, String pwd, DbConn cnx,
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

        JndiObjectResource.create(cnx, name, "javax.sql.DataSource", "org.apache.tomcat.jdbc.pool.DataSourceFactory",
                "connection for " + user, true, prms);
    }

    // ------------------ DATABASEPROP --------------------------------

    public static void createMailSession(DbConn cnx, String name, String hostname, int port, boolean useTls, String username,
            String password)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("smtpServerHost", hostname);
        prms.put("smtpServerPort", String.valueOf(port));
        prms.put("smtpUser", username);
        prms.put("smtpPassword", password);
        prms.put("useTls", String.valueOf(useTls));

        JndiObjectResource.create(cnx, name, "javax.mail.Session", "com.enioka.jqm.providers.MailSessionFactory",
                "mail SMTP server used for sending notification mails", true, prms);
    }

    // ------------------ JNDI FOR JMS & co --------------------------------

    public static void createJndiQcfMQSeries(DbConn cnx, String jndiAlias, String description, String hostname, String queueManagerName,
            Integer port, String channel)
    {
        createJndiQcfMQSeries(cnx, jndiAlias, description, hostname, queueManagerName, port, channel, null);
    }

    public static void createJndiQcfMQSeries(DbConn cnx, String jndiAlias, String description, String hostname, String queueManagerName,
            Integer port, String channel, HashMap<String, String> optionalParameters)
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

        JndiObjectResource.create(cnx, jndiAlias, "com.ibm.mq.jms.MQQueueConnectionFactory",
                "com.ibm.mq.jms.MQQueueConnectionFactoryFactory", description, false, prms);
    }

    public static void createJndiQueueMQSeries(DbConn cnx, String jndiAlias, String description, String queueName,
            HashMap<String, String> optionalParameters)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("QU", queueName);
        if (optionalParameters != null)
        {
            prms.putAll(optionalParameters);
        }

        JndiObjectResource.create(cnx, jndiAlias, "com.ibm.mq.jms.MQQueue", "com.ibm.mq.jms.MQQueueFactory", description, false, prms);
    }

    public static void createJndiQcfActiveMQ(DbConn cnx, String jndiAlias, String description, String Url,
            HashMap<String, String> optionalParameters)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("brokerURL", Url);
        if (optionalParameters != null)
        {
            prms.putAll(optionalParameters);
        }

        JndiObjectResource.create(cnx, jndiAlias, "org.apache.activemq.ActiveMQConnectionFactory",
                "org.apache.activemq.jndi.JNDIReferenceFactory", description, false, prms);
    }

    public static void createJndiQueueActiveMQ(DbConn cnx, String jndiAlias, String description, String queueName,
            HashMap<String, String> optionalParameters)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("physicalName", queueName);
        if (optionalParameters != null)
        {
            prms.putAll(optionalParameters);
        }

        JndiObjectResource.create(cnx, jndiAlias, "org.apache.activemq.command.ActiveMQQueue",
                "org.apache.activemq.jndi.JNDIReferenceFactory", description, false, prms);
    }

    public static void createJndiFile(DbConn cnx, String jndiAlias, String description, String path)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("PATH", path);
        JndiObjectResource.create(cnx, jndiAlias, "java.io.File.File", "com.enioka.jqm.providers.FileFactory", description, true, prms);
    }

    public static void createJndiUrl(DbConn cnx, String jndiAlias, String description, String url)
    {
        HashMap<String, String> prms = new HashMap<String, String>();
        prms.put("URL", url);
        JndiObjectResource.create(cnx, jndiAlias, "java.io.URL", "com.enioka.jqm.providers.UrlFactory", description, true, prms);
    }

    // ---------------------------- SEC -----------------------------------------------------

    public static void createUser(DbConn cnx, String login, String password, RRole... roles)
    {
        ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
        String[] rr = new String[roles.length];
        for (int i = 0; i < roles.length; i++)
        {
            rr[i] = roles[i].getName();
        }

        RUser.create(cnx, login, new Sha512Hash(password, salt, 100000).toHex(), salt.toHex(), rr);
    }
}
