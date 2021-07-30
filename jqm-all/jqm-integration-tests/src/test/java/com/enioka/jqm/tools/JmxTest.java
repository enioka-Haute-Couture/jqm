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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;

import org.junit.Assert;
import org.junit.Test;

import com.enioka.jqm.api.JobRequest;
import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.model.RUser;
import com.enioka.jqm.pki.JdbcCa;
import com.enioka.jqm.test.helpers.CreationTools;
import com.enioka.jqm.test.helpers.TestHelpers;

public class JmxTest extends JqmBaseTest
{

    /**
     * Test registration of a remote JMX using default settings (no SSL and no
     * authentication) and test connection to this remote JMX.
     */
    @Test
    public void jmxRemoteTest() throws Exception
    {
        JmxAgent.unregisterAgent();

        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42, "KillApp", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        int i = JobRequest.create("KillApp", "TestUser").submit();

        // Set JMX server on free ports
        ServerSocket s1 = new ServerSocket(0);
        int port1 = s1.getLocalPort();
        ServerSocket s2 = new ServerSocket(0);
        int port2 = s2.getLocalPort();
        s1.close();
        s2.close();
        String hn = InetAddress.getLocalHost().getHostName();

        cnx.runUpdate("node_update_jmx_by_id", port1, port2, TestHelpers.node.getId());
        cnx.commit();

        // Go
        addAndStartEngine();
        TestHelpers.waitForRunning(1, 10000, cnx);
        this.sleep(1); // time to actually run, not only Loader start.

        // Connect to JMX server
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hn + ":" + port1 + "/jndi/rmi://" + hn + ":" + port2 + "/jmxrmi");
        JMXConnector cntor = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = cntor.getMBeanServerConnection();
        int count = mbsc.getMBeanCount();
        System.out.println(count);

        String[] domains = mbsc.getDomains();
        System.out.println("*** domains:");
        for (String d : domains)
        {
            System.out.println(d);
        }
        Set<ObjectInstance> mbeans = mbsc.queryMBeans(new ObjectName("com.enioka.jqm:*"), null);
        System.out.println("*** beans in com.enioka.jqm:*: ");
        for (ObjectInstance oi : mbeans)
        {
            System.out.println(oi.getObjectName());
        }
        Assert.assertTrue(mbeans.size() >= 5);
        // 1 node, 3 pollers, 1 running instance, 1 JDBC pool. The pool may not be
        // visible due to a call to resetSingletons.

        // /////////////////
        // Loader beans
        ObjectName killBean = new ObjectName("com.enioka.jqm:type=Node.Queue.JobInstance,Node=" + TestHelpers.node.getName() + ",Queue=VIPQueue,name=" + i);
        System.out.println("Name to kill: " + killBean.toString());
        mbeans = mbsc.queryMBeans(killBean, null);
        if (mbeans.isEmpty())
        {
            Assert.fail("could not find the JMX Mbean of the launched job");
        }
        JavaJobInstanceTrackerMBean proxy = JMX.newMBeanProxy(mbsc, killBean, JavaJobInstanceTrackerMBean.class);
        Assert.assertEquals("KillApp", proxy.getApplicationName());
        Assert.assertEquals((Integer) i, proxy.getId());
        Assert.assertEquals("TestUser", proxy.getUser());

        // Elements that are not set or testable reproductibly, but should not raise any
        // exception
        proxy.getEnqueueDate();
        proxy.getKeyword1();
        proxy.getKeyword2();
        proxy.getKeyword3();
        proxy.getModule();
        proxy.getRunTimeSeconds();
        proxy.getRunTimeSeconds(); // Twice for all code paths.
        proxy.getSessionId();

        // Kill it though JMX
        proxy.kill();
        Thread.sleep(4000);

