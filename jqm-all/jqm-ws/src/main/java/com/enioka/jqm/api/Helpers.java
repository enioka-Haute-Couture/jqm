package com.enioka.jqm.api;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.enioka.jqm.jpamodel.GlobalParameter;

public final class Helpers
{
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
