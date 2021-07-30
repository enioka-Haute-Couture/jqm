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
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ListenedSslRMIClientSocketFactory implements RMIClientSocketFactory, Serializable
{

    private static final long serialVersionUID = -7134357497865225601L;

    public ListenedSslRMIClientSocketFactory()
    {
    }

    public Socket createSocket(String host, int port) throws IOException
    {
        SocketFactory sf = SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) sf.createSocket(host, port);
        socket.addHandshakeCompletedListener(JmxSslHandshakeListener.getInstance());

        return socket;
    }

    public int hashCode()
    {
        return getClass().hashCode();
    }

    public boolean equals(Object obj)
    {
        return obj == this || (obj != null && getClass() == obj.getClass());
    }

}