        // //////////////////
        // Engine bean
        ObjectName engine = new ObjectName("com.enioka.jqm:type=Node,name=" + TestHelpers.node.getName());
        JqmEngineMBean proxyEngine = JMX.newMBeanProxy(mbsc, engine, JqmEngineMBean.class);
        Assert.assertEquals(1, proxyEngine.getCumulativeJobInstancesCount() + proxyEngine.getCurrentlyRunningJobCount());
        Assert.assertTrue(proxyEngine.getUptime() > 0);
        proxyEngine.getVersion();
        Assert.assertTrue(proxyEngine.isAllPollersPolling());
        Assert.assertTrue(!proxyEngine.isFull());

        // //////////////////
        // Poller bean
        ObjectName poller = new ObjectName("com.enioka.jqm:type=Node.Queue,Node=" + TestHelpers.node.getName() + ",name=VIPQueue");
        QueuePollerMBean proxyPoller = JMX.newMBeanProxy(mbsc, poller, QueuePollerMBean.class);
        Assert.assertEquals(1, proxyPoller.getCumulativeJobInstancesCount() + proxyPoller.getCurrentActiveThreadCount());
        proxyPoller.getCurrentlyRunningJobCount();
        proxyPoller.getJobsFinishedPerSecondLastMinute();
        Assert.assertEquals((Integer) 40, proxyPoller.getMaxConcurrentJobInstanceCount());
        Assert.assertEquals((Integer) 1, proxyPoller.getPollingIntervalMilliseconds());

        Assert.assertTrue(proxyPoller.isActuallyPolling());
        Assert.assertTrue(!proxyPoller.isFull());

        proxyPoller.stop();

