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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.enioka.jqm.jpamodel.Cl;
import com.enioka.jqm.jpamodel.ClEvent;
import com.enioka.jqm.jpamodel.ClHandler;
import com.enioka.jqm.jpamodel.ClHandlerParameter;
import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDef.PathType;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.Queue;

class XmlJobDefParser
{
    private static Logger jqmlogger = Logger.getLogger(XmlJobDefParser.class);

    private XmlJobDefParser()
    {

    }

    /**
     * Will import all JobDef from an XML file. Must not be called from within an open JPA transaction.
     * 
     * @param path
     * @param em
     * @throws JqmEngineException
     */
    static void parse(String path, EntityManager em) throws JqmEngineException
    {
        // Argument checks
        jqmlogger.trace(path);
        if (path == null || path.isEmpty())
        {
            throw new IllegalArgumentException("XML file path cannot be empty");
        }
        if (em == null)
        {
            throw new IllegalArgumentException("EntityManager cannot be null");
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
        Map<String, Queue> createdQueues = new HashMap<String, Queue>();
        JobDef jd = null;
        Queue queue = null;

        try
        {
            em.getTransaction().begin();
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);

            // Schema validation
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File("./lib/res.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(doc));

            doc.getDocumentElement().normalize();

            // First parse CLs
            NodeList clList = doc.getElementsByTagName("context");
            int nbCl = 0;
            for (int clIndex = 0; clIndex < clList.getLength(); clIndex++)
            {
                Cl cl;
                nbCl++;
                Node clNode = clList.item(clIndex);
                if (clNode.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                Element clElement = (Element) clNode;

                String clName = clElement.getElementsByTagName("name").item(0).getTextContent().trim();

                try
                {
                    cl = em.createQuery("SELECT q FROM Cl q WHERE q.name=:name", Cl.class).setParameter("name", clName).getSingleResult();
                }
                catch (NoResultException e)
                {
                    cl = new Cl();
                    cl.setName(clName);
                    em.persist(cl);
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

                cl.getHandlers().clear();
                if (clElement.getElementsByTagName("eventHandlers").getLength() > 0)
                {
                    NodeList handlersList = ((Element) clElement.getElementsByTagName("eventHandlers").item(0))
                            .getElementsByTagName("handler");
                    for (int j = 0; j < handlersList.getLength(); j++)
                    {
                        Element hElement = (Element) handlersList.item(j);
                        ClHandler handler = new ClHandler();
                        cl.getHandlers().add(handler);

                        handler.setClassName(hElement.getElementsByTagName("className").item(0).getTextContent().trim());
                        handler.setEventType(ClEvent.JI_STARTING);

                        if (hElement.getElementsByTagName("parameters").getLength() > 0)
                        {
                            NodeList prmList = ((Element) hElement.getElementsByTagName("parameters").item(0))
                                    .getElementsByTagName("parameter");
                            for (int k = 0; k < prmList.getLength(); k++)
                            {
                                Element prmElement = (Element) prmList.item(k);
                                ClHandlerParameter prm = new ClHandlerParameter();
                                prm.setKey(prmElement.getElementsByTagName("key").item(0).getTextContent());
                                prm.setValue(prmElement.getElementsByTagName("value").item(0).getTextContent());
                                handler.getParameters().add(prm);
                            }
                        }
                    }
                }
            }
            if (nbCl > 0)
            {
                em.getTransaction().commit();
                em.getTransaction().begin();
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

                for (int jdIndex = 0; jdIndex < jdList.getLength(); jdIndex++)
                {
                    Element jdElement = (Element) jdList.item(jdIndex);

                    // Retrieve existing JobDef (if exists)
                    String name = jdElement.getElementsByTagName("name").item(0).getTextContent().trim();
                    jd = Helpers.findJobDef(name, em);
                    if (jd == null)
                    {
                        jd = new JobDef();
                    }

                    // Retrieve the Queue on which to run the JobDef
                    if (jd.getQueue() == null && jdElement.getElementsByTagName("queue").getLength() != 0)
                    {
                        // Specified inside the XML,nothing yet in DB. Does the queue already exist?
                        String qname = jdElement.getElementsByTagName("queue").item(0).getTextContent().trim();
                        queue = Helpers.findQueue(qname, em);
                        if (queue == null)
                        {
                            // The queue must be created.
                            if (createdQueues.containsKey(qname))
                            {
                                queue = createdQueues.get(qname);
                            }
                            else
                            {
                                queue = new Queue();
                                queue.setDescription("Created from a jobdef import. Description should be set later");
                                queue.setName(qname);
                                em.persist(queue);
                                createdQueues.put(qname, queue);
                            }
                        }
                        jd.setQueue(queue);
                    }
                    else if (jd.getQueue() == null)
                    {
                        // Not specified (and no queue specified inside DB) => default queue
                        queue = em.createQuery("SELECT q FROM Queue q WHERE q.defaultQueue = true", Queue.class).getSingleResult();
                        jd.setQueue(queue);
                    }

                    // Simple jar attributes
                    jd.setJarPath(jarElement.getElementsByTagName("path").item(0).getTextContent().trim());
                    jd.setPathType(PathType.valueOf(jarElement.getElementsByTagName("pathType").getLength() > 0
                            ? jarElement.getElementsByTagName("pathType").item(0).getTextContent().trim() : "FS"));

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
                        try
                        {
                            jd.setCl(em.createQuery("SELECT q FROM Cl q WHERE q.name=:name", Cl.class)
                                    .setParameter("name", jdElement.getElementsByTagName("executionContext").item(0).getTextContent())
                                    .getSingleResult());
                        }
                        catch (NoResultException e)
                        {
                            jqmlogger.fatal("Incorrect deployment descriptor: a job definition is using undefined context "
                                    + jdElement.getElementsByTagName("executionContext").item(0).getTextContent());
                        }
                    }
                    else
                    {
                        jd.setCl(null);
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
                    for (JobDefParameter jdp : jd.getParameters())
                    {
                        em.remove(jdp);
                    }
                    jd.getParameters().clear();
                    NodeList prmList = jdElement.getElementsByTagName("parameter");
                    for (int prmIndex = 0; prmIndex < prmList.getLength(); prmIndex++)
                    {
                        Element prmElement = (Element) prmList.item(prmIndex);

                        JobDefParameter jdp = new JobDefParameter();
                        jdp.setKey(prmElement.getElementsByTagName("key").item(0).getTextContent());
                        jdp.setValue(prmElement.getElementsByTagName("value").item(0).getTextContent());
                        jd.getParameters().add(jdp);
                    }

                    em.persist(jd);
                    jqmlogger.info("Imported application " + jd.getApplicationName());
                }

            }
            em.getTransaction().commit();
        }
        catch (Exception e)
        {
            throw new JqmEngineException(
                    "an error occured while parsing the XML file " + path + ". No changes were done to the configuration.", e);
        }
    }
}
