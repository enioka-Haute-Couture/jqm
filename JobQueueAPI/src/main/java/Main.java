

public class Main
{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
            
//		System.out.println("WRITING IN THE DATABASE");
//		System.out.println("ClassLoader: " + ClassLoader.getSystemClassLoader().getResource("META-INF/persistence.xml"));
//	
////                EntityManagerFactory emf = Persistence.createEntityManagerFactory("jobqueue-api-pu");
//		EntityManager em = emf.createEntityManager();
//
//		EntityTransaction transac = em.getTransaction();
//		transac.begin();
//		JobInstance job = new JobInstance();
//		job.setFilePath("palombie/nid/marsu");
//		job.setSessionID(42);
//		job.setUser("MAG");
//
//		em.persist(job);
//		transac.commit();
//		em.close();
//		emf.close();
            
            Launch l = new Launch();
            l.Launch();

	}

}