        // Done
        cntor.close();
    }

    /**
     * Test registration of a remote JMX with or without SSL and clients
     * authentication and test connection to this remote JMX with or without a trust
     * store (containing the certificate of the internal PKI certification authority
     * which signed the certificate of the remote JMX), a key store (containing the
     * certificate of a test user trusted by the previous certification authority)
     * and correct credentials. <br>
     * SSL client authentication and credentials authentication are independent as
     * explained in {@link JmxAgent#registerAgent(int, int, String, DbConn)}. <br>
     * <br>
     * Each test must be executed in its own JVM. Indeed, the connection to JMX uses
     * the default SSL Context manager because it doesn't seem that it is (anymore
     * ?) possible to use a custom one despite informations that we can find on the
     * internet. This default SSL Context saves trust and key stores in cache and
     * never reloads the files where they come from. This cache is lost when using a
     * new JVM. That is the reason why this test must be executed in its own JVM.
     * 
     * @param testInstance
     *                            the instance of the running test
     * @param enableJmxSsl
     *                            = true if the test must register the remote JMX
     *                            Agent with SSL enabled
     * @param enableJmxSslAuth
     *                            = true if the test must register the remote JMX
     *                            Agent with SSL authentication
     * @param useClientTrustStore
     *                            = true if the test must use the valid client trust
     *                            store when connecting to the JMX Agent "remotely"
     * @param useClientKeyStore
     *                            = true if the test must use the valid client key
     *                            store when connecting to the JMX Agent "remotely"
     * @param createClientStore
     *                            = true if the test must create a default valid
     *                            client key store. In any case, the client key
     *                            store used by this test is stored in the
     *                            ./conf/client.pfx file, therefore the client key
     *                            store must be created there to be used.
     * @param useCredentials
     *                            = true if the test must provide username and
     *                            password while trying to connect to the JMX Agent
     *                            "remotely".
     * @param useExistingUsername
     *                            = true if the test must provide an existing
     *                            username while trying to connect to the JMX Agent
     *                            "remotely".
     * @param useCorrectPassword
     *                            = true if the test must provide the correct
     *                            password corresponding to the test user (if
     *                            {@code useExistingUsername} is false, then no
     *                            password is correct and this setting has no
     *                            effect) while trying to connect to the JMX Agent
     *                            "remotely".
     * @param roles
     *                            the list of roles to give to the test user.
     * @throws Exception
     */
    public static void jmxRemoteSslTest(JqmBaseTest testInstance, boolean enableJmxSsl, boolean enableJmxSslAuth, boolean useClientTrustStore, boolean useClientKeyStore,
            boolean createClientStore, boolean useCredentials, boolean useExistingUsername, boolean useCorrectPassword, String... roles) throws Exception
    {
        DbConn cnx = testInstance.cnx;
        JmxAgent.unregisterAgent();

        Helpers.setSingleParam("enableJmxSsl", Boolean.toString(enableJmxSsl), cnx);
        Helpers.setSingleParam("enableJmxSslAuth", Boolean.toString(enableJmxSslAuth), cnx);

        String userName = "testuser";
        String userPass = "password";
        new File("./conf").mkdir();
        Helpers.initializeConfigFile("jmxremote.policy", "./conf/jmxremote.policy", JmxTest.class.getClassLoader()); // Needed for the "alljmx" role giving all JMX
                                                                                                                     // permissions, but also for other roles.
        if (roles == null || roles.length == 0)
        {
            Helpers.createRoleIfMissing(cnx, "alljmx", "All JMX permissions", "jmx:javax.management.MBeanPermission \"*\", \"*\"");
            roles = new String[] { "client read only", "alljmx" };
        }
        Helpers.createUserIfMissing(cnx, userName, userPass, "test user", roles);

//        System.setProperty("javax.net.debug", "all");

        // From JmxTest#jmxRemoteTest method:
        // Set JMX server on free ports
        ServerSocket s1 = new ServerSocket(0);
        int port1 = s1.getLocalPort();
        ServerSocket s2 = new ServerSocket(0);
        int port2 = s2.getLocalPort();
        s1.close();
        s2.close();
        String hn = InetAddress.getLocalHost().getHostName();

        cnx.runUpdate("node_update_jmx_by_id", port1, port2, TestHelpers.node.getId());
        cnx.commit();

        // Go
        testInstance.addAndStartEngine();
        TestHelpers.waitForRunning(1, 10000, cnx);
        testInstance.sleep(1); // time to actually run, not only Loader start.

        // Connect to JMX server
        // following instructions of
        // https://db.apache.org/derby/docs/10.9/adminguide/radminjmxcode.html#radminjmxcode__connmbeanserver
        // and
        // https://www.cleantutorials.com/jconsole/jconsole-ssl-with-password-authentication:
        HashMap<String, Object> env = new HashMap<String, Object>();
        env.put("com.sun.jndi.rmi.factory.socket", new SslRMIClientSocketFactory());

        if (useCredentials)
        {
            String[] credentials = new String[] { userName, userPass };
            if (!useExistingUsername)
            {
                credentials[0] += "unknown";
            }
            if (!useCorrectPassword)
            {
                credentials[1] += "wrong";
            }
            env.put(JMXConnector.CREDENTIALS, credentials);
        }

        final String pfxPassword = GlobalParameter.getParameter(cnx, "pfxPassword", "SuperPassword");

        final String trustStorePath = "./conf/trusted.jks";
        final String keyStorePath = "./conf/client.pfx";

        if (createClientStore)
        {
            // From JettyTest class:
            JdbcCa.prepareClientStore(cnx, "CN=" + userName, keyStorePath, pfxPassword, "client-cert", "./conf/client.cer");
        }
        // System properties are JVM related, they are lost at the end of execution
        // (https://stackoverflow.com/questions/21204334/system-setproperty-and-system-getproperty):
        if (useClientTrustStore)
        {
            System.setProperty("javax.net.ssl.trustStore", trustStorePath);
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
            System.setProperty("javax.net.ssl.trustStorePassword", pfxPassword);
        }
        else
        {
            System.clearProperty("javax.net.ssl.trustStore");
            System.clearProperty("javax.net.ssl.trustStoreType");
            System.clearProperty("javax.net.ssl.trustStorePassword");
        }

        if (useClientKeyStore)
        {
            System.setProperty("javax.net.ssl.keyStore", keyStorePath);
            System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
            System.setProperty("javax.net.ssl.keyStorePassword", pfxPassword);
        }
        else
        {
            System.clearProperty("javax.net.ssl.keyStore");
            System.clearProperty("javax.net.ssl.keyStoreType");
            System.clearProperty("javax.net.ssl.keyStorePassword");
        }

        // From JmxTest#jmxRemoteTest method:
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hn + ":" + port1 + "/jndi/rmi://" + hn + ":" + port2 + "/jmxrmi");
        JMXConnector cntor = JMXConnectorFactory.connect(url, env);
        MBeanServerConnection mbsc = cntor.getMBeanServerConnection();

        int count = mbsc.getMBeanCount();
        System.out.println(count);

        String[] domains = mbsc.getDomains();
        System.out.println("*** domains:");
        for (String d : domains)
        {
            System.out.println(d);
        }

        // Create the job now and not before in order to create it only if the
        // connection to "remote" JMX is succesful and the authenticated user (if there
        // is one) has the JMX permission to call MBeanServerConnection#getDomains().
        // Otherwise, if the job is created and the remote JMX doesn't receive the kill
        // order because the connection isn't established or because the authenticated
        // user hasn't enough JMX permissions (like in
        // JmxRemoteSslWithoutAuthMissingPermissionsTest), the job takes a lot of time
        // to stop by itself.
        CreationTools.createJobDef(null, true, "pyl.KillMe", null, "jqm-tests/jqm-test-pyl/target/test.jar", TestHelpers.qVip, 42, "KillApp", null, "Franquin", "ModuleMachin", "other", "other", false, cnx);
        int i = JobRequest.create("KillApp", "TestUser").submit();

        TestHelpers.waitForRunning(1, 10000, cnx);
        testInstance.sleep(1); // time to actually run, not only Loader start.

        Set<ObjectInstance> mbeans = mbsc.queryMBeans(new ObjectName("com.enioka.jqm:*"), null);
        System.out.println("*** beans in com.enioka.jqm:*: ");
        for (ObjectInstance oi : mbeans)
        {
            System.out.println(oi.getObjectName());
        }
        Assert.assertTrue(mbeans.size() >= 5);
        // 1 node, 3 pollers, 1 running instance, 1 JDBC pool. The pool may not be
        // visible due to a call to resetSingletons.

        // /////////////////
        // Loader beans
        ObjectName killBean = new ObjectName("com.enioka.jqm:type=Node.Queue.JobInstance,Node=" + TestHelpers.node.getName() + ",Queue=VIPQueue,name=" + i);
        System.out.println("Name to kill: " + killBean.toString());
        mbeans = mbsc.queryMBeans(killBean, null);
        if (mbeans.isEmpty())
        {
            Assert.fail("could not find the JMX Mbean of the launched job");
        }
        JavaJobInstanceTrackerMBean proxy = JMX.newMBeanProxy(mbsc, killBean, JavaJobInstanceTrackerMBean.class);
        Assert.assertEquals("KillApp", proxy.getApplicationName());
        Assert.assertEquals((Integer) i, proxy.getId());
        Assert.assertEquals("TestUser", proxy.getUser());

        // Elements that are not set or testable reproductibly, but should not raise any
        // exception
        proxy.getEnqueueDate();
        proxy.getKeyword1();
        proxy.getKeyword2();
        proxy.getKeyword3();
        proxy.getModule();
        proxy.getRunTimeSeconds();
        proxy.getRunTimeSeconds(); // Twice for all code paths.
        proxy.getSessionId();

        // Kill it though JMX
        proxy.kill();
        Thread.sleep(4000);

        // //////////////////
        // Engine bean
        ObjectName engine = new ObjectName("com.enioka.jqm:type=Node,name=" + TestHelpers.node.getName());
        JqmEngineMBean proxyEngine = JMX.newMBeanProxy(mbsc, engine, JqmEngineMBean.class);
        Assert.assertEquals(1, proxyEngine.getCumulativeJobInstancesCount() + proxyEngine.getCurrentlyRunningJobCount());
        Assert.assertTrue(proxyEngine.getUptime() > 0);
        proxyEngine.getVersion();
        Assert.assertTrue(proxyEngine.isAllPollersPolling());
        Assert.assertTrue(!proxyEngine.isFull());

        // //////////////////
        // Poller bean
        ObjectName poller = new ObjectName("com.enioka.jqm:type=Node.Queue,Node=" + TestHelpers.node.getName() + ",name=VIPQueue");
        QueuePollerMBean proxyPoller = JMX.newMBeanProxy(mbsc, poller, QueuePollerMBean.class);

        Assert.assertEquals(1, proxyPoller.getCumulativeJobInstancesCount() + proxyPoller.getCurrentActiveThreadCount());
        proxyPoller.getCurrentlyRunningJobCount();
        proxyPoller.getJobsFinishedPerSecondLastMinute();
        Assert.assertEquals((Integer) 40, proxyPoller.getMaxConcurrentJobInstanceCount());
        Assert.assertEquals((Integer) 1, proxyPoller.getPollingIntervalMilliseconds());

        Assert.assertTrue(proxyPoller.isActuallyPolling());
        Assert.assertTrue(!proxyPoller.isFull());

        proxyPoller.stop();

        // Done
        cntor.close();
    }

    /**
     * See
     * {@link #jmxRemoteSslTest(JqmBaseTest, boolean, boolean, boolean, boolean, Runnable)}
     */
    public static void jmxRemoteSslTest(JqmBaseTest testInstance, boolean enableJmxSsl, boolean enableJmxSslAuth, boolean useClientTrustStore, boolean useClientKeyStore)
            throws Exception
    {
        jmxRemoteSslTest(testInstance, enableJmxSsl, enableJmxSslAuth, useClientTrustStore, useClientKeyStore, true, true, true, true);
    }

    /**
     * Test registration of a remote JMX using SSL and authentication of clients for
     * connections and test connection to this remote JMX with a client having valid
     * stuff to connect (the client trusts the server and the server trusts him).
     */
    @Test
    public void jmxRemoteSslWithAuthWithTrustStoreAndKeyStoreTest() throws Exception
    {
        jmxRemoteSslTest(this, true, true, true, true);
    }

    /**
     * Get the permissions of the {@code roleName} role from the current Java
     * policy.
     * 
     * @param roleName
     *                 the name of the role whose permissions are collected
     * @return the enumeration of the permissions of the {@code roleName} role
     */
    static Enumeration<Permission> getRolePolicyPermissions(String roleName)
    {
        Principal[] principals = new Principal[] { new JmxJqmRolePrincipal(roleName) };
        ProtectionDomain pDomain = new ProtectionDomain(new CodeSource(null, (CodeSigner[]) null), null, null, principals);
        return Policy.getPolicy().getPermissions(pDomain).elements();
    }

    /**
     * Check if the current Java policy gives to the {@code roleName} role all the
     * {@code rolePerms} permissions.
     * 
     * @param roleName
     *                  the name of the role whose permissions are checked
     * @param rolePerms
     *                  the permissions that the role is meant to have
     * @return true if the role has all the {@code rolePerms} permissions
     */
    static boolean checkRolePolicyPermissions(String roleName, String[] rolePerms)
    {
        Enumeration<Permission> rolePolicyPerms = getRolePolicyPermissions(roleName);
        System.out.println("# Checking policy permissions for " + roleName + " role...");
        Map<String, List<String>> policyPerms = new HashMap<String, List<String>>();
        while (rolePolicyPerms.hasMoreElements())
        {
            Permission perm = rolePolicyPerms.nextElement();

            String permName = getPermissionName(perm);
            List<String> permActions = getPermissionActions(perm);

            List<String> savedPermActions = policyPerms.get(permName);
            if (savedPermActions == null)
            {
                policyPerms.put(permName, permActions);
            }
            else
            {
                savedPermActions.addAll(permActions); // May have actions put several times, but it's not a problem.
            }
        }

        boolean missingPerm = false;
        for (String perm : rolePerms)
        {
            String formattedPermName = formatJmxPermission(perm);
            if (formattedPermName == null) // Not a JMX permission, ignored
                continue;

            String permName;
            List<String> permActions;
            if (perm.contains("\",")) // Perm has actions
            {
                int nameDelimiterIndex = formattedPermName.lastIndexOf(" ");
                permName = formattedPermName.substring(0, nameDelimiterIndex);
                permActions = getActionsList(formattedPermName.substring(nameDelimiterIndex + 1));
            }
            else
            {
                permName = formattedPermName;
                permActions = new ArrayList<String>();
            }

            List<String> policyPermActions = policyPerms.get(permName);
            if (policyPermActions == null)
            {
                missingPerm = true;
                System.out.println("Missing permission [" + formattedPermName + "] in policy");
            }
            else if (!policyPermActions.containsAll(permActions))
            {
                missingPerm = true;
                System.out.println("Missing at least one action of [" + formattedPermName + "] permission in policy");
            }
        }
        return !missingPerm;
    }

    /**
     * Get the name of the given permission in a certain format.
     * 
     * @param perm
     *             the permission from which the name is collected
     * @return the name of the permission in a certain format
     */
    private static String getPermissionName(Permission perm)
    {
        return perm.getClass().getName() + " " + perm.getName();
    }

    /**
     * Get the list of actions of the given permission
     * 
     * @param perm
     *             the permission from which the actions are collected
     * @return the list of actions of the given permission, that list is empty if
     *         the permission has no action.
     */
    private static List<String> getPermissionActions(Permission perm)
    {
        return getActionsList(perm.getActions());
    }

    /**
     * Get the list of actions from {@code actions} string.
     * 
     * @param actions
     *                the actions separated by a ","
     * @return the list of actions, can be empty but never null.
     */
    private static List<String> getActionsList(String actions)
    {
        return actions != null && actions.length() > 0 ? new ArrayList<String>(Arrays.asList(actions.split(","))) : new ArrayList<String>(); // A permission can have no
                                                                                                                                             // action
    }

    private static final Pattern PERMISSION_USELESS_CHARS = Pattern.compile("[\";,]");

    /**
     * Format the {@code jqmPermName} permission name to suit the format of
     * {@link #getPermissionName(Permission)} and {@link #getActionsList(String)} if
     * it is a JMX permission ("jmx:" prefix), otherwise return null.
     * 
     * @param jqmPermName
     *                    the name of the JQM permission saved in the database
     * @return the formatted JMX permission, or null if the permission isn't a JMX
     *         permission.
     */
    private static String formatJmxPermission(String jqmPermName)
    {
        return jqmPermName.startsWith("jmx:") ? PERMISSION_USELESS_CHARS.matcher(jqmPermName.substring(4)).replaceAll("").trim() : null;
    }

    /**
     * Test {@link JmxAgent#updatePolicyFile(Map)} method, checking that the
     * permissions saved in the JQM database are correctly added to the roles by the
     * Java policy.
     * 
     * @throws Exception
     */
    @Test
    public void jmxPolicyPermissionsUpdateTest() throws Exception
    {
        new File("./conf").mkdir();
        new File(JmxAgent.POLICY_PATH).delete();
        Helpers.initializeConfigFile("jmxremote.policy", JmxAgent.POLICY_PATH, JmxTest.class.getClassLoader());
        System.setProperty("java.security.policy", JmxAgent.POLICY_PATH); // Can force to use only this policy file by appending a "=" before policyPath.
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }

        String[] role1Perms = new String[] { "jmx:javax.management.MBeanPermission \"com.enioka.jqm.*#*[com.enioka.jqm:*]\", \"getDomains\"",
                "jmx:java.net.SocketPermission \"localhost:1111\", \"listen\"" };
        String[] role2Perms = new String[] { "jmx:javax.management.MBeanPermission \"com.enioka.jqm.*#*[com.enioka.jqm:*]\", \"queryMBeans\"",
                "jmx:java.net.SocketPermission \"localhost:2222\", \"resolve\"", "jmx:java.lang.reflect.ReflectPermission \"suppressAccessChecks\"" };
        String[] role3Perms = new String[] { "jmx:java.net.SocketPermission \"localhost:3333\", \"listen\"" };
        String[] role4Perms = new String[] { "jmx:java.net.SocketPermission \"localhost:4444\", \"listen\"" };
        Helpers.createRoleIfMissing(cnx, "role1", "Some JMX permissions", role1Perms);
        Helpers.createRoleIfMissing(cnx, "role2", "Some JMX permissions", role2Perms);
        Helpers.createRoleIfMissing(cnx, "role3", "Some JMX permissions", role3Perms);
        Helpers.createRoleIfMissing(cnx, "role4", "Some JMX permissions", role4Perms);

        // Get roles from the database and update policy the same way that
        // JmxLoginModule does, ie with an user:
        String userName = "testuser";
        String userPass = "password";
        Helpers.createUserIfMissing(cnx, userName, userPass, "test user", "role1", "role2", "role3", "role4");
        List<RRole> roles = RUser.selectlogin(cnx, userName).getRoles(cnx);
        JmxAgent.updatePolicyFile(JmxAgent.getJmxPermissionsOfRoles(roles, cnx));
        cnx.close();

        // Role 1 is defined in jmxremote.policy test file, some permissions are added
        // from the database (defined in role1Perms) and should be present after
        // updating the policy file:
        Assert.assertTrue(JmxTest.checkRolePolicyPermissions("role1", role1Perms));

        // Role 2 is defined in jmxremote.policy test file, some permissions are added
        // from the database (defined in role2Perms) and some permissions are not added
        // but checked if they are present after updating the policy file (to check that
        // JmxTest#checkRolePolicyPermissions works)
        // (java.lang.reflect.ReflectPermission "suppressAccessChecks" permission has no
        // action, but should be added without problem):
        Assert.assertTrue(JmxTest.checkRolePolicyPermissions("role2", role2Perms));
        Assert.assertFalse(JmxTest.checkRolePolicyPermissions("role2", new String[] {
                "jmx:javax.management.MBeanPermission \"com.enioka.jqm.*#*[com.enioka.jqm:*]\", \"queryMBeans\"",
                "jmx:java.net.SocketPermission \"localhost:2222\", \"resolve\"", "jmx:java.lang.reflect.ReflectPermission \"suppressAccessChecks\"",
                "jmx:java.net.SocketPermission \"localhost:2222\", \"listen\"", "jmx:java.net.SocketPermission \"localhost:4444\", \"resolve\"" }));

        // Role 3 is not defined in jmxremote.policy test file, it is completely created
        // from database after updating the policy file:
        Assert.assertTrue(JmxTest.checkRolePolicyPermissions("role3", role3Perms));

        // Role 4 is defined in jmxremote.policy test file, it has permission
        // [java.net.SocketPermission "localhost:5555", "listen"] that isn't in the
        // database, it should be removed after updating the policy file:
        Assert.assertTrue(JmxTest.checkRolePolicyPermissions("role4", role4Perms));
        Assert.assertFalse(JmxTest.checkRolePolicyPermissions("role4", new String[] { "jmx:java.net.SocketPermission \"localhost:5555\", \"listen\"" }));
    }

}
