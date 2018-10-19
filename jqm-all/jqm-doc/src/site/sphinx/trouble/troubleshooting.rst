﻿Troubleshooting
#######################################

* When starting JQM, “address already in use” error appears. Change the ports of your nodes (by default a node is on a random free port, but you may have started multiple nodes on the same port)
* Problem with the download of the dependencies during the execution: Your nexus or repositories configuration must be wrong. Check your pom.xml or the JQM global parameters.

If your problem does not appear above and the rest of the documentation has no answer for your issue, please :doc:`open a ticket<bugs>`.
