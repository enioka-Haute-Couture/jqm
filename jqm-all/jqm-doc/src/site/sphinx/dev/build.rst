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
* Login & password to Maven Central Repository (https://central.sonatype.com/) with permissions on com.enioka.jqm.
* Login & password to Read the Docs with permissions on com.enioka.jqm.
* Docker client 18.06+ and access to the multi-arch build environment. -- deprecated,  build is migrating to Github action

Update release notes
+++++++++++++++++++++++++

Add a chapter to the release notes & commit the file.

Checkout
+++++++++++++

Check out the branch master with git.

Full build & tests
++++++++++++++++++++++++++++

There is no distinction between tests & integration tests in JQM so this will run all tests. ::

	mvn clean install -Pbuild-frontend

Sonar snapshot (deprecated)
++++++++++++++++++

This will create a new Sonar analysis.

::

    mvn clean install -DskipTests -Psonar
	mvn test sonar:sonar -Psonar
    mvn clean

Release test
+++++++++++++

The release plug-in is (inside the pom.xml) parametrized to use a local git repository, so as to allow mistakes.
During that step, all packages are bumped in version number, even if they were not modified. ::

	mvn release:prepare -Darguments='-DskipTests'

This creates a Git tag with the release version and updates the POMs to the next development version.

Then the test package must be test-deployed in a two-node configuration. To build the release version for testing::

	mvn package -Prelease -Pbuild-frontend -DskipTests scm:checkout -Drevision=<tag-name>

Release
+++++++++++++

This will checkout the release tag, build it, and upload to Maven Central Repository. ::

     mvn release:perform -Darguments='-Dgpg.keyname=<keyname> -Prelease -Pbuild-frontend -DskipTests'

Maven Central Repository validation
************************************

Go to https://central.sonatype.com/ and publish the release.

Git push
+++++++++++++

At this step, the release is done and the local git modifications can be pushed to the central git repository on GitHub.

.. warning:: when using GitHub for Windows, tags are not pushed during sync. Using the command line is compulsory.

::

    git push origin --tags
    git push origin

(push tags before code to help RTD synchronization)

Documentation
+++++++++++++++

Go to jqm.rtfd.org and change the default branch to the newly created tag.

GitHub upload
++++++++++++++++

Create a release inside GitHub and upload the zip and tar.gz produced by the jqm-service project. Add a link to the release notes inside.

.. note:: only do this **after** the documentation is up on ReadTheDocs. Creating a release sends a mail to followers, so any link to the doc would be dead otherwise.

Docker Hub upload
++++++++++++++++++++

Checkout the version tag ::

    git checkout jqm-all-3.2.0

For linux amd64 architecture, build the Docker image for the new release ::

    docker build --platform linux/amd64 --rm --pull --provenance=false --sbom=false -t enioka/jqm:3.2.0 -f ./linux/Dockerfile ../

Push the image to Docker Hub ::

    docker push enioka/jqm:3.2.0

Then tag the image as latest and 3 and push again ::

    docker tag enioka/jqm:3.2.0 enioka/jqm:latest
    docker tag enioka/jqm:3.2.0 enioka/jqm:3
    docker push enioka/jqm:latest
    docker push enioka/jqm:3
