Client API details
###################

.. highlight:: java

The JqmClient interface
*************************

.. class:: JqmClient

    This interface contains all the necessary methods to interact with JQM functions.
    
    **All methods have detailed Javadoc. The Javadoc is available on Maven Central** (as are the binaries and the source code).
    This paragraph gives the methods prototypes as well as how they should be used. For details on exceptions thrown, etc. please
    refer to the javadoc.
        
New execution requests
++++++++++++++++++++++++++++++++++++++
    
    .. method:: JqmClient.enqueue(JobRequest executionRequest) -> integer
        
        The core method of the Job Queue Manager: it enqueues a new job execution request, as described in the object parameter.
        It returns the ID of the request. This ID will be kept throughout the life cycle of the request until it becomes the ID 
        of the history item after the execution ends. This ID is reused in many other methods of the API.
        
        It consumes a :class:`JobRequest` item, which is a "form" object in which all ncessary parameters can be specified.
        
    .. method:: JqmClient.enqueue(String applicationName, String user) -> integer
    
        A simplified version of the method above.
    
    .. method:: JqmClient.enqueueFromHistory(Integer jobIdToCopy) -> integer
    
        This method copies an ended request. (this creates a new request - it has no impact whatsoever on the copied request)
    
Job request deleting
++++++++++++++++++++++++++++++++++++++
    
    .. method:: JqmClient.cancelJob(Integer id) -> void
    
        When called on a waiting execution request, removes it from the queue and moves it to history with CANCELLED status.
        This is the standard way of cancelling a request.
        
        *Synchronous method*
        
    .. method:: JqmClient.deleteJob(int id) -> void
    
        This method should not usually be called. It completely removes a job execution request from the database.
        Please use cancelJob instead.
        
        *Synchronous method*
        
    .. method:: JqmClient.killJob(int id) -> void
        
        Attempts to kill a running job instance. As Java thread are quite hard to kill, this may well have no effect.
        
        *Asynchronous method*
    
Pausing and restarting jobs
++++++++++++++++++++++++++++++++++++++
    
    .. method:: JqmClient.pauseQueuedJob(int id) -> void
    
        When called on a job execution request it is ignored by engines and status forever in queue.
        
    .. method:: JqmClient.resumeJob(int id) -> void
    
        Will re insert a paused execution request into the queue. The place inside the queue may change from what it
        used to be before the pause.
        
    .. method:: JqmClient.restartCrachedJob(int id) -> int
    
        Will create an execution request from a crashed history element and *remove all traces of the failed execution**.
        
Queries on Job instances
++++++++++++++++++++++++++++++++++++++

The API offers many methods to query either ended jobs or waiting/running ones. When there is a choice, please use
the method which is the mst specific to your needs, as it may have optimizations not present in the more general ones.

    .. method:: JqmClient.getJob(int id) -> JobInstance
    
        Returns either a running or an ended job instance.
        
    .. method:: JqmClient.getJobs() -> List<JobInstance>
    
        Returns all job instances.
        
    .. method:: JqmClient.getActiveJobs() -> List<JobInstance>
    
        Lists all waiting or running job instances.
        
    .. method:: JqmClient.getUserActiveJobs(String username) -> List<JobInstance>
    
        Lists all waiting or running job instances which have the given "username" tag.
        
    .. method:: JqmClient.getJobs(Query q) -> List<JobInstance>
    
        please see :doc:`query`.
    
Quick access helpers
++++++++++++++++++++++++++++++++++++++

    .. method:: JqmClient.getJobMessages(int id) -> List<String>
    
        Retrieves all the messages created by a job instance (ended or not)
    
    .. method:: JqmClient.getJobProgress(int id) -> int
    
        Get the progress indication that may have been given by a job instance (running or done).
        
Files & logs retrieval
++++++++++++++++++++++++++++++++++++++

    .. method:: JqmClient.getJobDeliverables(int id) -> List<Deliverable>
    
        Return all metadata concerning the (potential) files created by the job instance: Excel files, PDFs, ...
        These are the files explicitly referenced by the job instance through the :meth:`JobManager.addDeliverable` method.
        
    .. method:: JqmClient.getDeliverableContent(Deliverable d) -> InputStream
    
        The actual content of the file described by the :class:`Deliverable` object. 
        
        **This method, in all implementations, uses a direct HTTP(S) connection to the engine that has run the job instance.**
        
        **The responsibility to close the stream lies on the API user**
    
    .. method:: JqmClient.getDeliverableContent(int deliverableId) -> InputStream
    
        Same a above.
    
    .. method:: JqmClient.getJobDeliverablesContent(int jobId) -> List<InputStream>
    
        Helper method. A loop on :meth:`getDeliverableContent` for all files created by a single job instance.
    
    .. method:: JqmClient.getJobLogStdOut(int jobId) -> InputStream

        Returns the standard output flow of of an ended job instance. 
        
        **This method, in all implementations, uses a direct HTTP(S) connection to the engine that has run the job instance.**
        
        **The responsibility to close the returned stream lies on the API user**
        
    .. method:: JqmClient.getJobLogStdErr(int jobId) -> InputStream
    
        Same as :meth:`getJobLogStdOut` but for standard error flow.
        
Referential queries
++++++++++++++++++++++++++++++++++++++

These methods allow to retrieve all the referential data that may be needed to use the other methods: queue names, application
names, etc.

    .. method:: JqmClient.getQueues() -> List<Queue>
    
    .. method:: JqmClient.getJobDefinitions() -> List<JobDef>
    
    .. method:: JqmClient.getJobDefinition(String applicationName) -> JobDef

API objects
*****************

JobRequest
++++++++++++++

.. class:: JobRequest

    Job execution request. It contains all the data needed to enqueue a request (the application name), as well as non-mandatory data. 
    It is consumed by :meth:`JqmClient.enqueue`.
    
    **Basically, this is the form one has to fill in order to submit an execution request.**
    
Queue
+++++++++++

.. class:: Queue

    All the metadata describing a :term:`queue`. Read only element.
    
    Please note there is another queue class that exists within JQM, inside the com.enioka.jqm.jpa packages.
    The JPA one is an internal JQM class and should not be confused with the API one, which is a stable interface.
    
JobDef
+++++++++++
    
.. class:: JobDef
    
    All the metadata describing a :term:`job definition`. Read-only element.
    
    Please note there is another class with this name that exists within JQM, inside the com.enioka.jqm.jpa packages.
    The JPA one is an internal JQM class and should not be confused with the API one, which is a stable interface.
    
    
Example
************

::

    # Enqueue a job
    int i = JqmClientFactory.getClient().enqueue("superbatchjob");
    
    # Get its status
    Status s = JqmClientFactory.getClient().getStatus(i);
    
    # If still waiting, cancel it
    if (s.equals(State.WAITING))
        JqmClientFactory.getClient().cancel(i);
    

