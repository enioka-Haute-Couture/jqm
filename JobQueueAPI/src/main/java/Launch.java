
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.lang.Thread;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import mcd.JobInstance;
import org.eclipse.persistence.config.PersistenceUnitProperties;
/**
 *
 * @author Pierre COPPEE <pierre.coppee@enioka.com>
 */
public class Launch {

    public void Launch() {
        
        
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML,"persistence.xml");
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu", properties);
            EntityManager em = emf.createEntityManager();
            
            Thread.currentThread().setContextClassLoader(oldCL);
            
            EntityTransaction transac = em.getTransaction();
		transac.begin();
		JobInstance job = new JobInstance();
		job.setFilePath("palombie/nid/marsu");
		job.setSessionID(42);
		job.setUser("MAG");

		em.persist(job);
		transac.commit();
		em.close();
		emf.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to init entityManager because emf is null");
        }
    }
}
