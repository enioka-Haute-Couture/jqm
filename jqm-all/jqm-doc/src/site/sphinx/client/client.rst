Interacting with JQM
############################

.. highlight:: java

Client API
******************

The **client API** enables any Java (and other languages for some implementations of the API) program to interact
with the very core function of JQM: asynchronous executions. This API exposes every common method pertaining to this goal:
new execution requests, checking if an execution is finished, listing finished executions, retrieving files created by an execution...

To use it, one of the two implementations of the client API must be imported: either :doc:`an Hibernate JPA 2.0 client<hibernateclient>`
with jqm-api-client-hibernate.jar or :doc:`a web service client<webservice>` with jqm-api-client-jersey.jar.

Then it is simply a matter of calling::

	JqmClientFactory.getClient();

The client returned implements an interface named :class:`JqmClient`, which is profusely documented in JavaDoc form. Suffice to say that 
it contains many methods related to:

* enqueing new execution requests
* removing requests, killing jobs, pausing waiting jobs
* modifying waiting jobs
* querying job instances along many axis (is running, user, ...)
* get messages & advancement notices
* retrieve files created by jobs executions
* some metadata retrieval methods to ease creating a GUI front to the API

For exemple, to list all executions known to JQM::

	List<JobInstance> jobs = JqmClientFactory.getClient().().getJobs();

Now, each implementation has different needs as far as configuration is concerned. Basically, Hibernate needs to know how to 
connect to the database, and the web service must know the web service server. To allow easy configuration, the following principles apply:

#. Each client provider can have one (always optional) configuration file inside META-INF. It is specific for each provider, see their doc
#. It is possible to overload these values through the API **before the first call to getClient**::

	Properties p = new Properties();
	p.put("com.enioka.ws.url", "http://localhost:9999/marsu/ws");
	JqmClientFactory.setProperties(p);
	List<JobInstance> jobs = JqmClientFactory.getClient().().getJobs();

The name of the properties depends on the provider, refer to their respective documentations.

Please note that all implementations are supposed to cache the :class:`JqmClient` object. Therefore, it is customary to simply use JqmClientFactory.getClient()
each time a client is needed, rather than storing it inside a local variable.

For non-Java clients, use the :doc:`web service API<webservice>` which can be called from anywhere.

From scripts
*******************

A very rudimentary "web service" exists to allow for curl/wget-style interaction. (it has nothing to do with the full web 
service API, which is optional. The script API is always present). Basically, it allows to easily launch a JQM job from a job scheduler.

To enqueue a job, POST on /enqueue with the following POST compulsory parameters: application name, user and the following 
POST optional parameters: module, mail, other1, other2, other3, parentid, sessionid. Parameters are given as param_nn for the name 
and paramvalue_nn for the value. Signification of these is the same as in the client API. The server answers with the ID of the request.

To retrieve the status of a request, GET on /status?id=nnn Status is given as text.

.. warning:: there is no authentication. It will be implemented one day as an option. See ticket #9.
