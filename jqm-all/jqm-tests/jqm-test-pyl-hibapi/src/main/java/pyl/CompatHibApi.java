package pyl;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import jpa.Entity;

import org.apache.log4j.Logger;

import com.enioka.jqm.api.JobManager;

public class CompatHibApi implements Runnable
{
    private static final Logger log = Logger.getLogger(CompatHibApi.class);
    private JobManager jm;

    @Override
    public void run()
    {
        log.info("Starting payload");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("marsu-pu");
        EntityManager em = emf.createEntityManager();

        log.info("Running query");
        List<Entity> res = em.createQuery("SELECT e from Entity e", Entity.class).getResultList();
        log.info(res.size());

        if (jm.parameters().size() == 0)
        {
            log.info("Queuing again - with parameter and through the JM API");
            Map<String, String> prms = new HashMap<String, String>();
            prms.put("stop", "1");
            jm.enqueue(jm.applicationName(), null, null, null, null, null, null, null, null, prms);
        }
        else
        {
            System.out.println(jm.parameters().get("stop"));
        }
        log.info("End of payload");
    }
}