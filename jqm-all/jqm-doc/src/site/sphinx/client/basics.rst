Basics
#################

.. highlight:: java

The client API is defined an a Java interface, and has two implementations. Therefore, to use the client API, 
one of its two implementations must be imported: either :doc:`the Hibernate JPA 2.0 one<hibernateclient>`
with jqm-api-client-hibernate.jar or :doc:`the web service client<webservice>` with jqm-api-client-jersey.jar.

Then it is simply a matter of calling::

	JqmClientFactory.getClient();

The client returned implements an interface named :class:`JqmClient`, which is profusely documented in JavaDoc form, as well as in the 
:doc:`next section<methods>`. Suffice to say that it contains many methods related to:

* queueing new execution requests
* removing requests, killing jobs, pausing waiting jobs
* modifying waiting jobs
* querying job instances along many axis (is running, user, ...)
* get messages & advancement notices
* retrieve files created by jobs executions
* some metadata retrieval methods to ease creating a GUI front to the API

For example, to list all executions known to JQM::

	List<JobInstance> jobs = JqmClientFactory.getClient().getJobs();

Now, each implementation has different needs as far as configuration is concerned. Basically, Hibernate needs to know how to 
connect to the database, and the web service must know the web service server. To allow easy configuration, the following principles apply:

#. Each client provider can have one (always optional) configuration file inside the classpath. It is specific for each provider, see their doc
#. It is possible to overload these values through the API **before the first call to getClient**::

	Properties p = new Properties();
	p.put("com.enioka.ws.url", "http://localhost:9999/marsu/ws");
	JqmClientFactory.setProperties(p);
	List<JobInstance> jobs = JqmClientFactory.getClient().getJobs();
#. An implementation can use obvious other means. E.g. Hibernate will try JNDI to retrieve a database connection.

The name of the properties depends on the implementation, refer to their respective documentations.

Please note that all implementations are supposed to cache the :class:`JqmClient` object. Therefore, it is customary to simply use JqmClientFactory.getClient()
each time a client is needed, rather than storing it inside a local variable.

For non-Java clients, use the :doc:`web service API<webservice>` which can be called from anywhere.

Finally, JQM uses unchecked exception as most APIs should (see `this article <http://www.artima.com/intv/handcuffs.html>`_).
As much as possible (when called from Java) the API will throw:

* :class:`JqmInvalidRequestException` when the source of the error comes from the caller (inconsistent arguments, null arguments, ...)
* :class:`JqmClientException` when it comes from the API's internals - usually due to a misconfiguration or an environment issue (network down, etc).
