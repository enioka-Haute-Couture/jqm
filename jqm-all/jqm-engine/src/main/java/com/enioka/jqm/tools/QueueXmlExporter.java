package com.enioka.jqm.tools;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.enioka.jqm.jpamodel.JobDef;
import com.enioka.jqm.jpamodel.Queue;

class QueueXmlExporter
{
    private static Logger jqmlogger = Logger.getLogger(QueueXmlExporter.class);
    private EntityManager em = Helpers.getNewEm();

    QueueXmlExporter()
    {

    }

    void export(String path, String queueName)
    {
        Queue q = null;
        Element root = new Element("jqm");
        Document document = new Document(root);

        Element queues = new Element("queues");
        root.addContent(queues);

        try
        {
            q = em.createQuery("SELECT q FROM Queue q WHERE q.name = :n", Queue.class).setParameter("n", queueName).getSingleResult();
        }
        catch (NonUniqueResultException s)
        {
            jqmlogger.error("Queue " + queueName + " is non unique. The admin must change the queue configurations");
            return;
        }
        catch (NoResultException ss)
        {
            jqmlogger.error("This queue doesn't exist");
            return;
        }

        Element queue = new Element("queue");
        queues.addContent(queue);

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

        jds.clear();

        save(path, document);
    }

    void exportSeveral(String path, List<String> queueNames)
    {
        Queue q = null;
        Element root = new Element("jqm");
        Document document = new Document(root);

        Element queues = new Element("queues");
        root.addContent(queues);

        for (String queueName : queueNames)
        {
            try
            {
                q = em.createQuery("SELECT q FROM Queue q WHERE q.name = :n", Queue.class).setParameter("n", queueName).getSingleResult();
            }
            catch (NonUniqueResultException s)
            {
                jqmlogger.warn("Queue " + queueName + " is non unique. The admin must change the queue configuration");
            }
            catch (NoResultException ss)
            {
                jqmlogger.debug("This queue doesn't exist");
            }
            Element queue = new Element("queue");
            queues.addContent(queue);

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

            jds.clear();
        }

        save(path, document);
    }

    void exportAll(String path)
    {
        ArrayList<Queue> qs = null;
        Element root = new Element("jqm");
        Document document = new Document(root);

        Element queues = new Element("queues");
        root.addContent(queues);

        qs = (ArrayList<Queue>) em.createQuery("SELECT q FROM Queue q", Queue.class).getResultList();

        if (qs.isEmpty())
        {
            jqmlogger.warn("No queue");
        }

        for (Queue q : qs)
        {
            Element queue = new Element("queue");
            queues.addContent(queue);

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

            jds.clear();
        }

        save(path, document);
    }

    void save(String filePath, Document doc)
    {
        try
        {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            out.output(doc, new FileOutputStream(filePath));
        }
        catch (java.io.IOException e)
        {
            jqmlogger.error("could not create file", e);
        }
    }
}
