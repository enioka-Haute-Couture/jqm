/**
 *
 */
package dependencies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.wagon.WagonProvider;
import org.eclipse.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;


/**
 * @author Pierre COPPEE <pierre.coppee@enioka.com>
 *
 */
public class DependencyResolver {

	//public static final String MAVEN_CENTRAL_URL = "http://repo1.maven.org/maven2/";

	public static class ResolveResult {
		public String classPath;
		public List<ArtifactResult> artifactResults;

		public ResolveResult(String classPath, List<ArtifactResult> artifactResults) {
			this.classPath = classPath;
			this.artifactResults = artifactResults;
		}
	}

	final RepositorySystemSession session;
	final RepositorySystem repositorySystem;
	final List<String> repositories = new ArrayList<String>();

	public DependencyResolver(File localRepoDir, String... repos) throws IOException {
		repositorySystem = newRepositorySystem();
		//System.out.println("localRepoDir : " + localRepoDir);
		//System.out.println("repositorySystem : " + repositorySystem);
		session = newSession(repositorySystem, localRepoDir);
		repositories.addAll(Arrays.asList(repos));
	}

	public synchronized ArrayList<ResolveResult> resolve(ArrayList<String> artifactCoords) throws Exception {

		ArrayList<ResolveResult> rr = new ArrayList<DependencyResolver.ResolveResult>();

		for (int i = 0; i < artifactCoords.size(); i++)
		{
			Dependency dependency = new Dependency(new DefaultArtifact(artifactCoords.get(i)), "compile");
			System.out.println("artifactCoords: " + i);
			CollectRequest collectRequest = new CollectRequest();
			collectRequest.setRoot(dependency);

			for (int j = 0; j < repositories.size(); ++j) {
				final String repoUrl = repositories.get(j);
				//System.out.println("repo: " + repositories.get(j));
				//System.out.println("RepoURL: " + repoUrl.toString());
				collectRequest.addRepository(j > 0 ? repo(repoUrl, null, "default") : repo(repoUrl, "central", "default"));
			}

//			System.out.println("SESSION: " + session);
//			System.out.println("COLLECTREQUEST: " + collectRequest.toString());
			DependencyNode node = repositorySystem.collectDependencies(session, collectRequest).getRoot();
//			System.out.println("TESTED");

			DependencyRequest dependencyRequest = new DependencyRequest();

			dependencyRequest.setRoot(node);


			//		Collects and resolves the transitive dependencies of an artifact.
			//		This operation is essentially a combination of collectDependencies(RepositorySystemSession, CollectRequest)
			//		and resolveArtifacts(RepositorySystemSession, Collection).
			DependencyResult res = repositorySystem.resolveDependencies(session, dependencyRequest);

			PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
			node.accept(nlg);
			rr.add(new ResolveResult(nlg.getClassPath(), res.getArtifactResults()));
		}
		return rr;
	}

	private RepositorySystemSession newSession(RepositorySystem system, File localRepoDir) throws IOException {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository(localRepoDir.getAbsolutePath());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

		return session;
	}

	private RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.setServices(WagonProvider.class, new ManualWagonProvider());
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        return locator.getService(RepositorySystem.class);
	}

	@SuppressWarnings("unused")
	private RemoteRepository repo(String repoUrl) {
//		System.out.println("DebugRepo: " + repoUrl);
		return new RemoteRepository.Builder(null, null, repoUrl).build();
	}

	private RemoteRepository repo(String repoUrl, String repoName, String repoType) {
//		System.out.println("repoName: " + repoName + " " + "repoType: " + repoType + " " + "repoUrl: " + repoUrl);
		return new RemoteRepository.Builder(repoName, repoType, repoUrl).build();
	}
}
