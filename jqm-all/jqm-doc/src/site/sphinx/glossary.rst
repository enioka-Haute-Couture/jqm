Glossary
###########

.. glossary::
	
	Payload
		the actual Java code that runs inside the JQM engine, containing business logics. This is must be provided by 
		the application using JQM.
	
	Job Definition
	JobDef
		the metadata describing the payload. Also called JobDef. Entirely described inside the JobDef XML file.
		Identified by a name called "Application Name"    

	Job Request
		the action of asking politely the execution of a :term:`JobDef` (which in turn means running the payload) 

	Job Instance
		the result of of a Job Request. It obeys the Job Instance lifecycle (enqueued, running, endded, ...). It is archived at the
		end of its run (be it successful or not) into the history.
		
	JQM Node
	JQM Engine
		an instance of the JQM service (as in 'Windows service' or 'Unix init.d service') that can run payloads
	
	Job queue
	Queue
		a virtual FIFO queue where :term:`job requests<Job Request>` are lined up. These queues are polled by some :term:`nodes<JQM Node>`.
		
	Enqueue
		the action of putting a new :term:`Job Request` inside a :term:`Queue`. The queue is usually determined by the :term:`JobDef` which holds
		a default queue.

