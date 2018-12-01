Command Line Interface (CLI)
#################################

.. highlight:: bash

The CLI has commands to:

* create a new node inside the cluster
* export and import job definitions (deployment descriptors)
* import queues
* import cluster configuration
* create a new execution request
* get the status of an execution request
* create or reset user accounts, including root
* apply a configuration template to a node
* enable or disable web APIs, using TLS or not
* update the database

To get all details, use `java -jar jqm.jar --help`.

The most useful options are exposed in a more user friendly way with the
script jqm.sh (or jqm.ps1 under Windows).

.. warning:: a few options exist which are not listed here. They are internal and are not part the API (may change without notice, etc).
