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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyStore;
import java.security.Policy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.naming.spi.NamingManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.RPermission;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.pki.JdbcCa;

/**
 * The JMX Agent is (JVM-wide) RMI for serving remote JMX requests. It is
 * compulsory because JQM uses fixed ports for the JMX server.
 */
final class JmxAgent
{
    private static Logger jqmlogger = LoggerFactory.getLogger(JmxAgent.class);
    private static JMXConnectorServer connectorServer = null;

    /**
     * Path of the Java policy file used for JMX remote permissions
     */
    static final String POLICY_PATH = "./conf/jmxremote.policy";

    private JmxAgent()
    {
        // Utility class
    }

    /**
     * Register the JMX Agent with or without SSL and client authentication
     * depending on settings in the database (provided by the DbConn instance). <br>
     * <br>
     * Authentication with (username, password) credentials is enabled when SSL is
     * enabled, supposing that when an user enables SSL, he wants a minimum of
     * access security. This authentication, managed by {@link JmxLoginModule}, is
     * independent of SSL client authentication (that can be disabled with
     * enableJmxSslAuth global parameter) and gives permissions to an authenticated
     * user depending on his roles. This allows to delete an account from the
     * database and prevent his (previous) owner from continuing to connect to the
     * remote JMX and being authenticated with his trusted certificate. Indeed, it
     * isn't easy to make a trusted certificate no longer trusted, but it is easy to
     * delete an account from the database. Currently, any trusted client
     * certificate can be used to connect to the remote JMX with SSL and SSL client
     * authentification with a valid (username, password) couple, the provided
     * username isn't compared to the username written in the certificate. It would
     * be good to find a way to check that they are the same. <br>
     * JMX permissions are configurable in the conf/jmxremote.policy config file for
     * each role and each user.
     * 
     * @param registryPort
     *                     the port of the JMX remote registry
     * @param serverPort
     *                     the port of the JMX remote server
     * @param hostname
     *                     the hostname of the JMX remote server
     * @param cnx
     *                     a connection to the database
     * @throws JqmInitError
     */
    static synchronized void registerAgent(int registryPort, int serverPort, String hostname, DbConn cnx) throws JqmInitError
    {
        if (connectorServer != null)
        {
            // The agent is JVM-global, not engine specific, so prevent double start.
            jqmlogger.info("The JMX remote agent has already been registered. Please unregister it before if you want to register it again with other settings.");
            return;
        }

        jqmlogger.trace("registering remote agent");
        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            JndiContext ctx = (JndiContext) NamingManager.getInitialContext(null);
            ctx.registerRmiContext(LocateRegistry.createRegistry(registryPort));

            boolean useSsl = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableJmxSsl", "false"));
            boolean sslNeedClientAuth = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableJmxSslAuth", "false"));
            boolean useInternalPki = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableInternalPki", "true"));
            String pfxPassword = GlobalParameter.getParameter(cnx, "pfxPassword", "SuperPassword");

            HashMap<String, Object> env = null;

            if (useSsl)
            {
                jqmlogger.info("JQM will use SSL for all communications with the JMX remote agent as parameter enableJmxSsl is 'true'");

                final String jaasConfigPath = "./conf/jaas.config";

                try
                {
                    new File("./conf").mkdir();
                    Helpers.initializeConfigFile("jmx/jaas.config", jaasConfigPath, null);
                    Helpers.initializeConfigFile("jmx/jmxremote.policy", POLICY_PATH, null);
                }
                catch (Exception e)
                {
                    jqmlogger.error("An error occurred while initializing config files for the JMX remote agent authentication", e);
                }

                System.setProperty("java.security.auth.login.config", jaasConfigPath);
                System.setProperty("java.security.policy", POLICY_PATH); // Can force to use only this policy file by appending a "=" before policyPath.
                if (System.getSecurityManager() == null)
                {
                    System.setSecurityManager(new SecurityManager());
                }

                env = new HashMap<String, Object>();

                // From JettyServer class:
                if (useInternalPki)
                {
                    jqmlogger.info("JQM will use its internal PKI for all certificates as parameter enableInternalPki is 'true'");
                    JdbcCa.prepareWebServerStores(cnx, "CN="
                            + hostname, "./conf/keystore.pfx", "./conf/trusted.jks", pfxPassword, hostname, "./conf/server.cer", "./conf/ca.cer");
                }

                // Following instructions of
                // https://docs.oracle.com/javadb/10.10.1.2/adminguide/radminjmxenablepwdssl.html
                // and
                // https://docs.oracle.com/javase/8/docs/technotes/guides/management/agent.html:
                System.setProperty("com.sun.management.jmxremote.registry.ssl", Boolean.toString(useSsl));
                System.setProperty("com.sun.management.jmxremote.ssl.need.client.auth", Boolean.toString(sslNeedClientAuth));

                RMIServerSocketFactory ssf = null;
//                System.setProperty("javax.net.debug", "all");

                try
                {
                    // Load the SSL keystore properties
                    char[] pfxPasswordChars = pfxPassword != null ? pfxPassword.toCharArray() : null;

                    final String keyStorePath = "./conf/keystore.pfx";
                    KeyStore ks = null;
                    ks = KeyStore.getInstance("PKCS12");
                    FileInputStream ksfis = new FileInputStream(keyStorePath);
                    try
                    {
                        ks.load(ksfis, pfxPasswordChars);
                    }
                    finally
                    {
                        ksfis.close();
                    }
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(ks, pfxPasswordChars);

                    final String trustStorePath = "./conf/trusted.jks";
                    KeyStore ts = null;
                    ts = KeyStore.getInstance("JKS");
                    FileInputStream tsfis = new FileInputStream(trustStorePath);
                    try
                    {
                        ts.load(tsfis, pfxPasswordChars);
                    }
                    finally
                    {
                        tsfis.close();
                    }
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    tmf.init((KeyStore) ts);

                    SSLContext sslctx = SSLContext.getInstance("TLSv1");
                    sslctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

                    if (sslNeedClientAuth)
                    {
                        jqmlogger.info("JQM will use client authentication through SSL for all communications with the JMX remote agent as parameter enableJmxSslAuth is 'true'");
                    }
                    ssf = new ContextfulSslRMIServerSocketFactory(sslctx, sslNeedClientAuth);
                }
                catch (Exception e)
                {
                    jqmlogger.error("JQM could not setup correctly the SSL context for the JMX remote agent", e);
                }

                env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, new ListenedSslRMIClientSocketFactory());
                env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);

