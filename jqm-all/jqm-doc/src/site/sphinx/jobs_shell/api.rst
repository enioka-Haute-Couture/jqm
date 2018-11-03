Calling JQM APIs
########################

Environment variables
**************************

Rather than having to call an API to obtain infomration as would be done in a programming language, 
JQM readily puts at the process disposal quite a few information on itself. See :ref:`shell-envvars` for a list.

WS APIs
*****************

An account is provided to call the JQM client APIs. Please see :doc:`../client/index` for details on how to use
this API.

The account is garanteed to stay valid at least 24 hours and should not be used for any longer than 24H. 
(It is destroyed randomly by JQM between 24 and 48 hours).

For example, a bash script may use this command to request the execution of a child job::

  curl --user "${JQM_API_LOGIN}:${JQM_API_PASSWORD}" --url "${JQM_API_LOCAL_URL}/ws/simple/ji" -XPOST -d "applicationname=ChildAppName&parentid=${JQM_JI_ID}" -H "Content-Type: application/x-www-form-urlencoded" 


Creating temp files
***********************

It is recommended to use the directory designated by the environment variable JQM_JI_TEMP_DIR for creating those.
This directory is purged at the end of the execution. It is specific to a single job instance.

Creating result files 
*************************

Those are files that need to be kept after the run and made available to users through the APIs. They usually are
the results of the execution: a report, an accounting book, a graph...

These files simply have to be created inside or moved at the root of the directory JQM_JI_DELIVERY_DIR. They will be referenced by
file name  by JQM.

Note that JQM moves the files from JQM_JI_DELIVERY_DIR to its own internal directory hierarchy after the execution
has ended.

.. warning:: result files are only collected if the job instance succeeds.



Unavailable APIs
*********************

The following APIs are available for Java job instances but not shell ones:

* sending progress information. (mostly useless for executables which only return control at the end)
* waiting for children death.
