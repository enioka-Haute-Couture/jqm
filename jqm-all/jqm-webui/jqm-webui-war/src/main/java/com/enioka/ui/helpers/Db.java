package com.enioka.ui.helpers;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.enioka.jqm.api.JqmClientFactory;

public final class Db
{
    private static EntityManagerFactory emf = null;

    public static EntityManager getEm()
    {
        if (emf == null)
        {
            Properties p = new Properties();
            p.put("hibernate.hbm2ddl.auto", "update");
            emf = Persistence.createEntityManagerFactory("jobqueue-api-pu", p);

            Properties p2 = new Properties();
            p2.put("emf", emf);
            JqmClientFactory.setProperties(p2);
        }
        return emf.createEntityManager();
    }
}
