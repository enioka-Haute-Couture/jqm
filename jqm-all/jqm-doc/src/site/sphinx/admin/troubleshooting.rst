Troubleshooting & known bugs
#######################################

* When starting JQM, “address already in use” error appears. Change the ports of your nodes (by default a node is on a random free port, but you may have started multiple nodes on the same port)
* When starting JQM, “Unable to build EntityManager factory” error appears. It means JQM cannot connect o its database. Check the information in the conf/db.properties file.
* Problem with the password during the execution of running jobs using the Hibernate client API (should be a rare configuration). M%eans the jdbc/jqm JNDI alias is incorrectly defined inside the JQM JNDI repository. In the DatabaseProp table, update field 'pwd'.
* Problem with the download of the dependencies during the execution: Your nexus or depositories configuration must be wrong. Check your pom.xml or ths JQM global parameters.
* Jcabi/Aether can’t find the current dependency: Check your repository parameters and if the dependency is on your nexus repository. If you are sure that the dependency is on the nexus, the directories corresponding to the groupId must be deleted on your local repository.
* "NoSuchMethodException": check that the versions of the APIs and engine is the same.
