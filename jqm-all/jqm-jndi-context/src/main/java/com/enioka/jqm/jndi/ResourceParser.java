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

import javax.naming.NamingException;
import javax.naming.StringRefAddr;

import com.enioka.jqm.jdbc.DbConn;
import com.enioka.jqm.jdbc.DbManager;
import com.enioka.jqm.model.JndiObjectResource;
import com.enioka.jqm.model.JndiObjectResourceParameter;

/**
 * Helper class to retrieve a {@link JndiResourceDescriptor} either from the XML resource file or from the database. <br>
 * XML file has priority over database.
 */
public final class ResourceParser
{

    private ResourceParser()
    {
        // Utility class
    }

    static JndiResourceDescriptor getDescriptor(String alias) throws NamingException
    {
        return fromDatabase(alias);
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

}
