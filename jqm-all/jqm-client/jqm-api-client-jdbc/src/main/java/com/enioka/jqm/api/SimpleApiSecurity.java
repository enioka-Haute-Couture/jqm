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
package com.enioka.jqm.api;

import java.util.Calendar;
import java.util.UUID;

import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.RUser;

/**
 * Helper class helping dealing with internal security when calling the simple REST API (the API dealing with file transfers)<br>
 * This API may be secured - therefore, we must create a temporary internal user with the necessary permissions.
 * 
 */
final class SimpleApiSecurity
{
    private static Logger jqmlogger = LoggerFactory.getLogger(SimpleApiSecurity.class);

    private static volatile RUser user;
    private static volatile String secret;
    private static volatile Duet logindata;
    private static Boolean useAuth = null;
    private static volatile Object lock = new Object();

    static class Duet
    {
        String usr;
        String pass;
    }

    private SimpleApiSecurity()
    {
        // Helper class
    }

    /**
     * Will create (or recreate) if necessary the temporary login data.<br>
     * Will create its own transaction - therefore the given connection must not have any active transaction.
     */
    static Duet getId(DbConn cnx)
    {
        if (logindata == null && useAuth == null)
        {
            useAuth = Boolean.parseBoolean(GlobalParameter.getParameter(cnx, "enableWsApiAuth", "true"));

            if (!useAuth)
            {
                jqmlogger.debug("The client API will not use any authentication to download files");
                logindata = new Duet();
                logindata.pass = null;
                logindata.usr = null;
            }
            else
            {
                jqmlogger.debug("The client API will use authentication to download files");
            }
        }

        if (!useAuth)
        {
            return logindata;
        }

        if (user == null || user.getExpirationDate().before(Calendar.getInstance()))
        {
            synchronized (lock)
            {
                if (user == null || user.getExpirationDate().before(Calendar.getInstance()))
                {
                    jqmlogger.debug("The client API will create an internal secret to access the simple API for file downloading");

                    // Create new
                    String login = UUID.randomUUID().toString();

                    secret = UUID.randomUUID().toString();
                    ByteSource salt = new SecureRandomNumberGenerator().nextBytes();
                    String hash = new Sha512Hash(secret, salt, 100000).toHex();
                    String saltS = salt.toHex();

                    Calendar expiration = Calendar.getInstance();
                    expiration.add(Calendar.DAY_OF_YEAR, 1);

                    int id = RUser.create(cnx, login, hash, saltS, expiration, true, "administrator");
                    user = RUser.select_id(cnx, id);

                    logindata = new Duet();
                    logindata.pass = secret;
                    logindata.usr = login;

                    // Purge all old internal accounts
                    cnx.runUpdate("user_delete_expired_internal");

                    cnx.commit();
                }
            }
        }

        return logindata;
    }

    static void dispose()
    {
        user = null;
        logindata = null;
        secret = null;
        useAuth = null;
    }
}
