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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxSslHandshakeListener implements HandshakeCompletedListener
{
    private static JmxSslHandshakeListener instance;
    private Logger jqmlogger = LoggerFactory.getLogger(JmxSslHandshakeListener.class);

    /**
     * Time in milliseconds during which a SSL handshake success is saved for an
     * user. <br>
     * During this time, considering a user who attempted to connect with SSL client
     * authentication providing the certificate of an existing user named USERNAME
     * (Common Name of the certificate), any client certificate can be used to
     * authenticate with the (USERNAME, password of USERNAME) credentials, because
     * it doesn't seem that we have a mean to know which client certificate
     * corresponds to the user processed in JmxLoginModule.
     */
    private static final long SSL_SUCCESS_EXPIRE_TIME = 3000L;

    /**
     * List of clients's username for which the SSL handshake succeeded, saved in
     * keys of this map, values of this map are the time when the SSL handshake
     * succeeded for the last time for the given user. <br>
     * If there are several connection attempts for a same user, only the last SSL
     * handshake success time is kept.
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
            String username = clientPrincipal.getName().substring(3);
            jqmlogger.debug("SSL handshake succeeded for User[" + username + "]");
            addUserSuccessfulSslHandshake(username);
        }
    }

    /**
     * Save the SSL handshake success for the given user. <br>
     * If there are several connection attempts for a same user, only the last SSL
     * handshake success time is kept.
     * 
     * @param username
     *                 the Common Name of the client certificate, meant to be equal
     *                 to JQM username
     */
    private void addUserSuccessfulSslHandshake(String username)
    {
        sslSuccessClientsUsernames.put(username, System.currentTimeMillis());
    }

    /**
     * Checks if the specified username corresponds to a client certificate for
     * which an SSL handshake succeeded in last {@link #SSL_SUCCESS_EXPIRE_TIME}
     * milliseconds.
     * 
     * @param username
     *                 the Common Name of the client certificate, meant to be equal
     *                 to JQM username
     * @return true if the specified username is the Common Name of a client
     *         certificate for which an SSL handshake succeeded before calling this
     *         method in last {@link #SSL_SUCCESS_EXPIRE_TIME} milliseconds.
     */
    public boolean userSucceededSslHandshake(String username)
    {
        Long lastSuccess = sslSuccessClientsUsernames.get(username);
        if (lastSuccess != null)
        {
            if (System.currentTimeMillis() - lastSuccess > SSL_SUCCESS_EXPIRE_TIME)
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

}
