Release process
#############################

.. highlight:: bash

This is the procedure that should be followed for making an official JQM release.

Environment
++++++++++++++++

The release environment must have:

* PGP & the release private key
* Access to a Sonar server with a correctly configured Maven settings.xml
* The Selenium setup (see :doc:`tests`) - this has been deprecated. It may come back later.
* Internet access
* Login & password to Sonatype OSSRH with permissions on com.enioka.jqm.
* Login & password to Read the Docs with permissions on com.enioka.jqm.

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

This will create a new Sonar analysis.

::

	mvn sonar:sonar

Once done, take a snaphot in Sonar.

Release test
+++++++++++++

The release plug-in is (inside the pom.xml) parametrized to use a local git repository, so as to allow mistakes. 
During that step, all packages are bumped in version number, even if they were not modified. ::

	mvn release:prepare -Darguments='-DskipTests'
	mvn package
	
Then the test package must be test-deployed in a two-node configuration.

Release
+++++++++++++

This will upload the packages to the OSSRH staging repository.::

	mvn release:perform -Darguments='-DskipTests'

OSSRH validation
********************

Go to https://oss.sonatype.org/ and unstage the release. This will in time allow synchronization with Maven Central.

Git push
+++++++++++++

At this step, the release is done and the local git modifications can be pushed to the central git repository on GitHub.

.. warning:: when using GitHub for Windows, tags are not pushed during sync. Using the command line is compulsory.

::

    git push origin
    git push origin --tags

GitHub upload
++++++++++++++++

Create a release inside GitHub and upload the zip and tar.gz produced by the jqm-engine project. Add a link to the release notes inside.

Documentation
+++++++++++++++

Go to jqm.rtfd.org and change the default branch to the newly created tag.
