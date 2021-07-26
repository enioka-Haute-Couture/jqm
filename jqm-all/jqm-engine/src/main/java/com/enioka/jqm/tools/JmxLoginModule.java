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
package com.enioka.jqm.tools;

import java.util.*;
import java.io.IOException;
import java.security.Principal;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;

import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.jdbc.NonUniqueResultException;
import com.enioka.jqm.model.RRole;
import com.enioka.jqm.model.RUser;

/**
 * Module used by the JMX remote agent to authenticate users with the provided
 * credentials. JQM roles are affected to the authenticated user by giving to
 * its subject instance a {@link JmxJqmRolePrincipal} principal instance per
 * role. The subject also receives a {@link JmxJqmUsernamePrincipal} principal
 * instance in order to allow user specific JMX permissions.
 */
public class JmxLoginModule implements LoginModule
{

    private Logger jqmlogger = null;

    private Subject subject;
    private CallbackHandler callbackHandler;

    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;

    private String userName;
    private RUser user;
    private List<RRole> userRoles;
    private Set<Principal> userPrincipals;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options)
    {
        this.subject = subject;
        this.callbackHandler = callbackHandler;

        jqmlogger = "true".equalsIgnoreCase((String) options.get("debug")) ? LoggerFactory.getLogger(JmxLoginModule.class) : null;
    }

    public boolean login() throws LoginException
    {
        if (jqmlogger != null)
        {
            jqmlogger.debug("Logging in...");
        }

        if (callbackHandler == null)
        {
            throw new LoginException("callbackHandler cannot be null"); // It is required to get credentials.
        }

        Callback[] callbacks = new Callback[] { new NameCallback("name:"), new PasswordCallback("password:", false) };

        try
        {
            callbackHandler.handle(callbacks);
        }
        catch (IOException e)
        {
            String error = "IOException calling handle on callbackHandler";
            if (jqmlogger != null)
            {
                jqmlogger.error(error, e);
            }
            throw new LoginException(error);
        }
        catch (UnsupportedCallbackException e)
        {
            String error = "UnsupportedCallbackException calling handle on callbackHandler";
            if (jqmlogger != null)
            {
                jqmlogger.error(error, e);
            }
            throw new LoginException(error);
        }

        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];

        userName = nameCallback.getName();
        if (userName == null)
        {
            throw new LoginException("Provided username is null");
        }

        String password = new String(passwordCallback.getPassword());

        try
        {
            DbConn cnx = Helpers.getNewDbSession();
            user = RUser.selectlogin(cnx, userName);
            userRoles = user.getRoles(cnx);
            cnx.close();
        }
        catch (NoResultException e)
        {
            String error = "User[" + userName + "] does not exist in the database";
            if (jqmlogger != null)
            {
                jqmlogger.debug(error, e);
            }
            throw new LoginException(error);
        }
        catch (NonUniqueResultException e)
        {
            String error = "User[" + userName + "] has multiple entries in the database";
            if (jqmlogger != null)
            {
                jqmlogger.debug(error, e);
            }
            throw new LoginException(error);
        }
        catch (Exception e)
        {
            String error = "Error while querying the database";
            if (jqmlogger != null)
            {
                jqmlogger.error(error, e);
            }
            throw new LoginException(error);
        }

        String userPasswordHash = user.getPassword();

        // Code taken from com.enioka.jqm.tools.Helpers#createUserIfMissing(DbConn,
        // String, String, String, String...):
        String passwordHash = null;
        if (password != null && !"".equals(password))
        {
            ByteSource salt = ByteSource.Util.bytes(Hex.decode(user.getHashSalt())); // Code taken from com.enioka.jqm.webui.shiro.JdbcRealm#getUser(String)
            passwordHash = new Sha512Hash(password, salt, 100000).toHex();
        }

        if (userPasswordHash != null && userPasswordHash.equals(passwordHash))
        {
            if (jqmlogger != null)
            {
                jqmlogger.debug("Login accepted");
            }

            loginSucceeded = true;
            return loginSucceeded;
        }
        else
        {
            if (jqmlogger != null)
            {
                jqmlogger.debug("Login refused");
            }
            loginSucceeded = false;
            throw new FailedLoginException("Invalid credentials, login refused.");
        }

    }

    public boolean commit() throws LoginException
    {
        if (jqmlogger != null)
        {
            jqmlogger.debug("Committing...");
        }

        if (!loginSucceeded)
        {
            return false;
        }

        if (subject == null)
        {
            throw new LoginException("Subject cannot be null"); // Otherwise we cannot assign him his principals and therefore his permissions.
        }

        userPrincipals = new HashSet<Principal>();
        userPrincipals.add(new JmxJqmUsernamePrincipal(userName)); // To allow specific JMX permissions for specific users.

        if (userRoles != null)
        {
            for (RRole r : userRoles)
            {
                if (r != null)
                {
                    String roleName = r.getName();
                    if (roleName != null)
                    {
                        userPrincipals.add(new JmxJqmRolePrincipal(roleName));
                    }
                }
            }
        }
        if (!user.getInternal()) // Code taken from com.enioka.jqm.webui.shiro.JdbcRealm#getUser(String)
        {
            userPrincipals.add(new JmxJqmRolePrincipal("human"));
        }

        for (Principal principal : userPrincipals)
        {
            if (!subject.getPrincipals().contains(principal))
            {
                subject.getPrincipals().add(principal);
                if (jqmlogger != null)
                {
                    jqmlogger.debug("Added " + principal + " to subject[userName: " + userName + "]");
                }
            }
        }

        commitSucceeded = true;
        return commitSucceeded;
    }

    public boolean abort() throws LoginException
    {
        if (jqmlogger != null)
        {
            jqmlogger.debug("Received abort request");
        }

        if (!loginSucceeded)
        {
            return false;
        }
        else
        {
            logout();
            return true;
        }
    }

    public boolean logout() throws LoginException
    {
        if (jqmlogger != null)
        {
            jqmlogger.debug("Logout subject[userName: " + userName + "]");
        }

        if (subject != null && userPrincipals != null)
        {
            subject.getPrincipals().removeAll(userPrincipals);
        }
        loginSucceeded = false;
        commitSucceeded = false;
        userName = null;
        userPrincipals = null;
        return true;
    }

}
