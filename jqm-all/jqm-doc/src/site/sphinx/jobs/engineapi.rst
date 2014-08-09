Engine API
#########################

The engine API is an interface offered optionally to running job instances allowing them to interact with JQM.

It allows them to do some operations only available to running jobs (such as specifying that a file they have just
created should be made available to end users) as well as a subset of operations coming directly from the :doc:`/client/client`.
The latter is mostly for convenience - that way, clients do not have to import, set parameters and initialize the full API - everything
is readied  byt eh engine (and very quickly because the engine reuses some of its own objects).
 
Using the API is easy: one just has to declare, inside the job main class, a Field of :class:`JobManager` type. It can be static.
Then, the JQM engine will inject an instance inside that field at runtime and it can be used without further ado. 
    
.. class:: JobManager

    This interface gives access to JQM engine variables and methods. It allows to retrieve the characteristics of the currently running 
    job instances, as well as creating new instances and other useful methods.
    It should never be instantiated but injected by the JQM engine. For the injection to take place, 
    the payload main class should have a field of type JobManager (directly or through inheritance, as well as public or private).

Current job metadata
**********************

For the description of these items, please see the job instance description.

    .. method:: JobManager.parentId() -> int
    
    .. method:: JobManager.jobApplicationId() -> int
    
    .. method:: JobManager.jobInstanceID() -> int
    
    .. method:: JobManager.canBeRestarted() -> boolean
    
    .. method:: JobManager.applicationName() -> String
    
    .. method:: JobManager.sessionID() -> String
    
    .. method:: JobManager.application() -> String
    
    .. method:: JobManager.module() -> String
    
    .. method:: JobManager.keyword1() -> String
    
    .. method:: JobManager.keyword2() -> String
    
    .. method:: JobManager.keyword3() -> String
    
    .. method:: JobManager.userName() -> String
    
    .. method:: JobManager.parameters() -> Map<String, String>
    
Enqueue & retrieve jobs
************************



Communications
********************


Misc.
************

