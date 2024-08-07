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
package com.enioka.jqm.jndi;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.StringRefAddr;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.JndiObjectResource;
import com.enioka.jqm.model.JndiObjectResourceParameter;
import com.enioka.jqm.shared.exceptions.JqmRuntimeException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class to retrieve a {@link JndiResourceDescriptor} either from the XML resource file or from the database. <br>
 * XML file has priority over database.
 */
final class ResourceParser
{
    private static Map<String, JndiResourceDescriptor> xml = null;

    private ResourceParser()
    {
        // Utility class
    }

    static JndiResourceDescriptor getDescriptor(String alias) throws NamingException
    {
        if (xml == null)
        {
            xml = new HashMap<String, JndiResourceDescriptor>();
            boolean atLeastOneXmlLoaded = false;

            var resourceFileList = System.getProperty("com.enioka.jqm.resourceFiles", "resources.xml,");
            for (String file : resourceFileList.split(","))
            {
                atLeastOneXmlLoaded = importXml(file, true) || atLeastOneXmlLoaded;
            }

            if (!atLeastOneXmlLoaded)
            {
                throw new NamingException("No resource file from list [" + resourceFileList + "] found in classpath");
            }
        }
        if (xml.containsKey(alias))
        {
            return xml.get(alias);
        }
        else
        {
            return fromDatabase(alias);
        }
    }

    private static JndiResourceDescriptor fromDatabase(String alias) throws NamingException
    {
        JndiObjectResource resource = null;

        try
        {
            try (DbConn cnx = DbManager.getDb().getConn())
            {
                resource = JndiObjectResource.select_alias(cnx, alias);

                JndiResourceDescriptor d = new JndiResourceDescriptor(resource.getType(), resource.getDescription(), null,
                        resource.getAuth(), resource.getFactory(), resource.getSingleton());
                for (JndiObjectResourceParameter prm : resource.getParameters(cnx))
                {
                    d.add(new StringRefAddr(prm.getKey(), prm.getValue() != null ? prm.getValue() : ""));
                    // null values forbidden (but equivalent to "" in Oracle!)
                }

                return d;
            }
        }
        catch (Exception e)
        {
            NamingException ex = new NamingException("Could not find a JNDI object resource of name " + alias);
            ex.setRootCause(e);
            throw ex;
        }
    }

    private static boolean importXml(String fileName, boolean optional) throws NamingException
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        // The resource file is either inside /conf (added to classpath by packaging) or inside test classpath (Maven resource path).
        try (InputStream is = ClassLoader.getSystemResourceAsStream(fileName))
        {
            boolean atLeastOneResourceLoaded = false;
            if (is == null && !optional)
            {
                throw new JqmRuntimeException("Cannot find in class path resource file named " + fileName);
            }
            if (is == null && optional)
            {
                return false;
            }

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("resource");

            String jndiAlias = null, resourceClass = null, description = "no description", scope = null, auth = "Container", factory = null;
            boolean singleton = false;

            for (int i = 0; i < nList.getLength(); i++)
            {
                Node n = nList.item(i);
                Map<String, String> otherParams = new HashMap<String, String>();

                NamedNodeMap attrs = n.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++)
                {
                    Node attr = attrs.item(j);
                    String key = attr.getNodeName();
                    String value = attr.getNodeValue();

                    if ("name".equals(key))
                    {
                        jndiAlias = value;
                    }
                    else if ("type".equals(key))
                    {
                        resourceClass = value;
                    }
                    else if ("description".equals(key))
                    {
                        description = value;
                    }
                    else if ("factory".equals(key))
                    {
                        factory = value;
                    }
                    else if ("auth".equals(key))
                    {
                        auth = value;
                    }
                    else if ("singleton".equals(key))
                    {
                        singleton = Boolean.parseBoolean(value);
                    }
                    else
                    {
                        otherParams.put(key, value);
                    }
                }

                if (resourceClass == null || jndiAlias == null || factory == null)
                {
                    throw new NamingException("could not load the resource file " + fileName);
                }

                JndiResourceDescriptor jrd = new JndiResourceDescriptor(resourceClass, description, scope, auth, factory, singleton);
                for (Map.Entry<String, String> prm : otherParams.entrySet())
                {
                    jrd.add(new StringRefAddr(prm.getKey(), prm.getValue()));
                }
                atLeastOneResourceLoaded = true;
                xml.put(jndiAlias, jrd);
            }
            return atLeastOneResourceLoaded;
        }
        catch (Exception e)
        {
            NamingException pp = new NamingException("could not initialize the JNDI local resources");
            pp.setRootCause(e);
            throw pp;
        }
    }
}
