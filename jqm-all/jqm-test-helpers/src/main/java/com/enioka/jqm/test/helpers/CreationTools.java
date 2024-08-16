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

import java.util.HashMap;
import java.util.Map;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.model.Cl;
import com.enioka.jqm.model.JndiObjectResource;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.Queue;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.model.RUser;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.lang.util.ByteSource;

/**
 * A set of static methods which help creating test data for automated tests.<br>
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 */
public class CreationTools
{
    // private static Logger jqmlogger = LoggerFactory.getLogger(CreationTools.class);

    private CreationTools()
    {}

    // ------------------ JOB DEFINITION ------------------------

    // TODO: simplify all these overloads (test impact)

    public static long createJobDef(String description, boolean canBeRestarted, String javaClassName, Map<String, String> parameters,
            String jp, Long queueId, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx)
    {
        return createJobDef(description, canBeRestarted, javaClassName, parameters, jp, queueId, maxTimeRunning, applicationName,
                application, module, keyword1, keyword2, keyword3, highlander, cnx, null, false, null, false, PathType.FS);
    }

    public static long createJobDef(String description, boolean canBeRestarted, String javaClassName, Map<String, String> parameters,
            String jp, Queue queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx)
    {
        return createJobDef(description, canBeRestarted, javaClassName, parameters, jp, queue.getId(), maxTimeRunning, applicationName,
                application, module, keyword1, keyword2, keyword3, highlander, cnx, null, false, null, false, PathType.FS);
    }

    public static long createJobDef(String descripton, boolean canBeRestarted, String javaClassName, Map<String, String> parameters,
            String jp, Long queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx, String specificIsolationContext)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, parameters, jp, queue, maxTimeRunning, applicationName, application,
                module, keyword1, keyword2, keyword3, highlander, cnx, specificIsolationContext, false, null, false, PathType.FS);
    }

    public static long createJobDef(String descripton, boolean canBeRestarted, String javaClassName, Map<String, String> parameters,
            String jp, Long queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx, String specificIsolationContext,
            boolean childFirstClassLoader)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, parameters, jp, queue, maxTimeRunning, applicationName, application,
                module, keyword1, keyword2, keyword3, highlander, cnx, specificIsolationContext, childFirstClassLoader, null, false,
                PathType.FS);
    }

    public static long createJobDef(String descripton, boolean canBeRestarted, String javaClassName, Map<String, String> parameters,
            String jp, Long queue, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx, String specificIsolationContext,
            boolean childFirstClassLoader, String hiddenJavaClasses)
    {
        return createJobDef(descripton, canBeRestarted, javaClassName, parameters, jp, queue, maxTimeRunning, applicationName, application,
                module, keyword1, keyword2, keyword3, highlander, cnx, specificIsolationContext, childFirstClassLoader, hiddenJavaClasses,
                false, PathType.FS);
    }

    public static long createJobDef(String description, boolean canBeRestarted, String javaClassName, Map<String, String> parameters,
            String jp, Long queueId, Integer maxTimeRunning, String applicationName, String application, String module, String keyword1,
            String keyword2, String keyword3, boolean highlander, DbConn cnx, String specificIsolationContext,
            boolean childFirstClassLoader, String hiddenJavaClasses, boolean classLoaderTracing, PathType pathType)
    {
        Long clId = null;

        if (specificIsolationContext != null || childFirstClassLoader || hiddenJavaClasses != null)
        {
            Cl cl;
            specificIsolationContext = specificIsolationContext == null ? applicationName : specificIsolationContext;
            try
            {
                cl = Cl.select_key(cnx, specificIsolationContext);
                clId = cl.getId();
            }
            catch (NoResultException e)
            {
                clId = Cl.create(cnx, specificIsolationContext, childFirstClassLoader, hiddenJavaClasses, classLoaderTracing, true, null);
            }
        }

        long i = JobDef.create(cnx, description, javaClassName, parameters == null ? new HashMap<>() : parameters, jp, queueId,
                maxTimeRunning, applicationName, application, module, keyword1, keyword2, keyword3, highlander, clId, pathType);

        cnx.commit();
        return i;
    }

    // ------------------ DATABASEPROP --------------------------------

    public static long createDatabaseProp(String name, String driver, String url, String user, String pwd, DbConn cnx,
            String validationQuery, Map<String, String> parameters)
    {
        return createDatabaseProp(name, driver, url, user, pwd, cnx, validationQuery, parameters, true);
    }

    public static long createDatabaseProp(String name, String driver, String url, String user, String pwd, DbConn cnx,
            String validationQuery, Map<String, String> parameters, boolean singleton)
    {
        HashMap<String, String> prms = new HashMap<>();
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
        prms.put("url", url);
        prms.put("validationQuery", validationQuery);
        prms.put("testWhileIdle", "true");
        prms.put("testOnBorrow", "true");

        return JndiObjectResource.create(cnx, name, "javax.sql.DataSource", "org.apache.tomcat.jdbc.pool.DataSourceFactory",
                "connection for " + user, singleton, prms);
    }

    // ------------------ MAIL --------------------------------

    public static void createMailSession(DbConn cnx, String name, String hostname, int port, boolean useTls, String username,
            String password)
    {
        HashMap<String, String> prms = new HashMap<>();
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
        HashMap<String, String> prms = new HashMap<>();
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
        HashMap<String, String> prms = new HashMap<>();
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
        HashMap<String, String> prms = new HashMap<>();
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
        HashMap<String, String> prms = new HashMap<>();
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
        HashMap<String, String> prms = new HashMap<>();
        prms.put("PATH", path);
        JndiObjectResource.create(cnx, jndiAlias, "java.io.File.File", "com.enioka.jqm.providers.FileFactory", description, true, prms);
    }

    public static void createJndiUrl(DbConn cnx, String jndiAlias, String description, String url)
    {
        HashMap<String, String> prms = new HashMap<>();
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

    // ---------------------------- STRING -----------------------------------------------------

    public static void createJndiString(DbConn cnx, String jndiAlias, String description, String content)
    {
        HashMap<String, String> prms = new HashMap<>();
        prms.put("STRING", content);

        JndiObjectResource.create(cnx, jndiAlias, "java.lang.String", "com.enioka.jqm.providers.StringFactory", description, false, prms);
    }
}
