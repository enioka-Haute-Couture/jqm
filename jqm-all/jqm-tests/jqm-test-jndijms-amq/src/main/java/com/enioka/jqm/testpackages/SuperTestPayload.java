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

package com.enioka.jqm.testpackages;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.spi.NamingManager;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.enioka.jqm.api.JobBase;

public class SuperTestPayload extends JobBase
{

    @Override
    public void start()
    {
        System.out.println("Thread context class loader is: " + Thread.currentThread().getContextClassLoader());
        System.out.println("Class class loader used for loading test class is: " + this.getClass().getClassLoader());
        int nb = 0;

        try
        {
            // Get the QCF
            Object o = NamingManager.getInitialContext(null).lookup("jms/qcf");
            System.out.println("Received a " + o.getClass());

            // Do as cast & see if no errors
            QueueConnectionFactory qcf = (QueueConnectionFactory) o;

            // For testing purpose, we use the real AMQ object here - it is not necessary in normal JMS operations
            ActiveMQConnectionFactory tmp = (ActiveMQConnectionFactory) qcf;
            for (Object g : tmp.getProperties().keySet())
            {
                System.out.println("Property: " + g + " - " + tmp.getProperties().getProperty((String) g));
            }
            tmp = null;
            // End of test specific AMQ dependency

            // Get the Queue
            Object p = NamingManager.getInitialContext(null).lookup("jms/testqueue");
            System.out.println("Received a " + p.getClass());
            Queue q = (Queue) p;

            // Now that we are sure that JNDI works, let's write a message
            System.out.println("Opening connection & session to the broker");
            Connection connection = qcf.createConnection();
            connection.start();
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);

            System.out.println("Creating producer");
            MessageProducer producer = session.createProducer(q);
            TextMessage message = session.createTextMessage("HOUBA HOP. SIGNED: MARSUPILAMI");

            System.out.println("Sending message");
            producer.send(message);
            producer.close();
            session.commit();
            System.out.println("A message was sent to the broker");

            // Browse and check the message is there
            Connection connection2 = qcf.createConnection();
            connection2.start();
            Session session2 = connection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueBrowser qb = session2.createBrowser(q);
            // Warning is suppressed - the API gives a the choice between raw generics and unsafe cast!
            @SuppressWarnings("unchecked")
            Enumeration<TextMessage> msgs = qb.getEnumeration();
            while (msgs.hasMoreElements())
            {
                TextMessage msg = msgs.nextElement();
                System.out.println("Message received: " + msg.getText());
                nb++;
            }
            System.out.println("Browsing will end here");
            qb.close();
            System.out.println("End of browsing. Nb of message read: " + nb);

            // We are done!
            connection.close();
            connection2.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (nb == 0)
        {
            throw new RuntimeException("test has failed - no messages were received.");
        }
    }
}
