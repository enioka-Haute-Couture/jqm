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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.NoResultException;
import com.enioka.jqm.model.GlobalParameter;

class XmlConfigurationParser
{
    private static Logger jqmlogger = LoggerFactory.getLogger(XmlConfigurationParser.class);

    private XmlConfigurationParser()
    {

    }

    /**
     * Will import all configuration from an XML file. Creates and commits a transaction.
     * 
     * @param path
     * @param cnx
     * @throws JqmEngineException
     */
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
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder;

        try
        {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);

            // Schema validation
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(XmlConfigurationParser.class.getClassLoader().getResource("prm.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(doc));

            doc.getDocumentElement().normalize();

            // First parse nodes (engines)
            NodeList nodeList = doc.getElementsByTagName("node");
            for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++)
            {
                com.enioka.jqm.model.Node node;
                Node nodeNode = nodeList.item(nodeIndex);
                if (nodeNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element nodeElement = (Element) nodeNode;

                String nodeName = nodeElement.getElementsByTagName("name").item(0).getTextContent().trim();
                String listenInterface = nodeElement.getElementsByTagName("interface").item(0).getTextContent().trim();
                int webPort = Integer.parseInt(nodeElement.getElementsByTagName("webPort").item(0).getTextContent().trim());
                int jmxRegistryPort = Integer.parseInt(nodeElement.getElementsByTagName("jmxRegistryPort").item(0).getTextContent().trim());
                int jmxServerPort = Integer.parseInt(nodeElement.getElementsByTagName("jmxServerPort").item(0).getTextContent().trim());

                boolean enabled = Boolean.parseBoolean(nodeElement.getElementsByTagName("enabled").item(0).getTextContent().trim());
                boolean simpleApi = Boolean
                        .parseBoolean(nodeElement.getElementsByTagName("loadSimpleWebApi").item(0).getTextContent().trim());
                boolean clientApi = Boolean
                        .parseBoolean(nodeElement.getElementsByTagName("loadClientWebApi").item(0).getTextContent().trim());
                boolean adminApi = Boolean
                        .parseBoolean(nodeElement.getElementsByTagName("loadAdminWebApi").item(0).getTextContent().trim());

                String jobDefDirectory = nodeElement.getElementsByTagName("jobDefDirectory").item(0).getTextContent().trim();
                String tmpDirectory = nodeElement.getElementsByTagName("tmpDirectory").item(0).getTextContent().trim();
                String deliverableDirectory = nodeElement.getElementsByTagName("deliverableDirectory").item(0).getTextContent().trim();

                String logLevel = nodeElement.getElementsByTagName("logLevel").item(0).getTextContent().trim();

                try
                {
                    node = com.enioka.jqm.model.Node.select_single(cnx, "node_select_by_key", nodeName);
                    cnx.runUpdate("node_update_changed_by_id", deliverableDirectory, listenInterface, enabled, jmxRegistryPort,
                            jmxServerPort, adminApi, clientApi, simpleApi, nodeName, webPort, jobDefDirectory, logLevel, false,
                            tmpDirectory, node.getId(), deliverableDirectory, listenInterface, enabled, jmxRegistryPort, jmxServerPort,
                            adminApi, clientApi, simpleApi, nodeName, webPort, jobDefDirectory, logLevel, false, tmpDirectory);
                }
                catch (NoResultException e)
                {
                    cnx.runUpdate("node_insert", deliverableDirectory, listenInterface, enabled, jmxRegistryPort, jmxServerPort, adminApi,
                            clientApi, simpleApi, nodeName, webPort, jobDefDirectory, logLevel, false, tmpDirectory);
                }

                jqmlogger.info("Imported node  " + nodeName);
            }

            // Second parse global parameters
            NodeList gpList = doc.getElementsByTagName("parameter");
            for (int gpIndex = 0; gpIndex < gpList.getLength(); gpIndex++)
            {
                Node gpNode = gpList.item(gpIndex);
                if (gpNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element gpElement = (Element) gpNode;

                String key = gpElement.getElementsByTagName("key").item(0).getTextContent().trim();
                String value = gpElement.getElementsByTagName("value").item(0).getTextContent().trim();

                GlobalParameter.setParameter(cnx, key, value);
                jqmlogger.info("Imported global parameter key: " + key + " - value: " + value);
            }

            // Done
            cnx.commit();
        }
        catch (Exception e)
        {
            throw new JqmXmlException(
                    "an error occured while parsing the XML file " + path + ". No changes were done to the configuration.", e);
        }
    }
}
