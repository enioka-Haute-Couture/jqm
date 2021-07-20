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

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Factory used to support Java 6 because the Java 6 default
 * SslRMIServerSocketFactory doesn't support a custom SSLContext. In Java 7 or
 * greater, the default SslRMIServerSocketFactory supports a custom SSLContext
 * provided to the constructor.
 */
public class ContextfulSslRMIServerSocketFactory implements RMIServerSocketFactory, Serializable
{

    private static final long serialVersionUID = 553809401536491998L;

    private SSLContext sslctx;
    private boolean needClientAuth;

    public ContextfulSslRMIServerSocketFactory(SSLContext sslctx, boolean needClientAuth)
    {
        this.sslctx = sslctx;
        this.needClientAuth = needClientAuth;
    }

    public ServerSocket createServerSocket(int port) throws IOException
    {
        SSLServerSocketFactory sf = sslctx.getServerSocketFactory();
        SSLServerSocket socket = (SSLServerSocket) sf.createServerSocket(port);
        socket.setNeedClientAuth(needClientAuth);

        return socket;
    }

    public int hashCode()
    {
        return getClass().hashCode();
    }

    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        return true;
    }

}
