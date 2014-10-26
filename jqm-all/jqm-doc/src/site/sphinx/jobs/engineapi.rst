Engine API
#########################

The engine API is an interface offered optionally to running job instances allowing them to interact with JQM.

It allows them to do some operations only available to running jobs (such as specifying that a file they have just
created should be made available to end users) as well as a subset of operations coming directly from the :doc:`/client/client`.
The latter is mostly for convenience - that way, clients do not have to import, set parameters and initialize the full API - everything
is readied  by the engine (and very quickly because the engine reuses some of its own already-initialized objects).
 
Using the API is easy: one just has to declare, inside the job main class, a Field of :class:`JobManager` type. It can be static.
Then, the JQM engine will inject an instance inside that field at runtime and it can be used without further ado. 
    
.. class:: JobManager

    This interface gives access to JQM engine variables and methods. It allows to retrieve the characteristics of the currently running 
    job instances, as well as creating new instances and other useful methods.
    It should never be instantiated but injected by the JQM engine. For the injection to take place, 
    the payload main class should have a field of type JobManager (directly or through inheritance, as well as public or private).
    
    Use is very straightforward::
    
        public class App implements Runnable
        {
            private JobManager jm;
            
            @Override
            public void run()
            {
                // JM can be used immediately.
                jm.enqueue("otherjob", "me");
            }
        }

Current job metadata
**********************

For the description of these items, please see the job instance description. Please note that these are methods, not fields - this is
only because Java does not allow to specify fields inside an interface.

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

.. method:: JobManager.enqueue(String applicationName, String user, String mail, String sessionId, String application, String module, String keyword1, String keyword2, String keyword3, Map<String, String> parameters) -> int

    Enqueues a new execution request. This is asynchronous - it returns as soon as the request was posted.
    
    Equivalent to :meth:`JqmClient.enqueue`, but where the parameters are given directly instead of using a :class:`JobRequest` instance. 
    This is a little ugly but necessary due to the underlying class loader proxying magic.
    
    
.. method:: JobManager.enqueueSync(String applicationName, String user, String mail, String sessionId, String application, String module, String keyword1, String keyword2, String keyword3, Map<String, String> parameters) -> int

    Calls :meth:`enqueue` and waits for the end of the execution.

.. method:: JobManager.waitChild(int jobInstanceId) -> void
.. method:: JobManager.waitChildren() -> void

.. method:: JobManager.hasEnded(int jobInstanceId) -> Boolean
.. method:: JobManager.hasSucceeded(int jobInstanceId) -> Boolean
.. method:: JobManager.hasFailed(int jobInstanceId) -> Boolean

Communications
********************

.. method:: JobManager.sendMsg(String message) -> void

    Messages are strings that can be retrieved during run by other applications, so that interactive human users may 
    have a measure of a job instance progress. (typical messages highlight the job's internal steps)
    
.. method:: JobManager.sendProgress(Integer progress) -> void

    Progress is an integer that can be retrieved during run by other applications, so that interactive human users may have 
    a measure of a job instance progress. (typically used for percent of completion)

.. method:: JobManager.addDeliverable(String path, String fileLabel) -> int

    When a file is created and should be retrievable from the client API, the file must be referenced with this method.
    
    **The file is moved by this method!** Only call when you don't need the file any more.
    
    It is strongly advised to use :meth:`getWorkDir` to get a directory where to first create your files.
    
Misc.
************

.. method:: JobManager.defaultConnect() -> String

    The default connection JNDI alias. To retrieve a default connection, simply use::
    
        ((DataSource)InitialContext.doLookup(jm.defaultConnect)).getConnection();
    
    See :ref:`jobs_resource_jdbc` for more details.
    
    Preferably use directly :meth:`JobManager.getDefaultConnection` to directly retrieve a connection.
    
.. method:: JobManager.getDefaultConnection() -> Connection

    A connection as described by the default JNDI alias. See :ref:`jobs_resource_jdbc` for more details.

.. method:: JobManager.getWorkDir() -> File

    If temp files are necessary, use this directory. The directory already exists. It is used by a single instance. It is purged at the end of the run.

.. method:: JobManager.yield() -> void
    
    This simply notifies the engine that it can briefly take over the thread, mostly to check if the thread should commit
    suicide. See :ref:`culling` for more details.    