                // Used from Java 6 to Java 10 (not tested with greater versions):
                env.put("jmx.remote.x.authenticate", true);
                env.put("jmx.remote.x.login.config", "JmxLoginConfig");
            }

            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://" + hostname + ":" + serverPort + "/jndi/rmi://" + hostname + ":" + registryPort + "/jmxrmi");
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);

            connectorServer.start();
            jqmlogger.info("The JMX remote agent was registered. Connection string is " + url);
        }
        catch (Exception e)
        {
            throw new JqmInitError("Could not create remote JMX agent", e);
        }
    }

    /**
     * Unregister the JMX Agent if it is registered. Allows to register it again
     * with other settings with {@link #registerAgent(int, int, String, DbConn)}.
     */
    static synchronized void unregisterAgent()
    {
        if (connectorServer != null)
        {
            try
            {
                connectorServer.stop();
                connectorServer = null;
                jqmlogger.info("The JMX remote agent has been unregistered.");
            }
            catch (IOException e)
            {
                jqmlogger.error("An error occurred during JMX remote agent unregistration.", e);
            }
        }
        else
        {
            jqmlogger.info("The JMX remote agent has not been registered, ignoring unregistration order.");
        }
    }

    /**
     * Get all JMX permissions ("jmx:" prefix) of the JQM roles provided in
     * {@code roles} list.
     * 
     * @param roles
     *              the list of roles from which the permissions are collected
     * @param cnx
     *              a connection to the database
     * @return a map containing, for a JQM role name key, the list of JMX
     *         permissions of this role as value.
     */
    static Map<String, List<String>> getJmxPermissionsOfRoles(List<RRole> roles, DbConn cnx)
    {
        if (roles == null || roles.isEmpty())
        {
            return null;
        }

        Map<String, List<String>> rolesPerms = new HashMap<String, List<String>>();

        for (RRole r : roles)
        {
            if (r != null)
            {
                String roleName = r.getName();
                List<RPermission> roleRPerms = r.getPermissions(cnx);
                if (roleRPerms != null)
                {
                    List<String> rolePerms = new ArrayList<String>();
                    for (RPermission perm : roleRPerms)
                    {
                        String permName = perm.getName();
                        if (permName.startsWith("jmx:"))
                        {
                            rolePerms.add(permName.substring(4));
                        }
                    }
                    rolesPerms.put(roleName, rolePerms);
                }
            }
        }
        return rolesPerms;
    }

    /**
     * Update the policy file (located at {@link #POLICY_PATH}) and then the current
     * Java policy (that should use the policy file) with the permissions of roles
     * provided in {@code rolesPerms}. <br>
     * All other permissions not provided in {@code rolesPerms} are kept, therefore
     * if a role is missing in {@code rolesPerms}, its permissions won't be updated
     * for the current policy and in the policy file.<br>
     * Permissions given in {@code rolesPerms} are considered as up to date. For a
     * given role, all permissions that were present in the policy file but are
     * missing in {@code rolesPerms} will be removed from the policy file.
     * 
     * @param rolesPerms
     *                   the permissions of the roles to update, saved as the value
     *                   for the corresponding role name key.
     */
    static void updatePolicyFile(Map<String, List<String>> rolesPerms)
    {
        if (rolesPerms == null || rolesPerms.isEmpty())
        {
            return; // Nothing to update
        }

        final String policyTempPath = POLICY_PATH + ".temp";

        FileReader fr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter writer = null;
        try
        {
            fr = new FileReader(POLICY_PATH);
            br = new BufferedReader(fr);
            fw = new FileWriter(policyTempPath);
            bw = new BufferedWriter(fw);
            writer = new PrintWriter(bw);

            final String roleGrantStart = "grant principal com.enioka.jqm.tools.JmxJqmRolePrincipal \"";

            String currentBlockRole = null;
            List<String> currentBlockRolePerms = null; // Updated JMX permissions of the role of the current block, that should and
                                                       // will be written in the policy file.

            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.startsWith(roleGrantStart))
                {
                    String temp = line.substring(roleGrantStart.length());
                    int roleNameEndIndex = temp.indexOf('"');
                    if (roleNameEndIndex > 0)
                    {
                        currentBlockRole = temp.substring(0, roleNameEndIndex);
                        currentBlockRolePerms = rolesPerms.get(currentBlockRole);
                    }
                    else
                    {
                        jqmlogger.error("Invalid policy format: Encountered the beginning of a grant instruction for a JQM role in the Java policy file, but didn't found the '\"' that should end the role name. Ignoring this grant instruction for JQM.");
                    }
                }
                else if (currentBlockRolePerms != null)
                {
                    boolean currentBlockEnd = line.contains("};"); // The "};" could be in the same line that a permission instruction

                    if (line.contains("permission"))
                    {
                        String trimmedLine = line.trim();
                        String permName = trimmedLine.substring(11, trimmedLine.length() - (currentBlockEnd ? 2 : 1)).trim();

                        if (currentBlockRolePerms.contains(permName))
                        {
                            writer.println(line);
                            currentBlockRolePerms.remove(permName);
                        }
                        else
                        {
                            jqmlogger.debug("Removed line [" + line + "] from policy file to update it.");
                        }
                    }

                    if (currentBlockEnd)
                    {
                        // Add all remaining permissions of the current role that are present in the
                        // database but not yet in the policy file:
                        for (String newPerm : currentBlockRolePerms)
                        {
                            writer.println("        permission " + newPerm + ";");
                        }
                        rolesPerms.remove(currentBlockRole);

                        currentBlockRolePerms = null;
                        writer.println("};");
                    }
                    continue;
                }
                writer.println(line);
            }

            try
            {
                br.close();
                fr.close();
            }
            catch (IOException ignored)
            {
                // Readers are no longer useful, if an error occurs, it should not prevent the
                // writer to finish its job.
            }

            for (String newRole : rolesPerms.keySet())
            {
                List<String> rolePerms = rolesPerms.get(newRole);
                if (rolePerms != null && rolePerms.size() > 0)
                {
                    writer.println("");
                    writer.println(roleGrantStart + newRole + "\" {");
                    for (String rolePerm : rolePerms)
                    {
                        writer.println("        permission " + rolePerm + ";");
                    }
                    writer.println("};");
                }
            }

            try
            {
                bw.flush();
                bw.close();
                writer.close();
            }
            catch (IOException ignored)
            {
            }

            // Replace the old policy file with the temp file:
            File policyFile = new File(POLICY_PATH);
            policyFile.delete();
            new File(policyTempPath).renameTo(policyFile);
        }
        catch (Exception e)
        {
            jqmlogger.error("An error occurred while updating JMX permissions in the Java policy", e);
        }
        finally
        {
            try
            {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();

                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
                if (writer != null)
                    writer.close();
            }
            catch (IOException e)
            {
                jqmlogger.debug("Could not close correctly one or more files managers", e);
            }
        }

        try
        {
            Policy.getPolicy().refresh();
        }
        catch (SecurityException e)
        {
            jqmlogger.error("The security manager used doesn't allow JQM to refresh the policy (needs [java.security.SecurityPermission \"getPolicy\"] permission)", e);
        }
    }

}
