Database and reliability
##########################

As shown in :doc:`../archi` JQM nodes need the database to run, since they are basically polling the database for job instances to run.
So when the database goes down, nodes are unable to work correctly. However, they won't go down, for this would be an administration nightmare (for example, a database cluster switch over will briefly cut connectivity but should not require to restart a hundred JQM processes as it is a standard operation in many environments). They will just wait for the database to come back online.

In details:

* pollers stop on first failure. Therefore, no new job instance will run until database connectivity is restored. Failed pollers are restarted on database coming back on line.
* running job instances continue to run as long as they are not concerned with database connectivity. 
    * They will be impacted if they use some JQM API methods that call the database behind the scenes, such as when they themselves enqueue new job execution requests
    * They will not be impacted otherwise
    * Impacted instances will classically crash with a JDBC exception.
* ending job instances are stored in memory and wait for the database to come back to be reported. This is referred to as "delayed finalization" inside the logs.
    * Therefore, these instances will be stored as "running" inside the database even if they are actually done. Not that it matters if the database is fully down, but if only the connectivity is down and the db is up.
    
    
Pollers failing and the need for delayed finalization are reported as errors inside the main log. Coming back online operations are reported as warnings.

Finally, please note that delayed finalization is purely an in-memory process. That is, if the node is stopped, the state of the ended job instance is lost. On next node startup, the node will realize it does not know what has happened to a job instance it was running before being killed and will report it as crashed, even if it had ended correctly. This is to avoid false OK that would cause havoc inside scheduled production plans. So the rule of thumb is: *do not restart JQM nodes when the database is unavailable*.