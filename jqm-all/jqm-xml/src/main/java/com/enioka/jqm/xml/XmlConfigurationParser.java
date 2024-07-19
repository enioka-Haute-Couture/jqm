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

package com.enioka.jqm.xml;

import java.io.File;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.enioka.jqm.model.DeploymentParameter;
import com.enioka.jqm.model.GlobalParameter;
import com.enioka.jqm.model.JndiObjectResource;
import com.enioka.jqm.model.Queue;

/**
 * Highly non-efficient way to import cluster parameters. Created to make it easier to configure Docker containers. Should not be used
 * otherwise.
 */
public class XmlConfigurationParser
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
    public static void parse(String path, DbConn cnx) throws JqmXmlException
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
            NodeList gpList = ((Element) doc.getElementsByTagName("globalParameters").item(0)).getElementsByTagName("parameter");
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

            // Mapping cache
            List<DeploymentParameter> mappings = DeploymentParameter.select(cnx, "dp_select_all_with_names");

            // Queues
            cnx.runUpdate("q_update_default_none");
            Element dqElement = (Element) doc.getElementsByTagName("defaultQueueName").item(0);
            String dqName = dqElement.getTextContent().trim();
            NodeList qList = doc.getElementsByTagName("queue");
            for (int qIndex = 0; qIndex < qList.getLength(); qIndex++)
            {
                Node qNode = qList.item(qIndex);
                if (qNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element qElement = (Element) qNode;

                String name = qElement.getElementsByTagName("name").item(0).getTextContent().trim();
                String description = qElement.getElementsByTagName("description").item(0).getTextContent().trim();

                Queue q = CommonXml.findQueue(name, cnx);
                if (q == null)
                {
                    q = new Queue();
                }

                // Simple fields
                q.setName(qElement.getElementsByTagName("name").item(0).getTextContent());
                q.setDescription(qElement.getElementsByTagName("description").item(0).getTextContent());

                // Default?
                q.setDefaultQueue(dqName.equals(name));

                // Merge
                q.update(cnx);

                long queueId = Queue.select_key(cnx, name).getId();

                // Mappings
                NodeList mList = qElement.getElementsByTagName("mapping");
                for (int mIndex = 0; mIndex < mList.getLength(); mIndex++)
                {
                    Node mNode = mList.item(mIndex);
                    if (mNode.getNodeType() != Node.ELEMENT_NODE)
                    {
                        continue;
                    }
                    Element mElement = (Element) mNode;

                    String nodeName = mElement.getElementsByTagName("nodeName").item(0).getTextContent();
                    int maxThreads = Integer.parseInt(mElement.getElementsByTagName("maxThreads").item(0).getTextContent().trim());
                    int pollingIntervalMs = Integer
                            .parseInt(mElement.getElementsByTagName("pollingIntervalMs").item(0).getTextContent().trim());
                    boolean enabled = Boolean.parseBoolean(mElement.getElementsByTagName("enabled").item(0).getTextContent().trim());

                    // existing mapping?
                    long nodeId = com.enioka.jqm.model.Node.select_single(cnx, "node_select_by_key", nodeName).getId();

                    DeploymentParameter dp = null;
                    for (DeploymentParameter dpp : mappings)
                    {
                        if (dpp.getQueue() == queueId && dpp.getNode() == nodeId)
                        {
                            dp = dpp;
                            break;
                        }
                    }

                    if (dp != null)
                    {
                        cnx.runUpdate("dp_update_changed_by_id", enabled, maxThreads, pollingIntervalMs, nodeId, queueId, dp.getId(),
                                enabled, maxThreads, pollingIntervalMs, nodeId, queueId);
                    }
                    else
                    {
                        DeploymentParameter.create(cnx, enabled, nodeId, maxThreads, pollingIntervalMs, queueId);
                    }
                }

                // Done
                jqmlogger.info("Imported queue: " + name + " - " + description);
            }

            // JNDI
            NodeList srcList = doc.getElementsByTagName("resource");
            for (int srcIndex = 0; srcIndex < srcList.getLength(); srcIndex++)
            {
                Node srcNode = srcList.item(srcIndex);
                if (srcNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element srcElement = (Element) srcNode;

                String alias = srcElement.getElementsByTagName("alias").item(0).getTextContent().trim();
                String type = srcElement.getElementsByTagName("type").item(0).getTextContent().trim();
                String srcFactory = srcElement.getElementsByTagName("factory").item(0).getTextContent().trim();
                String description = srcElement.getElementsByTagName("description").item(0).getTextContent().trim();
                boolean singleton = Boolean.parseBoolean(srcElement.getElementsByTagName("singleton").item(0).getTextContent().trim());

                NodeList prmList = srcElement.getElementsByTagName("parameter");
                Map<String, String> prms = new HashMap<>();
                for (int prmIndex = 0; prmIndex < prmList.getLength(); prmIndex++)
                {
                    Node prmNode = prmList.item(prmIndex);
                    if (prmNode.getNodeType() != Node.ELEMENT_NODE)
                    {
                        continue;
                    }
                    Element prmElement = (Element) prmNode;

                    String key = prmElement.getElementsByTagName("key").item(0).getTextContent().trim();
                    String value = prmElement.getElementsByTagName("value").item(0).getTextContent().trim();
                    prms.put(key, value);
                }

                try
                {
                    JndiObjectResource src = JndiObjectResource.select_alias(cnx, alias);

                    cnx.runUpdate("jndi_update_changed_by_id", "CONTAINER", description, srcFactory, alias, singleton, null, type,
                            src.getId(), "CONTAINER", description, srcFactory, alias, singleton, null, type);

                    // Sync parameters too
                    List<String> existingKeys = cnx.runSelectColumn("jndiprm_select_all_in_jndisrc", 2, String.class, src.getId());
                    for (Map.Entry<String, String> e : prms.entrySet())
                    {
                        if (existingKeys.contains(e.getKey()))
                        {
                            // Update
                            cnx.runUpdate("jndiprm_update_changed_by_id", e.getValue(), src.getId(), e.getKey(), e.getValue());
                        }
                        else
                        {
                            // Insert
                            cnx.runUpdate("jndiprm_insert", e.getKey(), e.getValue(), src.getId());
                        }
                    }

                    ResultSet rs = cnx.runSelect("jndiprm_select_all_in_jndisrc", src.getId());
                    while (rs.next())
                    {
                        String key = rs.getString(2);
                        int id = rs.getInt(1);

                        if (!prms.containsKey(key))
                        {
                            cnx.runUpdate("jndiprm_delete_by_id", id);
                        }
                    }
                    rs.close();
                }
                catch (NoResultException e)
                {
                    JndiObjectResource.create(cnx, alias, type, srcFactory, description, singleton, prms);
                }

                jqmlogger.info("Imported JNDI alias: " + alias);
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
