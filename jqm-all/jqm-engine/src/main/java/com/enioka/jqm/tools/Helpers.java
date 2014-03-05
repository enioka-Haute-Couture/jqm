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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.enioka.jqm.jpamodel.Deliverable;
import com.enioka.jqm.jpamodel.GlobalParameter;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.jpamodel.MessageJi;

/**
 * This is a helper class for internal use only.
 * 
 */
public final class Helpers
{
    private static final String PERSISTENCE_UNIT = "jobqueue-api-pu";
    private static Logger jqmlogger = Logger.getLogger(Helpers.class);

    // The one and only EMF in the engine.
    private static Properties props = new Properties();
    private static EntityManagerFactory emf;

    Helpers()
    {

    }

    /**
     * Get a fresh EM on the jobqueue-api-pu persistence Unit
     * 
     * @return an EntityManager
     */
    public static EntityManager getNewEm()
    {
        if (emf == null)
        {
            emf = createFactory();
        }
        return emf.createEntityManager();
    }

    private static EntityManagerFactory createFactory()
    {
        FileInputStream fis = null;
        try
        {
            Properties p = new Properties();
            fis = new FileInputStream("conf/db.properties");
            p.load(fis);
            IOUtils.closeQuietly(fis);
            props.putAll(p);
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
        }
        catch (FileNotFoundException e)
        {
            // No properties file means we use the test HSQLDB (file, not in-memory) as specified in the persistence.xml
            IOUtils.closeQuietly(fis);
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
        }
        catch (IOException e)
        {
            jqmlogger.fatal("conf/db.properties file is invalid", e);
            IOUtils.closeQuietly(fis);
            throw new JqmInitError("Invalid database configuration configuration file", e);
        }
        catch (Exception e)
        {
            jqmlogger.fatal("Unable to connect with the database. Maybe your configuration file is wrong. "
                    + "Please check the password or the url in the $JQM_DIR/conf/db.properties", e);
            throw new JqmInitError("Database connection issue", e);
        }
        finally
        {
            IOUtils.closeQuietly(fis);
        }
    }

    static void allowCreateSchema()
    {
        props.put("hibernate.hbm2ddl.auto", "update");
    }

    /**
     * For internal test use only <br/>
     * <bold>WARNING</bold> This will invalidate all open EntityManagers!
     */
    static void resetEmf()
    {
        if (emf != null)
        {
            emf.close();
        }
        emf = createFactory();
    }

    /**
     * Create a text message that will be stored in the database. Must be called inside a JPA transaction.
     * 
     * @param textMessage
     * @param history
     * @param em
     * @return the JPA message created
     */
    static MessageJi createMessage(String textMessage, JobInstance jobInstance, EntityManager em)
    {
        MessageJi m = new MessageJi();
        m.setTextMessage(textMessage);
        m.setJobInstance(jobInstance);
        em.persist(m);
        return m;
    }

    /**
     * Create a Deliverable inside the database that will track a file created by a JobInstance Must be called from inside a JPA transaction
     * 
     * @param fp
     *            FilePath (relative to a root directory - cf. Node)
     * @param fn
     *            FileName
     * @param hp
     *            HashPath
     * @param ff
     *            File family (may be null). E.g.: "daily report"
     * @param jobId
     *            Job Instance ID
     * @param em
     *            the EM to use.
     * @return
     */
    static Deliverable createDeliverable(String path, String originalFileName, String fileFamily, Integer jobId, EntityManager em)
    {
        Deliverable j = new Deliverable();

        j.setFilePath(path);
        j.setRandomId(UUID.randomUUID().toString());
        j.setFileFamily(fileFamily);
        j.setJobId(jobId);
        j.setOriginalFileName(originalFileName);

        em.persist(j);
        return j;
    }

    /**
     * Retrieve the value of a single-valued parameter.
     * 
     * @param key
     * @param defaultValue
     * @param em
     * @return
     */
    static String getParameter(String key, String defaultValue, EntityManager em)
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
