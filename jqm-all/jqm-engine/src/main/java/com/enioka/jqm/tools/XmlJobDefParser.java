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
                    jd = Helpers.findJobDef(jdElement.getElementsByTagName("name").item(0).getTextContent(), em);
                    if (jd == null)
                    {
                        jd = new JobDef();
                    }

                    // Retrieve the Queue on which to run the JobDef
                    if (jd.getQueue() == null && jdElement.getElementsByTagName("queue").getLength() != 0)
                    {
                        // Specified inside the XML,nothing yet in DB. Does the queue already exist?
                        String qname = jdElement.getElementsByTagName("queue").item(0).getTextContent();
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
                    jd.setJarPath(jarElement.getElementsByTagName("path").item(0).getTextContent());
                    jd.setPathType(PathType.valueOf(jarElement.getElementsByTagName("pathType").getLength() > 0
                            ? jarElement.getElementsByTagName("pathType").item(0).getTextContent() : "FS"));

                    // Simple JD attributes
                    jd.setCanBeRestarted(
                            "true".equals(jdElement.getElementsByTagName("canBeRestarted").item(0).getTextContent()) ? true : false);
                    jd.setJavaClassName(jdElement.getElementsByTagName("javaClassName").item(0).getTextContent());
                    jd.setDescription(jdElement.getElementsByTagName("description").item(0).getTextContent());
                    jd.setApplicationName(jdElement.getElementsByTagName("name").item(0).getTextContent());
                    if (jdElement.getElementsByTagName("application").getLength() > 0)
                    {
                        jd.setApplication(jdElement.getElementsByTagName("application").item(0).getTextContent());
                    }
                    jd.setModule(jdElement.getElementsByTagName("module").item(0).getTextContent());
                    jd.setHighlander("true".equals(jdElement.getElementsByTagName("highlander").item(0).getTextContent()) ? true : false);

                    // Keyword used to be called "other". We allow both for ascending compatibility. ("other" is deprecated - don't use)
                    if (jdElement.getElementsByTagName("other1").getLength() > 0)
                    {
                        jd.setKeyword1(jdElement.getElementsByTagName("other1").item(0).getTextContent());
                    }
                    if (jdElement.getElementsByTagName("keyword1").getLength() > 0)
                    {
                        jd.setKeyword1(jdElement.getElementsByTagName("keyword1").item(0).getTextContent());
                    }
                    if (jdElement.getElementsByTagName("other2").getLength() > 0)
                    {
                        jd.setKeyword2(jdElement.getElementsByTagName("other2").item(0).getTextContent());
                    }
                    if (jdElement.getElementsByTagName("keyword2").getLength() > 0)
                    {
                        jd.setKeyword2(jdElement.getElementsByTagName("keyword2").item(0).getTextContent());
                    }
                    if (jdElement.getElementsByTagName("other3").getLength() > 0)
                    {
                        jd.setKeyword3(jdElement.getElementsByTagName("other3").item(0).getTextContent());
                    }
                    if (jdElement.getElementsByTagName("keyword3").getLength() > 0)
                    {
                        jd.setKeyword3(jdElement.getElementsByTagName("keyword3").item(0).getTextContent());
                    }
                    if (jdElement.getElementsByTagName("specificIsolationContext").getLength() > 0)
                    {
                        jd.setSpecificIsolationContext(jdElement.getElementsByTagName("specificIsolationContext").item(0).getTextContent());
                    }
                    if (jdElement.getElementsByTagName("childFirstClassLoader").getLength() > 0)
                    {
                        jd.setChildFirstClassLoader(
                                "true".equals(jdElement.getElementsByTagName("childFirstClassLoader").item(0).getTextContent()) ? true
                                        : false);
                    }
                    if (jdElement.getElementsByTagName("hiddenJavaClasses").getLength() > 0)
                    {
                        jd.setHiddenJavaClasses(jdElement.getElementsByTagName("hiddenJavaClasses").item(0).getTextContent());
                    }

                    // Alert time
                    if (jdElement.getElementsByTagName("reasonableRuntimeLimitMinute").getLength() > 0)
                    {
                        jd.setMaxTimeRunning(
                                Integer.parseInt(jdElement.getElementsByTagName("reasonableRuntimeLimitMinute").item(0).getTextContent()));
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
