Release process
#############################

.. highlight:: bash

This is the procedure that should be followed for making an official JQM release.

Update release notes
+++++++++++++++++++++++++

Add a chapter to the release notes & commit the file.

Checkout
+++++++++++++

Check out the branch master with git.

Full build & tests
++++++++++++++++++++++++++++

There is no distinction between tests & integration tests in JQM so this will run all tests. ::

	mvn clean install

Sonar snapshot
++++++++++++++++++

This will run all tests once again.

::

	mvn sonar:sonar

Once done, take a snaphot in Sonar.

Release test
+++++++++++++

The release plugin is (inside the pom.xml) parametered to use a local git repository, so as to allow mistakes. 
During that step, all packages are bumped in version number, even if they were not modified. ::

	mvn release:prepare -Darguments='-DskipTests'
	mvn package
	
Then the test package must be test-deployed in a two-node configuration.

Release
+++++++++++++

This will upload the packages to the Nexus defined in the "internal" profile. Note that "soon" this should be replaced by a Central-synced repository. ::

	mvn release:perform -Pinternal -Darguments='-DskipTests'

GitHub upload
++++++++++++++++

Create a release inside GitHub and upload the following artifacts:

* jqm-engine
* jqm-webui
* jqm-ws

And, as long as JMQ is not on Central:

* jqm-api-client-core
* jqm-api-client-hibernate
* jqm-api-client-jersey

Git push
+++++++++++++

At this step, the release is done and the local git modifications can be pushed to the central git repository on GitHub.

.. warning:: when using GitHub for Windows, tags are not pushed during sync. Using the command line is compulsory.

::

	git push origin --tags

