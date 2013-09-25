
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import mcd.JobDefinition;
import mcd.JobInstance;
import mcd.JobParameter;

import org.eclipse.persistence.config.PersistenceUnitProperties;
/**
 *
 * @author Pierre COPPEE <pierre.coppee@enioka.com>
 */
public class Launch {

	public Launch() {


//		ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
//		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, properties);
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu", properties);
			EntityManager em = emf.createEntityManager();

			//Thread.currentThread().setContextClassLoader(oldCL);

			EntityTransaction transac = em.getTransaction();
			transac.begin();
			JobInstance job = new JobInstance();
			job.setSessionID(42);
			job.setUser("MAG");

			JobParameter jp = new JobParameter();
			jp.setKey("Marc-Antoine");
			jp.setValue("isMarsu");

			JobDefinition j = new JobDefinition();
			j.setJavaClassName("test");
			j.setMaxTimeRunning(42);

			em.persist(job);
			transac.commit();
			transac = em.getTransaction();
			transac.begin();
			em.persist(j);
			transac.commit();
			transac = em.getTransaction();
			transac.begin();
			em.persist(jp);
			transac.commit();
			em.close();
			emf.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to init entityManager because emf is null");
		}
	}
}
