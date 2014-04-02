Query API
############

The query API is the only part of the client API that goes beyond a simple method call, and hence deserves
a dedicated chapter. This API allows to easily make queries among the past and current :term:`job instance` s, 
using a fluent style.

Basics, running & filtering
*****************************

To create a :class:`Query`, simply do *Query.create()*. This will create a query without any predicates - if run, it will return
the whole execution history.

To add predicates, use the different :class:`Query` methods. For example, this will return every past instance for the :term:`job definition` named JD::

	Query.create().setApplicationName("JD");

To create predicates with wildcards, simply use "%" (the percent sign) as the wildcard. This will return at least the results of the previous
example and potentially more::

	Query.create().setApplicationName("J%");

To run a query, simply call run() on it. This is equivalent to calling *JqmClientFactory.getClient().getJobs(Query q)*. Running the previous example
would be::

	List<JobInstance> results = Query.create().setApplicationName("J%").run();

Querying live data
********************

By default, a Query only returns instances that have ended, not instances that are inside the different :term:`queues<queue>`. 
This is for performance reasons - the queues are the most sensitive part of the JQM database, and live in different tables than 
the History.

But it is totally supported to query the queues, and this behaviour is controlled through two methods: 
Query.setQueryLiveInstances (default is false) and Query.setQueryHistoryInstances (default is true). For example, 
the following will query only the queues and won't touch the history::

	Query.create().setQueryLiveInstances(true).setQueryHistoryInstances(false).setUser("test").run();

.. note:: When looking for instances of a desired state (ENDED, RUNNING, ...), it is highly recommended to query only the queue or only the history.
	Indeed, states are specific either to the queue or to history: an ended instance is always in the history, a running instance always 
	in the queue, etc. This is far quicker than querying both history and queues while filtering on state.

Pagination
**************

The history can grow very large - it depends on the activity inside your cluster. Therefore, doing a query that 
returns the full history dataset would be quite a catastrophy as far as performance is concerned (and would
probably fail miserably out of memory).

The API implements pagination for this case, with the usual first row and page size. ::

	Query.create().setApplicationName("JD").setFirstRow(100000).setPageSize(100).run();
	
.. warning:: failing to use pagination on huge datasets will simply crash your application.

Pagination cannot be used on live data queries - it is supposed there are never more than a few rows inside the queues.
Trying to use it nevertheless will trigger an JqmInvalidRequestException.

Sorting
********

The most efficient way to sort data is to have the datastore do it, especially if it is an RDBMS like in our case. The Query API 
therefore allows to specify sort clauses::

	Query.create().setApplicationName("J%").addSortAsc(Sort.APPLICATIONNAME).addSortDesc(Sort.DATEATTRIBUTION);
	
The two addSortxxx methods must be called in order of sorting priority - in the example above, the sorting is first by ascending 
application name (i.e. batch name) then by descending attribution date. The number of sort clauses is not limited.

Please note that sorting is obviously respected when pagination is used.

Shortcuts
***********

A few methods exist in the client API for the most usual queries: running instances, waiting instances, etc. These
should always be used when possible instead of doing a full Query, as the shortcuts often have optimizations
specific to their data subset.

Sample
**************

JQM source code contains one sample web application that uses the Query API. It is a J2EE JSF2/Primefaces form that exposes the full history
with all the capabilities detailed above: filtering, sorting, pagination, etc.

It lives inside jqm-all/jqm-webui/jqm-webui-war.

.. note:: this application is but **a sample**. It is not a production ready UI, it is not supported, etc.