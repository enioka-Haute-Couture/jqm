Scheduling jobs
######################

JQM is at its core a  passive system: it receives execution requests from an external system and processes these requests according to simple queuing mechanisms.
The requests can come from any type of system, the two prime and common examples being an interactive system (a user has pushed a button which has in turn triggered a new job request)
and a scheduler (Control^M, Automic, $Universe... or even the Unix crontab/Windows task scheduler).

However sometimes the need is simply to schedule launches on simple recurrence rules - without any need for the advanced functions offered by full-fledged job scheduler, such as inter-launch dependencies, misfire control...
In this case, the user had before JQM version 2 only two sub-optimal solutions: use a full job scheduler (costly and complicated) or use a crontab script (no logs, no history, ...). 
JQM v2 therefore adds support for simple recurrence rules, which are detailed in this chapter.

Recurrence rules
******************

JQM uses standard crontab expressions. 





.. warning:: beware of pure recurrences! The next occurrence of a recurrence is computed from its previous occurrence, or the start of the scheduler if none. And the scheduling engine inside JQM
   may restart (in case of a manual engine restart for example, or a cluster failover - this is exactly like what would happen in case of an OS restart or an OS cluster failover). It is most 
   often not an issue - a job running every minute most often may run twice in a single minute without consequences. If it is an issue, do not forget to put absolute references in your cron 
   expression. For example, 0 0 * * * means every day at midnight - there cannot be multiple launches
   
   
Time zones
**************

JQM always uses the system time zone to interpret the cron expressions. So if you have a cluster spanning multiple zones, this may lead to weird behaviour.