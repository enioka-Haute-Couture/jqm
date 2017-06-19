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

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Queue;

class XmlQueueParser
{
    private static Logger jqmlogger = LoggerFactory.getLogger(XmlQueueParser.class);

    private XmlQueueParser()
    {
        // Utility class.
    }

    static void parse(String path, DbConn cnx) throws JqmXmlException
    {
        // Argument checks
        jqmlogger.trace(path);
        if (path == null || path.isEmpty())
        {
            throw new IllegalArgumentException("XML file path cannot be empty");
        }
        if (cnx == null)
        {
            throw new IllegalArgumentException("Database connection cannot be null");
        }
        File f = new File(path);
        if (f == null || !f.isFile() || !f.canRead())
        {
            throw new IllegalArgumentException("The XML file " + f + " was not found or cannot be read.");
        }

        // Create parsers
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try
        {
            Queue q;

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();

            NodeList qList = doc.getElementsByTagName("queue");

            for (int qIndex = 0; qIndex < qList.getLength(); qIndex++)
            {
                Element qElement = (Element) qList.item(qIndex);

                // Insert or update?
                String qName = qElement.getElementsByTagName("name").item(0).getTextContent();
                q = CommonXml.findQueue(qName, cnx);
                if (q == null)
                {
                    q = new Queue();
                }

                // Simple fields
                q.setName(qElement.getElementsByTagName("name").item(0).getTextContent());
                q.setDescription(qElement.getElementsByTagName("description").item(0).getTextContent());
                if (qElement.getElementsByTagName("timeToLive").getLength() == 1)
                {
                    q.setTimeToLive(Integer.parseInt(qElement.getElementsByTagName("timeToLive").item(0).getTextContent()));
                }

                // We now merge & commit - we will need to reference the queue in the next paragraph.
                q.update(cnx);
                cnx.commit();

                // Applications that should use this queue
                NodeList appList = qElement.getElementsByTagName("applicationName");
                for (int appIndex = 0; appIndex < appList.getLength(); appIndex++)
                {
                    Element appElement = (Element) appList.item(appIndex);
                    String appName = appElement.getTextContent();
                    jqmlogger.debug("Default queue of the job " + appName + " must be changed");
                    cnx.runUpdate("jd_update_set_queue_by_key", q.getId(), appName);
                }
                cnx.commit();
            }
        }
        catch (Exception e)
        {
            throw new JqmXmlException("Could not parse the Queue XML", e);
        }
    }
}
