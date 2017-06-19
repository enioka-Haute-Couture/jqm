/**
 * Copyright © 2016 enioka. All rights reserved
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.model.Cl;
import com.enioka.jqm.model.ClHandler;
import com.enioka.jqm.model.JobDef;
import com.enioka.jqm.model.JobDefParameter;

class XmlJobDefExporter
{
    private XmlJobDefExporter()
    {
        // Static helper class
    }

    /**
     * Exports all available queues to an XML file.
     */
    static void export(String path, DbConn cnx) throws JqmXmlException
    {
        if (cnx == null)
        {
            throw new IllegalArgumentException("database connection cannot be null");
        }
        export(path, JobDef.select(cnx, "jd_select_all"), cnx);
    }

    /**
     * Exports several (given) job def to an XML file.
     */
    static void export(String xmlPath, List<JobDef> jobDefList, DbConn cnx) throws JqmXmlException
    {
        // Argument tests
        if (xmlPath == null)
        {
            throw new IllegalArgumentException("file path cannot be null");
        }
        if (jobDefList == null || jobDefList.isEmpty())
        {
            throw new IllegalArgumentException("job def list cannot be null or empty");
        }
        if (cnx == null)
        {
            throw new IllegalArgumentException("database connection name cannot be null");
        }

        Collections.sort(jobDefList, new Comparator<JobDef>()
        {
            @Override
            public int compare(JobDef o1, JobDef o2)
            {
                return o1.getJarPath().compareTo(o2.getJarPath());
            }
        });

        // Create XML document
        Element root = new Element("jqm");
        Document document = new Document(root);

        Element jobDefinitions = null;
        String currentJarPath = null;
        Set<Cl> cls = new HashSet<Cl>();

        for (JobDef j : jobDefList)
        {
            if (currentJarPath == null || !j.getJarPath().equals(currentJarPath))
            {
                currentJarPath = j.getJarPath();
                Element jar = new Element("jar");
                addTextElementToParentElement(jar, "path", currentJarPath);
                addTextElementToParentElement(jar, "pathType", j.getPathType().toString());
                jobDefinitions = new Element("jobdefinitions");
                jar.addContent(jobDefinitions);
                root.addContent(jar);
            }
            Element jobDefinition = getJobDefinitionElement(j, cnx);
            jobDefinitions.addContent(jobDefinition);

            if (j.getClassLoader(cnx) != null)
            {
                cls.add(j.getClassLoader());
            }
        }

        for (Cl cl : cls)
        {
            root.addContent(getClElement(cl));
        }

        // Done: save the file
        try
        {
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            out.output(document, new FileOutputStream(xmlPath));
        }
        catch (java.io.IOException e)
        {
            throw new JqmXmlException("Coul not save the XML file", e);
        }
    }

    private static Element getJobDefinitionElement(JobDef j, DbConn cnx)
    {
        Element jobDefinition = new Element("jobDefinition");

        addTextElementToParentElement(jobDefinition, "name", j.getApplicationName());
        addTextElementToParentElement(jobDefinition, "queue", j.getQueue(cnx).getName());
        addTextElementToParentElement(jobDefinition, "description", j.getDescription() == null ? "" : j.getDescription());
        addTextElementToParentElement(jobDefinition, "canBeRestarted", j.isCanBeRestarted() ? "true" : "false");
        addTextElementToParentElement(jobDefinition, "javaClassName", j.getJavaClassName());
        addTextElementToParentElement(jobDefinition, "application", j.getApplication());
        addTextElementToParentElement(jobDefinition, "module", j.getModule());
        addTextElementToParentElement(jobDefinition, "keyword1", j.getKeyword1());
        addTextElementToParentElement(jobDefinition, "keyword2", j.getKeyword2());
        addTextElementToParentElement(jobDefinition, "keyword3", j.getKeyword3());

        if (j.getMaxTimeRunning() != null)
            addTextElementToParentElement(jobDefinition, "reasonableRuntimeLimitMinute", j.getMaxTimeRunning() + "");
        addTextElementToParentElement(jobDefinition, "highlander", j.isHighlander() ? "true" : "false");

        if (j.getClassLoader(cnx) != null)
        {
            addTextElementToParentElement(jobDefinition, "executionContext", j.getClassLoader().getName());
        }

        List<JobDefParameter> jobDefParameters = j.getParameters(cnx);
        if (jobDefParameters != null && !jobDefParameters.isEmpty())
        {
            Element parameters = new Element("parameters");
            for (JobDefParameter p : jobDefParameters)
            {
                Element parameter = new Element("parameter");
                parameters.addContent(parameter);
                addTextElementToParentElement(parameter, "key", p.getKey());
                addTextElementToParentElement(parameter, "value", p.getValue());
            }
            jobDefinition.addContent(parameters);
        }

        return jobDefinition;
    }

    private static Element getClElement(Cl cl)
    {
        Element res = new Element("context");
        addTextElementToParentElement(res, "name", cl.getName());
        addTextElementToParentElement(res, "childFirst", cl.isChildFirst());
        addTextElementToParentElement(res, "hiddenJavaClasses", cl.getHiddenClasses());
        addTextElementToParentElement(res, "tracingEnabled", cl.isTracingEnabled());
        addTextElementToParentElement(res, "persistent", cl.isPersistent());
        addTextElementToParentElement(res, "runners", cl.getAllowedRunners());

        Element handlers = new Element("eventHandlers");
        res.addContent(handlers);
        for (ClHandler h : cl.getHandlers())
        {
            Element handler = new Element("handler");
            handlers.addContent(handler);
            addTextElementToParentElement(handler, "className", h.getClassName());
            addTextElementToParentElement(handler, "event", h.getEventType().toString());

            Element parameters = new Element("parameters");
            handler.addContent(parameters);
            for (Map.Entry<String, String> prm : h.getParameters().entrySet())
            {
                Element p = new Element("parameter");
                parameters.addContent(p);
                addTextElementToParentElement(p, "key", prm.getKey());
                addTextElementToParentElement(p, "value", prm.getValue());
            }
        }

        return res;
    }

    private static void addTextElementToParentElement(Element parent, String elementName, boolean content)
    {
        addTextElementToParentElement(parent, elementName, content ? "true" : "false");
    }

    private static void addTextElementToParentElement(Element parent, String elementName, String content)
    {
        if (content != null)
        {
            Element e = new Element(elementName);
            e.setText(content);
            parent.addContent(e);
        }
    }

}
