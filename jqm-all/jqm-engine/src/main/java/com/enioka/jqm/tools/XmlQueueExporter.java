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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Queue;

class XmlQueueExporter
{
    private XmlQueueExporter()
    {
        // Static helper class
    }

    /**
     * Exports a single queue to an XML file.
     */
    static void export(String path, String queueName, EntityManager em) throws JqmEngineException
    {
        // Argument tests
        if (queueName == null)
        {
            throw new IllegalArgumentException("queue name cannot be null");
        }
        if (em == null)
        {
            throw new IllegalArgumentException("entity manager name cannot be null");
        }
        Queue q = Helpers.findQueue(queueName, em);
        if (q == null)
        {
            throw new IllegalArgumentException("there is no queue named " + queueName);
        }

        List<Queue> l = new ArrayList<Queue>();
        l.add(q);
        export(path, l, em);
    }

    /**
     * Exports all available queues to an XML file.
     */
    static void export(String path, EntityManager em) throws JqmEngineException
    {
        if (em == null)
        {
            throw new IllegalArgumentException("entity manager name cannot be null");
        }
        export(path, em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList(), em);
    }

    /**
     * Exports all available queues to an XML file.
     */
    static void export(String path, EntityManager em, List<String> qNames) throws JqmEngineException
    {
        if (em == null)
        {
            throw new IllegalArgumentException("entity manager name cannot be null");
        }
        if (qNames == null || qNames.isEmpty())
        {
            throw new IllegalArgumentException("queue names list name cannot be null or empty");
        }
        List<Queue> qList = new ArrayList<Queue>();
        for (String qn : qNames)
        {
            Queue q = Helpers.findQueue(qn, em);
            if (q == null)
            {
                throw new IllegalArgumentException("There is no queue named " + qn);
            }
            qList.add(q);
        }
        export(path, qList, em);
    }

    /**
     * Exports several (given) queues to an XML file.
     */
    static void export(String path, List<Queue> queueList, EntityManager em) throws JqmEngineException
    {
        // Argument tests
        if (path == null)
        {
            throw new IllegalArgumentException("file path cannot be null");
        }
        if (queueList == null || queueList.isEmpty())
        {
            throw new IllegalArgumentException("queue list cannot be null or empty");
        }
        if (em == null)
        {
            throw new IllegalArgumentException("entity manager name cannot be null");
        }

        // Create XML document
        Element root = new Element("jqm");
        Document document = new Document(root);
        Element queues = new Element("queues");
        root.addContent(queues);

        for (Queue q : queueList)
        {
            Element queue = getQueueElement(q, em);
            queues.addContent(queue);
        }

        // Done: save the file
        try
        {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            out.output(document, new FileOutputStream(path));
        }
        catch (java.io.IOException e)
        {
            throw new JqmEngineException("Coul npot save the XML file", e);
        }
    }

    private static Element getQueueElement(Queue q, EntityManager em)
    {
        Element queue = new Element("queue");

        Element name = new Element("name");
        name.setText(q.getName());
        Element description = new Element("description");
        description.setText(q.getDescription());
        Element timeToLive = new Element("timeToLive");
        timeToLive.setText(q.getTimeToLive() + "");

        queue.addContent(name);
        queue.addContent(description);
        queue.addContent(timeToLive);

        Element jobs = new Element("jobs");
        queue.addContent(jobs);

        ArrayList<JobDef> jds = (ArrayList<JobDef>) em.createQuery("SELECT j FROM JobDef j WHERE j.queue = :q", JobDef.class)
                .setParameter("q", q).getResultList();

        for (JobDef j : jds)
        {
            Element job = new Element("applicationName");
            job.setText(j.getApplicationName());
            jobs.addContent(job);
        }

        return queue;
    }

}
