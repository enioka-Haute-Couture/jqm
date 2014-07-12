Simple web API
###################

This is the strict minimum to allow easy wget/curl integration. Most notably, this is what will be usually 
used for integration with a job scheduler like Orsyp $Universe or BMC ControlM.

It allows:

* Submitting a job execution request (returns the query ID)
* Checking the status of a request (identified by its ID)
* Retrieving the logs created by an ended request
* Retrieving the files (PDF reports, etc) created by an ended request

.. note:: this API should never be used directly from a Java program. The more complete client APIs actually encapsulate the simple API in a Java-friendly manner.



