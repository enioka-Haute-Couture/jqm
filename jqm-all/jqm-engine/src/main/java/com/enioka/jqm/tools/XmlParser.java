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
import javax.persistence.TypedQuery;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.JobDefParameter;
import com.enioka.jqm.jpamodel.Queue;

class XmlParser
{
    private static Logger jqmlogger = Logger.getLogger(XmlParser.class);

    private XmlParser()
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
        if (path == null || path.isEmpty())
        {
            throw new IllegalArgumentException("XML file path cannot be empty");
        }
        jqmlogger.trace(path);
        File f = new File(path);
        if (f == null || !f.isFile())
        {
            throw new JqmEngineException("The XML file " + f + " was not found");
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Map<String, Queue> createdQueues = new HashMap<String, Queue>();

        boolean canBeRestarted = true;
        String javaClassName = null;
        Queue queue = null;
        String applicationName = null;
        String application = null;
        String module = null;
        String keyword1 = null;
        String keyword2 = null;
        String keyword3 = null;
        boolean highlander = false;
        String jarPath = null;

        // SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        // Schema schema = null;
        // schema = factory.newSchema(new File("./lib/res.xsd"));
        // Validator validator = schema.newValidator();
        // DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        // Document document = parser.parse(f);
        // validator.validate(new DOMSource(document));

        try
        {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("jqm");

            em.getTransaction().begin();

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element e = (Element) nNode;
                    NodeList nl = e.getElementsByTagName("jobDefinition");

                    for (int i = 0; i < nl.getLength(); i++)
                    {
                        Element ee = (Element) nl.item(i);

                        canBeRestarted = ("true".equals(ee.getElementsByTagName("canBeRestarted").item(0).getTextContent())) ? true : false;
                        javaClassName = ee.getElementsByTagName("javaClassName").item(0).getTextContent();

                        JobDef jd;
                        TypedQuery<JobDef> q = em.createQuery("SELECT j FROM JobDef j WHERE j.applicationName = :n", JobDef.class);
                        q.setParameter("n", ee.getElementsByTagName("name").item(0).getTextContent());
                        if (q.getResultList().size() == 1)
                        {
                            jd = q.getResultList().get(0);
                            jd.getParameters().clear();
                        }
                        else
                        {
                            jd = new JobDef();
                        }

                        // Queue
                        if (jd.getQueue() == null && ee.getElementsByTagName("queue").getLength() == 0)
                        {
                            // Not specified => default queue
                            queue = em.createQuery("SELECT q FROM Queue q WHERE q.defaultQueue = true", Queue.class).getSingleResult();
                        }
                        else
                        {
                            // Specified inside the XML. Does the queue already exist?
                            String qname = ee.getElementsByTagName("queue").item(0).getTextContent();
                            try
                            {
                                queue = em.createQuery("SELECT q FROM Queue q WHERE q.name = :name", Queue.class)
                                        .setParameter("name", qname).getSingleResult();
                                // The queue exists
                            }
                            catch (NoResultException noe)
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
                        }

                        // Easy attributes
                        applicationName = ee.getElementsByTagName("name").item(0).getTextContent();
                        application = ee.getElementsByTagName("application").item(0).getTextContent();
                        module = ee.getElementsByTagName("module").item(0).getTextContent();
                        // Keyword used to be called "other". We allow both for ascending compatibility. ("other" is deprecated - don't use)
                        if (ee.getElementsByTagName("other1").getLength() > 0 && ee.getElementsByTagName("other2").getLength() > 0
                                && ee.getElementsByTagName("other3").getLength() > 0)
                        {
                            keyword1 = ee.getElementsByTagName("other1").item(0).getTextContent();
                            keyword2 = ee.getElementsByTagName("other2").item(0).getTextContent();
                            keyword3 = ee.getElementsByTagName("other3").item(0).getTextContent();
                        }
                        else if (ee.getElementsByTagName("keyword1").getLength() > 0 && ee.getElementsByTagName("keyword2").getLength() > 0
                                && ee.getElementsByTagName("keyword3").getLength() > 0)
                        {
                            keyword1 = ee.getElementsByTagName("keyword1").item(0).getTextContent();
                            keyword2 = ee.getElementsByTagName("keyword2").item(0).getTextContent();
                            keyword3 = ee.getElementsByTagName("keyword3").item(0).getTextContent();
                        }
                        highlander = ("true".equals(ee.getElementsByTagName("highlander").item(0).getTextContent())) ? true : false;
                        jarPath = e.getElementsByTagName("path").item(0).getTextContent();

                        NodeList l = ee.getElementsByTagName("parameter");

                        for (int j = 0; j < l.getLength(); j++)
                        {
                            Element t = (Element) l.item(j);

                            JobDefParameter jdp = new JobDefParameter();
                            jdp.setKey(t.getElementsByTagName("key").item(0).getTextContent());
                            jdp.setValue(t.getElementsByTagName("value").item(0).getTextContent());
                            jd.getParameters().add(jdp);
                        }

                        jd.setCanBeRestarted(canBeRestarted);
                        jd.setJavaClassName(javaClassName);
                        jd.setQueue(queue);
                        jd.setApplicationName(applicationName);
                        jd.setApplication(application);
                        jd.setModule(module);
                        jd.setKeyword1(keyword1);
                        jd.setKeyword2(keyword2);
                        jd.setKeyword3(keyword3);
                        jd.setHighlander(highlander);
                        jd.setJarPath(jarPath);

                        em.persist(jd);
                        jqmlogger.debug("Imported application " + applicationName);
                    }
                }
            }
            em.getTransaction().commit();
        }
        catch (Exception e)
        {
            throw new JqmEngineException("an error occured while parsing the XML file. No changes were done to the configuration.", e);
        }
    }
}
