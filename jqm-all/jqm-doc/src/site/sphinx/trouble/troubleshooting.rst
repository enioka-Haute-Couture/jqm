Troubleshooting
#######################################

* When starting JQM, “address already in use” error appears. Change the ports of your nodes (by default a node is on a random free port, but you may have started multiple nodes on the same port)
* When starting JQM, “Unable to build EntityManager factory” error appears. It means JQM cannot connect o its database. Check the information in the conf/resource.xml file.
* Problem with the download of the dependencies during the execution: Your nexus or repositories configuration must be wrong. Check your pom.xml or the JQM global parameters.
* "NoSuchMethodException": check that the versions of the APIs and engine is the same.

If your problem does not appear above and the rest of the documentation has no answer for your issue, please :doc:`open a ticket<bugs>`.
