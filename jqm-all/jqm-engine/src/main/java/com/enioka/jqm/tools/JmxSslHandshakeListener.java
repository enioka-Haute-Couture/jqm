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

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxSslHandshakeListener implements HandshakeCompletedListener
{
    private static JmxSslHandshakeListener instance;
    private Logger jqmlogger = LoggerFactory.getLogger(JmxSslHandshakeListener.class);

    /**
     * Time in milliseconds during which a SSL handshake success is saved for an
     * user. <br>
     * During this time, considering an user who attempted to connect with SSL
     * client authentication providing the certificate of an existing user named
     * USERNAME (Common Name of the certificate), any client certificate can be used
     * to authenticate with the (USERNAME, password of USERNAME) credentials,
     * because it doesn't seem that we have a mean to know which client certificate
     * corresponds to the user processed in {@link JmxLoginModule}.
     */
    private static final long SSL_SUCCESS_EXPIRE_TIME = 3000L;

    /**
     * Time in milliseconds during which all SSL handshake successes of an user
     * won't be saved (used when a successful authentication of this user is made).
     * New authentication for the user won't be possible for this time. <br>
     * Reason of the use of this time: <br>
     * A new SSL session will be established by JMX after {@link JmxLoginModule}
     * process. The success of the handshake of that new SSL session should be
     * ignored for authentication check (ie in {@link JmxSslHandshakeListener})
     * because authentication has already been made by {@link JmxLoginModule}. <br>
     * Because we don't know how to link this new SSL session to the authenticated
     * user, we can just ignore new SSL handshake successes for a small period of
     * time: SSL_SUCCESS_IGNORE_TIME milliseconds. That means that any connection
     * attempt with credentials of that user will be refused for that time. <br>
     * If we don't do this, a new SSL handshake success would be saved just after
     * {@link JmxLoginModule} process, and would be kept for
     * {@link #SSL_SUCCESS_EXPIRE_TIME} milliseconds, allowing any user with any
     * trusted certificate to attempt to authenticate with credentials of the
     * processed user. <br>
     * By ignoring the next SSL handshake success of an user after a successful
     * authentication of that user, we reduce the risk of somebody that could
     * authenticate with (username, password) credentials without the client
     * certificate corresponding to the user (with another trusted client
     * certificate). The risk isn't completely gone, it is still possible between
     * time when the SSL handshake success is detected by
     * {@link JmxSslHandshakeListener#handshakeCompleted(HandshakeCompletedEvent)}
     * and time when {@link JmxLoginModule} ends its authentication process.
     */
    private static final long SSL_SUCCESS_IGNORE_TIME = 3000L;

    /**
     * List of clients's username for which the SSL handshake succeeded, saved in
     * keys of this map, values of this map are the time when the SSL handshake
     * succeeded for the last time for the given user. <br>
     * If there are several connection attempts for a same user, only the last SSL
     * handshake success time is kept. <br>
     * If saved time is positive, then the last SSL handshake success is valid for
     * {@link #SSL_SUCCESS_EXPIRE_TIME} milliseconds from the saved time. <br>
     * If saved time is negative, then it means the SSL handshake successes won't be
     * saved for {@link #SSL_SUCCESS_IGNORE_TIME} milliseconds from the saved time.
     */
    static Map<String, Long> sslSuccessClientsUsernames = new HashMap<String, Long>();

    private JmxSslHandshakeListener()
    {
    }

    public static JmxSslHandshakeListener getInstance()
    {
        if (instance == null)
        {
            instance = new JmxSslHandshakeListener();
        }
        return instance;
    }

    @Override
    public void handshakeCompleted(HandshakeCompletedEvent event)
    {
        Principal clientPrincipal = event.getLocalPrincipal();
        if (clientPrincipal != null)
        {
            String username = clientPrincipal.getName();
            if (username != null)
            {
                username = username.substring(3);
                jqmlogger.debug("SSL handshake succeeded for User[" + username + "]");
                addUserSuccessfulSslHandshake(username);
            }
        }
    }

    /**
     * Save the SSL handshake success for the given user. <br>
     * If there are several connection attempts for a same user, only the last SSL
     * handshake success time is kept. <br>
     * If the user with the provided username has been successfully authenticated
     * recently (in last {@link #SSL_SUCCESS_EXPIRE_TIME} milliseconds) by
     * {@link JmxLoginModule}, this SSL handshake success won't be saved.
     * 
     * @param username
     *                 the Common Name of the client certificate, meant to be equal
     *                 to JQM username
     */
    private void addUserSuccessfulSslHandshake(String username)
    {
        Long previousTime = sslSuccessClientsUsernames.get(username);
        if (previousTime == null || (previousTime < 0 && (System.currentTimeMillis() + previousTime > SSL_SUCCESS_IGNORE_TIME)))
        {
            sslSuccessClientsUsernames.put(username, System.currentTimeMillis());
        }
    }

    /**
     * Check if the specified username corresponds to a client certificate for which
     * an SSL handshake succeeded in last {@link #SSL_SUCCESS_EXPIRE_TIME}
     * milliseconds.
     * 
     * @param username
     *                 the Common Name of the client certificate, meant to be equal
     *                 to JQM username
     * @return true if the specified username is the Common Name of a client
     *         certificate for which an SSL handshake succeeded before calling this
     *         method in last {@link #SSL_SUCCESS_EXPIRE_TIME} milliseconds.
     * @throws LoginException
     *                        if new SSL handshake successes are being ignored.
     */
    public boolean userSucceededSslHandshake(String username) throws LoginException
    {
        Long previousTime = sslSuccessClientsUsernames.get(username);
        if (previousTime != null)
        {
            if (previousTime < 0)
            {
                if (System.currentTimeMillis() + previousTime <= SSL_SUCCESS_IGNORE_TIME)
                {
                    throw new LoginException("Another connection of this account has been made recently, please wait before reconnecting.");
                }
                else
                {
                    return false;
                }
            }
            else if (System.currentTimeMillis() - previousTime > SSL_SUCCESS_EXPIRE_TIME)
            {
                sslSuccessClientsUsernames.remove(username);
                return false;
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Ignore future SSL handshake successes of the given user for
     * {@link #SSL_SUCCESS_IGNORE_TIME} milliseconds.
     * 
     * @param username
     *                 the Common Name of the client certificate, meant to be equal
     *                 to JQM username
     * @see #SSL_SUCCESS_IGNORE_TIME
     */
    public void ignoreUserSslHandshakeSuccesses(String username)
    {
        sslSuccessClientsUsernames.put(username, -System.currentTimeMillis());
    }

    /**
     * Clear last SSL handshake success information for the given user if it is not
     * useful for any other connection attempt.
     * 
     * @param username
     *                 the Common Name of the client certificate, meant to be equal
     *                 to JQM username
     */
    public void clearUserSslHandshakeSuccess(String username)
    {
        Long previousTime = sslSuccessClientsUsernames.get(username);
        if (previousTime == null || previousTime < 0 || (System.currentTimeMillis() - previousTime > SSL_SUCCESS_EXPIRE_TIME))
        {
            sslSuccessClientsUsernames.remove(username); // previousTime is not useful for any other connection attempt, can be removed
                                                         // to free memory space.
        }
    }

}
