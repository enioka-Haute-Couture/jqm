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
import java.util.HashMap;
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
import com.enioka.jqm.model.Cl;
import com.enioka.jqm.model.ClEvent;
import com.enioka.jqm.model.ClHandler;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.model.Queue;

public class XmlJobDefParser
{
    private static Logger jqmlogger = LoggerFactory.getLogger(XmlJobDefParser.class);

    private XmlJobDefParser()
    {

    }

    /**
     * Will import all JobDef from an XML file. Creates and commits a transaction.
     *
     * @param path
     *            full or relative path to the deployment descriptor to read.
     * @param cnx
     *            a database connection to use with no active transaction.
     * @throws JqmEngineException
     */
    public static void parse(String path, DbConn cnx) throws JqmXmlException
    {
        parse(path, cnx, null);
    }

    /**
     * Will import all JobDef from an XML file. Creates and commits a transaction.
     *
     * @param path
     *            full or relative path to the deployment descriptor to read.
     * @param cnx
     *            a database connection to use with no active transaction.
     * @param overrideJarBasePath
     *            ignore the base path of the jar in the deployment descriptor and use this one. It must be relative to to repository root.
     * @throws JqmEngineException
     */
    public static void parse(String path, DbConn cnx, String overrideJarBasePath) throws JqmXmlException
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

        // Result fields
        Map<String, Long> createdQueues = new HashMap<>();
        JobDef jd = null;
        Long queueId = null;

        try
        {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);

