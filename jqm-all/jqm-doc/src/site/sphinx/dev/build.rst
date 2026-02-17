Release process
#############################

.. highlight:: bash

This is the procedure that should be followed for making an official JQM release.

Prerequisites
++++++++++++++++

The following GitHub Secrets must be configured in the repository:

* ``MAVEN_CENTRAL_USERNAME`` - Maven Central login
* ``MAVEN_CENTRAL_PASSWORD`` - Maven Central password
* ``GPG_PRIVATE_KEY`` - GPG private key in ASCII armor format
* ``GPG_PASSPHRASE`` - GPG key passphrase
* ``GPG_KEYNAME`` - GPG key id
* ``DOCKER_USERNAME`` - Docker Hub username
* ``DOCKER_PASSWORD`` - Docker Hub password

Update release notes
+++++++++++++++++++++++++

Add a chapter to the release notes & commit the file to master branch.

Release test
++++++++++++++++++++++++++

Before running the automated release, test the release artifacts locally.

Check out the branch master with git, then build the release version for testing::

	mvn package -Prelease -Pbuild-frontend -DskipTests

The test package should be test-deployed in a two-node configuration to validate functionality.

Automated release
++++++++++++++++++++++++++++

The release process is automated via GitHub Actions using the ``JQM Release`` workflow.
You will be prompted to enter the release version (e.g., ``3.2.1``) and the next development version (e.g., ``3.2.2-SNAPSHOT``).

The workflow will automatically:

* Run the full build and tests
* Execute ``mvn release:prepare`` to create the release tag and bump versions
* Execute ``mvn release:perform`` to build with ``-Prelease`` profile and deploy to Maven Central
* Push the Git tags and commits to GitHub
* Build and push Docker images to Docker Hub (with version, major version, and latest tags)
* Create a draft GitHub release with the zip and tar.gz artifacts

Maven Central Repository validation
************************************

After the automated workflow completes, go to https://central.sonatype.com/ and publish the release.

Documentation
+++++++++++++++

Go to jqm.rtfd.org and change the default branch to the newly created tag.

GitHub Release
++++++++++++++++

The automated workflow creates a **draft** release on GitHub with the artifacts already uploaded.

1. Go to the repository **Releases** page
2. Edit the draft release for the new version
3. Update the release notes body with a link to the documentation
4. Once the documentation is live on ReadTheDocs, publish the release

.. note:: only publish **after** the documentation is up on ReadTheDocs. Creating a release sends a mail to followers, so any link to the doc would be dead otherwise.

Manual release process (deprecated)
++++++++++++++++++++++++++++++++++++

.. warning:: The manual process is deprecated. Use the GitHub Actions workflow described above.

For reference, the manual release process now replaced by the automated release step was:

Environment
***********

The release environment must have:

* PGP & the release private key
* Internet access
* Login & password to Maven Central Repository
* Docker client 18.06+

Full build & tests
******************

There is no distinction between tests & integration tests in JQM so this will run all tests. ::

	mvn clean install -Pbuild-frontend

Release
*******

::

	mvn release:prepare -Darguments='-DskipTests'

This creates a Git tag with the release version and updates the POMs to the next development version.

Then the test package must be test-deployed in a two-node configuration. To build the release version for testing::

	mvn package -Prelease -Pbuild-frontend -DskipTests scm:checkout -Drevision=<tag-name>

This will checkout the release tag, build it, and upload to Maven Central Repository. ::

	mvn release:perform -Darguments='-Dgpg.keyname=<keyname> -Prelease -Pbuild-frontend -DskipTests'

Git push
********

::

    git push origin --tags
    git push origin

Docker Hub upload
*****************

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

GitHub upload
*************

Create a release inside GitHub and upload the zip and tar.gz produced by the jqm-service project.
