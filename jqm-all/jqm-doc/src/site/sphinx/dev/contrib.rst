Contributing to JQM
#############################

JQM is an Open Source project under the Apache v2 license. We welcome every contribution through GitHub pull requests.

If you wonder if you should modify something, do not hesitate to mail the maintainer at mag@enioka.com with [JQM] inside the subject. 
It also works before opening feature requests.


JQM dev environment:

* Eclipse, latest version (standard for Enioka developers, it is of course possible to successfully use other IDEs or editors - use provided style xml)
  * Visual Studio Code configuration is also provided with the code, as it directly re-uses Eclipse formatting (and language server!).
* Maven 3.x (CLI, no Eclipse plugin) for dependencies, build, tests, packaging
* Sonar (through Maven. No public server provided, rules are :download:`here </files/rules_sonar.csv>`)
* Git

For the web application, a WebPack build is triggered by Maven. Developpers would simply use the "exec:exec" goal in the WS project to start a dev server with both Java and JS on port 8080.

Finally, please respect our coding style and conventions: they are described in the :doc:`style` page.