            // Schema validation
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(XmlJobDefParser.class.getClassLoader().getResource("res.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(doc));

            doc.getDocumentElement().normalize();

            // First parse CLs
            NodeList clList = doc.getElementsByTagName("context");
            for (int clIndex = 0; clIndex < clList.getLength(); clIndex++)
            {
                Cl cl;
                Node clNode = clList.item(clIndex);
                if (clNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element clElement = (Element) clNode;

                String clName = clElement.getElementsByTagName("name").item(0).getTextContent().trim();

                try
                {
                    cl = Cl.select_key(cnx, clName);

                    // Remove all handlers - we will recreate them.
                    cnx.runUpdate("clehprm_delete_all_for_cl", cl.getId());
                    cnx.runUpdate("cleh_delete_all_for_cl", cl.getId());
                }
                catch (NoResultException e)
                {
                    Cl.create(cnx, clName, false, null, false, true, null);
                    cl = Cl.select_key(cnx, clName);
                }

                // Basic attributes (with defaults)
                if (clElement.getElementsByTagName("childFirst").getLength() > 0)
                {
                    cl.setChildFirst(Boolean.parseBoolean(clElement.getElementsByTagName("childFirst").item(0).getTextContent()));
                }
                else
                {
                    cl.setChildFirst(false);
                }
                if (clElement.getElementsByTagName("tracingEnabled").getLength() > 0)
                {
                    cl.setTracingEnabled(Boolean.parseBoolean(clElement.getElementsByTagName("tracingEnabled").item(0).getTextContent()));
                }
                else
                {
                    cl.setTracingEnabled(false);
                }
                if (clElement.getElementsByTagName("persistent").getLength() > 0)
                {
                    cl.setPersistent(Boolean.parseBoolean(clElement.getElementsByTagName("persistent").item(0).getTextContent()));
                }
                else
                {
                    cl.setPersistent(true);
                }
                if (clElement.getElementsByTagName("hiddenJavaClasses").getLength() > 0)
                {
                    cl.setHiddenClasses(clElement.getElementsByTagName("hiddenJavaClasses").item(0).getTextContent().trim());
                }
                else
                {
                    cl.setHiddenClasses(null);
                }
                if (clElement.getElementsByTagName("runners").getLength() > 0)
                {
                    cl.setAllowedRunners(clElement.getElementsByTagName("runners").item(0).getTextContent().trim());
                }
                else
                {
                    cl.setAllowedRunners(null);
                }
                cl.update(cnx);

                if (clElement.getElementsByTagName("eventHandlers").getLength() > 0)
                {
                    NodeList handlersList = ((Element) clElement.getElementsByTagName("eventHandlers").item(0))
                            .getElementsByTagName("handler");
                    for (int j = 0; j < handlersList.getLength(); j++)
                    {
                        Element hElement = (Element) handlersList.item(j);
                        Map<String, String> handlerPrms = new HashMap<>();

                        if (hElement.getElementsByTagName("parameters").getLength() > 0)
                        {
                            NodeList prmList = ((Element) hElement.getElementsByTagName("parameters").item(0))
                                    .getElementsByTagName("parameter");
                            for (int k = 0; k < prmList.getLength(); k++)
                            {
                                Element prmElement = (Element) prmList.item(k);
                                handlerPrms.put(prmElement.getElementsByTagName("key").item(0).getTextContent(),
                                        prmElement.getElementsByTagName("value").item(0).getTextContent());
                            }
                        }

                        ClHandler.create(cnx, ClEvent.JI_STARTING,
                                hElement.getElementsByTagName("className").item(0).getTextContent().trim(), cl.getId(), handlerPrms);
                    }
                }
            }

            // Second parse jars
            NodeList jarList = doc.getElementsByTagName("jar");
            for (int jarIndex = 0; jarIndex < jarList.getLength(); jarIndex++)
            {
                Node jarNode = jarList.item(jarIndex);
                if (jarNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element jarElement = (Element) jarNode;
                NodeList jdList = jarElement.getElementsByTagName("jobDefinition");

                // Potentially remap jar path
                String jarPath = jarElement.getElementsByTagName("path").item(0).getTextContent().trim();
                if (overrideJarBasePath != null)
                {
                    String fileName = (new File(jarPath)).getName();
                    jarPath = (new File(overrideJarBasePath, fileName)).getPath();
                }

                for (int jdIndex = 0; jdIndex < jdList.getLength(); jdIndex++)
                {
                    Element jdElement = (Element) jdList.item(jdIndex);

                    // Retrieve existing JobDef (if exists)
                    String name = jdElement.getElementsByTagName("name").item(0).getTextContent().trim();
                    try
                    {
                        jd = JobDef.select_key(cnx, name);
                    }
                    catch (NoResultException e)
                    {
                        jd = new JobDef();
                    }

                    // Retrieve the Queue on which to run the JobDef
                    Queue q = null;
                    try
                    {
                        q = jd.getQueue(cnx);
                    }
                    catch (NoResultException e)
                    {
                        // Nothing.
                    }
                    if (q == null && jdElement.getElementsByTagName("queue").getLength() != 0)
                    {
                        // Specified inside the XML,nothing yet in DB. Does the specified queue already exist?
                        String qname = jdElement.getElementsByTagName("queue").item(0).getTextContent().trim();
                        try
                        {
                            queueId = Queue.select_key(cnx, qname).getId();
                        }
                        catch (NoResultException e)
                        {
                            // The queue must be created.
                            if (createdQueues.containsKey(qname))
                            {
                                queueId = createdQueues.get(qname);
                            }
                            else
                            {
                                queueId = Queue.create(cnx, qname, "Created from a jobdef import. Description should be set later", false);
                                createdQueues.put(qname, queueId);
                            }
                        }
                        jd.setQueue(queueId);
                    }
                    else if (q == null)
                    {
                        // Queue not specified in XML (and no queue already specified inside DB) => default queue
                        queueId = cnx.runSelectSingle("q_select_default", Long.class);
                        jd.setQueue(queueId);
                    }

                    // Simple jar attributes
                    jd.setJarPath(jarPath);
                    jd.setPathType(PathType.valueOf(jarElement.getElementsByTagName("pathType").getLength() > 0
                            ? jarElement.getElementsByTagName("pathType").item(0).getTextContent().trim()
                            : "FS"));

                    // Simple JD attributes
                    jd.setCanBeRestarted(
                            "true".equals(jdElement.getElementsByTagName("canBeRestarted").item(0).getTextContent().trim()) ? true : false);
                    jd.setJavaClassName(jdElement.getElementsByTagName("javaClassName").item(0).getTextContent().trim());
                    jd.setDescription(jdElement.getElementsByTagName("description").item(0).getTextContent());
                    jd.setApplicationName(name);
                    jd.setModule(jdElement.getElementsByTagName("module").item(0).getTextContent());
                    jd.setHighlander(
                            "true".equals(jdElement.getElementsByTagName("highlander").item(0).getTextContent().trim()) ? true : false);

                    // Classifier
                    if (jdElement.getElementsByTagName("application").getLength() > 0)
                    {
                        jd.setApplication(jdElement.getElementsByTagName("application").item(0).getTextContent());
                    }
                    else
                    {
                        jd.setApplication(null);
                    }

                    // Keyword used to be called "other". We allow both for ascending compatibility. ("other" is deprecated - don't use)
                    if (jdElement.getElementsByTagName("other1").getLength() > 0)
                    {
                        jd.setKeyword1(jdElement.getElementsByTagName("other1").item(0).getTextContent());
                    }
                    else
                    {
                        jd.setKeyword1(null);
                    }
                    if (jdElement.getElementsByTagName("keyword1").getLength() > 0)
                    {
                        jd.setKeyword1(jdElement.getElementsByTagName("keyword1").item(0).getTextContent());
                    }
                    else
                    {
                        jd.setKeyword1(null);
                    }
                    if (jdElement.getElementsByTagName("other2").getLength() > 0)
                    {
                        jd.setKeyword2(jdElement.getElementsByTagName("other2").item(0).getTextContent());
                    }
                    else
                    {
                        jd.setKeyword2(null);
                    }
                    if (jdElement.getElementsByTagName("keyword2").getLength() > 0)
                    {
                        jd.setKeyword2(jdElement.getElementsByTagName("keyword2").item(0).getTextContent());
                    }
                    else
                    {
                        jd.setKeyword2(null);
                    }
                    if (jdElement.getElementsByTagName("other3").getLength() > 0)
                    {
                        jd.setKeyword3(jdElement.getElementsByTagName("other3").item(0).getTextContent());
                    }
                    else
                    {
                        jd.setKeyword3(null);
                    }
                    if (jdElement.getElementsByTagName("keyword3").getLength() > 0)
                    {
                        jd.setKeyword3(jdElement.getElementsByTagName("keyword3").item(0).getTextContent());
                    }
                    else
                    {
                        jd.setKeyword3(null);
                    }

                    // Class loading
                    if (jdElement.getElementsByTagName("executionContext").getLength() > 0)
                    {
                        String clName = jdElement.getElementsByTagName("executionContext").item(0).getTextContent();
                        try
                        {
                            jd.setClassLoader(Cl.select_key(cnx, clName).getId());
                        }
                        catch (NoResultException e)
                        {
                            jqmlogger.error("Incorrect deployment descriptor: a job definition is using undefined context " + clName);
                        }
                    }
                    else
                    {
                        jd.setClassLoader(null);
                    }

                    // Alert time
                    if (jdElement.getElementsByTagName("reasonableRuntimeLimitMinute").getLength() > 0)
                    {
                        jd.setMaxTimeRunning(
                                Integer.parseInt(jdElement.getElementsByTagName("reasonableRuntimeLimitMinute").item(0).getTextContent()));
                    }
                    else
                    {
                        jd.setMaxTimeRunning(null);
                    }

                    // Parameters
                    Map<String, String> parameters = new HashMap<>();
                    NodeList prmList = jdElement.getElementsByTagName("parameter");
                    for (int prmIndex = 0; prmIndex < prmList.getLength(); prmIndex++)
                    {
                        Element prmElement = (Element) prmList.item(prmIndex);
                        parameters.put(prmElement.getElementsByTagName("key").item(0).getTextContent(),
                                prmElement.getElementsByTagName("value").item(0).getTextContent());
                    }

                    jd.update(cnx, parameters);
                    jqmlogger.info("Imported application " + jd.getApplicationName());
                }

            }
            cnx.commit();
        }
        catch (Exception e)
        {
            throw new JqmXmlException(
                    "an error occured while parsing the XML file " + path + ". No changes were done to the configuration.", e);
        }
    }
}
