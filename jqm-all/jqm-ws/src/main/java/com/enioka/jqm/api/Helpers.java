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
package com.enioka.jqm.api;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.enioka.jqm.jpamodel.GlobalParameter;

public final class Helpers
{
    static
    {
        Properties p = new Properties();
        p.put("javax.persistence.nonJtaDataSource", "jdbc/jqm");
        // p.put("hibernate.show_sql", "true");
        JqmClientFactory.setProperties(p);
    }

    private Helpers()
    {
        // helper class
    }

    public static EntityManager getEm()
    {
        return ((HibernateClient) JqmClientFactory.getClient()).getEm();
    }

    public static void closeQuietly(EntityManager em)
    {
        try
        {
            if (em != null)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        catch (Exception e)
        {
            // fail silently
        }
    }

    public static String getParameter(String key, String defaultValue, EntityManager em)
    {
        try
        {
            GlobalParameter gp = em.createQuery("SELECT n from GlobalParameter n WHERE n.key = :key", GlobalParameter.class)
                    .setParameter("key", key).getSingleResult();
            return gp.getValue();
        }
        catch (NoResultException e)
        {
            return defaultValue;
        }
    }

}
