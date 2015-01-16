Simple web API
###################

.. highlight:: bash

This is the strict minimum to allow easy wget/curl integration. Most notably, this is what will be usually 
used for integration with a job scheduler like Orsyp $Universe or BMC ControlM.

It allows:

* Submitting a job execution request (returns the query ID)
* Checking the status of a request (identified by its ID)
* Retrieving the logs created by an ended request
* Retrieving the files (PDF reports, etc) created by an ended request

.. note:: this API should never be used directly from a Java program. The more complete client APIs actually encapsulate the simple API in a Java-friendly manner.

Please refer to the following examples in PowerShell for the URLs to use (adapt DNS and port according to your environment. If you don't know these parameters, they are inside the NODE definitions and written at startup inside the server log). Also, these examples assume authentication is disabled (option -Credential should be used otherwise). ::

    # Get the status of job instance 1035
    PS> Invoke-RestMethod http://localhost:61260/ws/simple/status?id=1035
    ENDED
    
    # Get stdout of jobinstance 1035
    PS> Invoke-RestMethod http://localhost:61260/ws/simple/stdout?id=1035
    LOG LINE
    
    # Get stderr of job instance 1035 (void log as a result in this example)
    PS> Invoke-RestMethod http://localhost:61260/ws/simple/stderr?id=1035
    
    # Get file which ID is 77a73e85-e2b6-4e89-bb07-f7097b17e532
    # This ID cannot be guessed or retrieved through the simple API - this method mostly exist for the full API to call.
    # It is documented here for completion sake only.
    PS> Invoke-RestMethod http://localhost:61260/ws/simple/file?id=77a73e85-e2b6-4e89-bb07-f7097b17e532
    The first line
    The second line
    
    # Enqueue a new execution request. Returned number is the ID of the request (the ID to use with the other methods)
    PS> Invoke-RestMethod http://localhost:61260/ws/simple/ji -Method Post -Body @{applicationname="DemoApi"}
    1039
    
    # Same, but with parameters
    PS> Invoke-RestMethod http://localhost:61260/ws/simple/ji -Method Post -Body @{applicationname="DemoApi";parameterNames="p1";parameterValues="eee"}
    1047
    