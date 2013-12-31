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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Queue;


public class QueueXmlParser
{
	private static Logger jqmlogger = Logger.getLogger(QueueXmlParser.class);
	private EntityManager em = Helpers.getNewEm();

	private String name = null;
	private String description = null;
	private String maxTempInQueue = null;
	private ArrayList<String> jobs = new ArrayList<String>();

	public QueueXmlParser()
	{
	}

	void parse(String path) throws ParserConfigurationException, IOException
	{
		File f = new File(path);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		try
		{
			jqmlogger.debug(f.getPath());
			jqmlogger.debug("Working Directory = " + System.getProperty("user.dir"));

			Queue q;

			if (f == null || !f.isFile())
			{
				throw new FileNotFoundException("The XML file " + f + " was not found");
			}

			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);

			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("jqm");

			// Get the information about the queue
			for (int temp = 0; temp < nList.getLength(); temp++)
			{
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element e = (Element) nNode;

					NodeList nl = e.getElementsByTagName("queue");

					for (int i = 0; i < nl.getLength(); i++)
					{
						Element ee = (Element) nl.item(i);

						name = ee.getElementsByTagName("name").item(0).getTextContent();
						description = ee.getElementsByTagName("description").item(0).getTextContent();
						maxTempInQueue = ee.getElementsByTagName("maxTempInQueue").item(0).getTextContent();

						jqmlogger.debug("Name: " + name);
						jqmlogger.debug("Description: " + description);
						jqmlogger.debug("maxTempInQueue: " + maxTempInQueue);

						// The list must be purged
						jobs.clear();

						NodeList l = ee.getElementsByTagName("applicationName");
						jqmlogger.debug(l.getLength());

						for (int j = 0; j < l.getLength(); j++)
						{
							Element t = (Element) l.item(j);

							jqmlogger.debug("Default queue of the job " + t.getTextContent() + " must be changed");
							jobs.add(t.getTextContent());
						}

						// We must check if the queue already exist and add it
						try
						{
							q = em.createQuery("SELECT q FROM Queue q WHERE q.name = :n", Queue.class).setParameter("n", name).getSingleResult();
							jqmlogger.info("The queue " + name + "already exists. The information will be overrided");
							q.setDescription(description);
							q.setMaxTempInQueue(Integer.parseInt(maxTempInQueue));
						} catch (NonUniqueResultException s)
						{
							jqmlogger.warn("Queue " + name + " is non unique. The admin must change the queue configurations");
						} catch (NoResultException ss)
						{
							jqmlogger.debug("The queue will be created");
							em.getTransaction().begin();
							Queue queue = new Queue();

							queue.setDefaultQueue(false);
							queue.setDescription(description);
							queue.setMaxTempInQueue(Integer.parseInt(maxTempInQueue));
							queue.setMaxTempRunning(0);
							queue.setName(name);

							em.persist(queue);
							em.getTransaction().commit();
						}

						for (String n : jobs)
						{
							jqmlogger.debug("The jobs will be changed");
							try
							{
								JobDef j = em.createQuery("SELECT j FROM JobDef j WHERE j.applicationName = :n", JobDef.class).setParameter("n", n).getSingleResult();
								Queue queue = em.createQuery("SELECT q FROM Queue q WHERE q.name = :n", Queue.class).setParameter("n", name).getSingleResult();
								em.getTransaction().begin();
								j.setQueue(queue);
								em.getTransaction().commit();
								jqmlogger.debug("New configurations applied");
							}
							catch (NonUniqueResultException s)
							{
								jqmlogger.warn("Queue " + name + " or the JobDef "+ n + " is non unique. " +
										"The admin must change the queue or the JobDef configurations");
								jqmlogger.warn("This job will be ignored");

							}
							catch (NoResultException ss)
							{
								jqmlogger.debug("Can't find the queue " + name + "or the Job " + n);
							}
						}
					}
				}
			}
		}
		catch (ParserConfigurationException e)
		{
			jqmlogger.error(e);
		}
		catch (IOException e)
		{
			jqmlogger.error("Invalid xml. Please check the xml & its filepath", e);
		}
		catch (Exception e)
		{
			jqmlogger.error("Invalid xml. Please check the xml & its filepath", e);
		}
	}
}
