Scheduling jobs
######################

JQM is at its core a  passive system: it receives execution requests from an external system and processes these requests according to simple queuing mechanisms.
The requests can come from any type of systems, the two prime and common examples being an interactive system (a user has pushed a button which has in turn triggered a new job request)
and a scheduler (Control^M, Automic, $Universe... or even the Unix crontab/Windows task scheduler).

However sometimes the need is simply to schedule launches on simple recurrence rules - without any need for the advanced functions offered by full-fledged job scheduler, such as inter-launch dependencies, misfire control...
In this case, the user had before JQM version 2 only two sub-optimal solutions: use a full job scheduler (costly and complicated) or use a crontab script (no logs, no history, ...). 
JQM v2 therefore adds support for simple recurrence rules, which are detailed in this chapter.

Recurrence rules
******************

What they do
=================

Any job definition can be associated with zero to any number of schedules. The "schedule" object defines:

* a recurrence (see below), like * * * * * for "every minute"
* optionally, a queue which will override the default queue from the job definition
* optionally, a set of parameters which will override the ones inside the job definition.

Rule syntax
=================

JQM uses standard crontab expressions. Here is a primer, taken from the documentation of the library used for parsing cron expressions:

There are five parts inside a cron pattern.

* Minutes: 0 to 59
* Hours: 0 to 23
* Days of month: 1 to 31 (L means last day of month)
* Month: 1 to 12 (or jan to dec)
* Day of week (0 sunday to 6 saturday, or sun-sat)

The star * means "any". The comma is used to specify a list of values for a part. The dash is used to specify a range.

The scheduler will trigger when all five parts of the pattern are verified true.

Examples:

* \* \* \* \* \*: every minute
* 4 \* \* \* \*: every hour at minute 4. 
* \*/5 \* \* \* \*: every five minute (00:00, 00:05, 00:10...)
* 3-18/5 \* \* \* \*: every 5 minutes between the third and the eighteenth minute of every hour. (00:03, 00:08, 00:13, 00:18, 01:03, ...)
* \*/15 9-17 \* \* \*: every 15 minutes during business hours (from 09:00 until 17:45).

   
Time zones
=============

JQM always uses the system time zone to interpret the cron patterns. So if you have a cluster spanning multiple zones, this may lead to weird behaviour.

How to create them
======================

The administrator will use the web administration "job definitions" page. It is a button enabled when a job definition is selected.

The programmer will use the JobRequest API, which has been extended.

.. class:: JobRequest

	.. method:: JobRequest.setRecurrence(String cronExpression)
	
		 This actually creates a Scheduled Job for the given applicationName and optionally queue and parameters. (all other JobRequest elements are ignored). Note that when using this, there is no request immediately added to the queues - the actual requests will be created by the schedule.
		 

	.. method:: JobRequest.setScheduleId(Integer id)
	
		This creates an occurrence from an existing schedule instead of simply from the job definition (this does not affect the recurrence itself which continues to run normally)

Also note that a JqmClient.removeRecurrence exists to remove schedules created by this method.
		
Starting later
****************

Another type of scheduling is "one off" runs. This is done through the method:

	.. method:: JobRequest.setRunAfter(Calendar after)
	
		Put the request inside the queue but do not run it when it reaches the top of the queue. It will only be eligible for run when the given date is reached. When the given date is reached, standard queuing resumes.
		The resolution of this function is the minute: seconds and lower are ignored (truncated).


Manual scheduling
*******************

If you want to control the release time at will (the "run after" date is not known at the time of enqueuing) use this:

	.. method:: JobRequest.startHeld()
		
		Put the request inside the queue but do not run it until the JqmClient.resumeJob(int) method is called on the newly created job instance.
