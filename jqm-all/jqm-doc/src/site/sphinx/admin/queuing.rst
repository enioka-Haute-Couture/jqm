Advanced queuing
######################

.. warning:: This is preliminary documentation for a future release and not included in the released documentation.


How queues work
******************

JQM queues are basically FIFO queues: they are ordered by age, and JQM nodes take the head (the oldest job instances) from the queue first.
However, that's not all JQM is able to do.

Priority
++++++++++++

A first tweak to this is priority: job instances can have a priority attached to them. Priority is an integer between 1 to 10.
A job with a higher priority (closer to 10) will *always start before job instances of lower priorities*.
Basically, the queue is ordered first by priority, then by age.

Priorities can be changed through the client API. Depending on the type of job definition, they may also translate into technical characteristics
such as a thread priority for Java payloads.

Care should be taken when using these static priorities, as they may result in the starvation of lower priority job instances. This is by design,
so only use this mechanism when a "hard priority" is desired.

Resources
++++++++++++++

A second tweak is resources available to the poller. A pure FIFO queue would only start one job instance at the same time, wait for its end, rince and repeat.
This may be useful in many situations, but most often the administrator will want to run a specified count of job instances in parallel.
Therefore we introduce the notion of resource: a job instance may need some resource in order to run.

The most common resource type is thread/processes. If we say there are 3 threads available for polling a given queue on a given node,
the node will have at a given time at most 3 running job instances. Limiting the thread resource is the default inside JQM, with
a thread count given inside the Deployment Parameter (see :doc:`queues`).

However, multiple types of resources exist. JQM therefore defines the notion of **resource managers**. A resource manager is responsible for
a single resource type: threads, ports available, memory available... On each loop, a queue poller will ask the different enabled resource
manager if the job instances it wants to launch are compatible with their resource constraints. The resource manager will answer either
"Go for it", "I'm empty" and "Not enough resource, but I'm not empty yet".

In the last case, JQM will skip the job instance and analyse the next one. This is another non-FIFO behaviour with JQM.


Using resource managers
******************************

.. warning:: in the current JQM release, resource managers are hard coded! There are only three: one thread count per deployment parameter named *thread*, one
    highlander manager per deployment parameter named *highlander*, and one item list manager per node named after a *global parameter*. Future releases will allow to specify as many managers as
    desired and fully specify their parameters.

Parameters for resource managers come from multiple sources, listed here in order of ascending priority:

* a hard-coded default
* a value given as a parameter to the resource manager itself
* a value given in the Job Definition
* a value given in the Job Instance

Each resource manager configuration is identified by a **key**.

Not all parameters can be used in job definition and job instances - the tables below show which parameters can be (column "JI").

Parameters in job definition and job instance must specify which configuration they are targeting as there can be multiple instances of the
same type of resource manager. Therefore, when the parameter
name is `com.enioka.jqm.rm.quantity` and the configuration is named `thread`, the parameter key must be `com.enioka.jqm.rm.thread.quantity`.


Available resource managers
********************************

Quantity resource manager
+++++++++++++++++++++++++++

A Resource Manager which handles a set quantity of a given resource. By default, each run requires one unit of that resource. Blocks once
the resource is exhausted.

This RM has no persistence - resources are counted in memory, and considered free on node startup.

By default it has 10 units and each JI consumes one unit.

It uses a very simple resource model, but can still model things like a thread count, a memory share... It should basically be used every time
a resource can be sliced and must be shared.


+------------------------------------------------------------+----------------------------------------------------------------------------------------------+
| **Identity**                                               |                                                                                              |
+------------------------------------------------------------+----------------------------------------------------------------------------------------------+
| Class name                                                 | com.enioka.jqm.tools.QuantityResourceManager                                                 |
+------------------------------------------------------------+----------------------------------------------------------------------------------------------+
+------------------------------------------------------------+----------------------------------------------------------------+--------+--------+-----------+
| **Parameters**                                             |                                                                | **RM** | **JI** | Default   |
+------------------------------------------------------------+----------------------------------------------------------------+--------+--------+-----------+
| com.enioka.jqm.rm.quantity.quantity                        | The amount of resource available at startup                    | X      |        | 10        |
+------------------------------------------------------------+----------------------------------------------------------------+--------+--------+-----------+
| com.enioka.jqm.rm.quantity.consumption                     | The amount of resource taken by a job instance                 | X      | X      | 1         |
+------------------------------------------------------------+----------------------------------------------------------------+--------+--------+-----------+

.. warning:: in the current release, the default com.enioka.jqm.rm.quantity.quantity comes from the deployment parameter thread count.

Discrete resource manager
+++++++++++++++++++++++++++

A Resource Manager in which the resource is a list of items without order. Each job instance may take 0 to n items. Items are attributed nominaly -
they are named. An environment variable of all attributed items is made available to the job instance for when it runs.

Blocks once the resource is exhausted.

This RM has no persistence - resources are counted in memory, and considered free on startup.

By default it has 10 items named item01 to item10, and JI do NOT use any of them - only job instances with the right parameter will take an item.

It is another very simple resource model with countless applications. This one is best suited when resources actually cannot be shared and must be used by
at most one job instance at the same time.


+------------------------------------------------------------+----------------------------------------------------------------------------------------------+
| **Identity**                                               |                                                                                              |
+------------------------------------------------------------+----------------------------------------------------------------------------------------------+
| Class name                                                 | com.enioka.jqm.tools.DiscreteResourceManager                                                 |
+------------------------------------------------------------+----------------------------------------------------------------------------------------------+
+------------------------------------------------------------+----------------------------------------------------------------+--------+--------+-----------+
| **Parameters**                                             |                                                                | **RM** | **JI** | Default   |
+------------------------------------------------------------+----------------------------------------------------------------+--------+--------+-----------+
| com.enioka.jqm.rm.discrete.list                            | The comma-separated list of items                              | X      |        | yes       |
+------------------------------------------------------------+----------------------------------------------------------------+--------+--------+-----------+
| com.enioka.jqm.rm.discrete.consumption                     | The number of items taken by a job instance                    | X      | X      | 0         |
+------------------------------------------------------------+----------------------------------------------------------------+--------+--------+-----------+
+------------------------------------------------------------+----------------------------------------------------------------------------------------------+
| **Resulting environment variables**                        |                                                                                              |
+------------------------------------------------------------+----------------------------------------------------------------------------------------------+
| JQM_RM_DISCRETE_{configuration key}_ITEMS                  | A comma-separated list of booked items                                                       |
+------------------------------------------------------------+----------------------------------------------------------------------------------------------+

.. warning:: in the current release, the com.enioka.jqm.rm.discrete.list comes from the global parameter named `discreteRmList`. The key of the manager comes
    from the global parameter `discreteRmName`.

Highlander resource manager
+++++++++++++++++++++++++++++++

This applies the highlander rules. As it has no parameters, it is not detailed further.
