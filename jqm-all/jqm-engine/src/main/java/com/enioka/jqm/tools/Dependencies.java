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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class Dependencies
{
    private List<String> list = new ArrayList<String>();
    private static Logger jqmlogger = Logger.getLogger(Dependencies.class);

    Dependencies(String path)
    {
        File fXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        String dep = "";

        try
        {
            jqmlogger.debug(fXmlFile.getPath());
            jqmlogger.debug("Working Directory = " + System.getProperty("user.dir"));
            if (fXmlFile == null || !fXmlFile.isFile())
            {
                throw new FileNotFoundException("The XML file " + fXmlFile + " was not found");
            }

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("parent");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;

                    dep += eElement.getElementsByTagName("groupId").item(0).getTextContent().toString() + ":";
                    dep += eElement.getElementsByTagName("artifactId").item(0).getTextContent().toString() + ":pom:";
                    dep += eElement.getElementsByTagName("version").item(0).getTextContent().toString();

                    list.add(dep);
                    dep = "";
                }
            }

            nList = doc.getElementsByTagName("dependency");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;

                    dep += eElement.getElementsByTagName("groupId").item(0).getTextContent().toString() + ":";
                    dep += eElement.getElementsByTagName("artifactId").item(0).getTextContent().toString() + ":";
                    dep += eElement.getElementsByTagName("version").item(0).getTextContent().toString();

                    list.add(dep);
                    dep = "";
                }
            }

        }
        catch (ParserConfigurationException e)
        {
            jqmlogger.error(e);
        }
        catch (SAXException e)
        {
            jqmlogger.error("Invalid POM XML file content. Please fix the Maven file.", e);
        }
        catch (IOException e)
        {
            jqmlogger.error("Invalid pom.xml definition - could not read the file. Please check the pom.xml & its filepath " + path, e);
        }
        catch (Exception e)
        {
            jqmlogger.error("Invalid pom.xml. Please check the pom.xml & its filepath " + path, e);
        }
    }

    /**
     * @return the list
     */
    List<String> getList()
    {
        return list;
    }
}
