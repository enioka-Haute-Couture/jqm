Testing
###############

JQM is tested through an series of automated JUnit tests. These tests are usage-oriented (*integration tests*)
rather than unit oriented. This means: every single functionality of JQM must have (at least) one automated test
that involves running a job inside a full engine.

Automated builds
***********************

Travis
++++++++

The project has a public CI server on http://travis-ci.org/enioka/jqm.

Selenium
++++++++++++++

The project has a public Selenium server at https://saucelabs.com/u/marcanpilami

Tests
************

Standard tests
+++++++++++++++++++++

These are the tests that **should always be run before any commit**. Any failure fails the build.

They are run though Maven (mvn clean install test) and should be able to run without any specific configuration.
They are always run by Travis.

Selenium tests
++++++++++++++++++++

The UI also has a few dedicated tests that run inside Selenium. To avoid configuration and ease test reproducibility, 
we use Sauce Labs' cloud Selenium. The Travis build uses the maintainer's account. 

As this account is personal, its credentials are not included inside the build descriptor and these tests are disabled by default
(they are inside a specific Maven profile). In order to use them, a free account on Sauce Labs is required, as well as 
putting this inside Maven's settings.xml::

    <profile>
        <id>selenium</id>
        <activation>
            <activeByDefault>false</activeByDefault>
        </activation>
        <properties>
            <SAUCE_USERNAME>YOUR_USER_NAME</SAUCE_USERNAME>
            <SAUCE_ACCESS_KEY>YOUR_ACCESS_KEY</SAUCE_ACCESS_KEY>
            <SAUCE_URL>localhost:4445/wd/hub</SAUCE_URL>
        </properties>
    </profile>
    
Moreover, as the web application actually runs on the developer's computer and not on the Selenium server,
a tunnel must be activated, using `Sauce Connect <https://docs.saucelabs.com/reference/sauce-connect/>`_. The URL above reflects this.

.. note:: the Sauce Connect Maven plugin was not included in the pom, because it implies starting
    and stopping the tunnel on each test run - and this is a very long process. It's easier on the nerves to simply start the
    tunnel and forget it.
    
Finally, running the tests is simply done by going inside the jqm-wstst project and running the classic "mvn test -Pselenium" command.
Obviously, if in the settings.xml file the profile was marked as active by default, the -P option can be omitted.

Web-services dev and tests
++++++++++++++++++++++++++++++++

The admin GUI as well as all the web services are inside the jqm-ws project.

To develop and test this project in Eclipse, one needs a fully working JQM database. The easiest way to get it is to 
install a local node following the documentation. Then enable the admin GUI & create the root account with the command line. Do not enable SSL.

The node can be stopped - it won't be needed anymore.

Then, inside Eclipse, install a Tomcat 7. (not 8 - this would require Java 7).

The project contains a context.xml file inside src/test/webapp/META-INF that must be updated with the connection string to your database.
Please do not commit these modifications.

.. warning:: you must ensure the src/test/webapp/META-INF directory is inside the "deployment assembly" inside Eclipse's project properties.

Then the database driver to the the lib directory of Tomcat

Everything is ready - the project can now be "run on server". The URL will be http://localhost:8080/jqm-ws
