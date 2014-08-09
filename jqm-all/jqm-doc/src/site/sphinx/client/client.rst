Full client API
############################

The **client API** enables any program (in Java, as well as in any other languages for some implementations of the API) to interact
with the very core function of JQM: asynchronous executions. This API exposes every common method pertaining to this goal:
new execution requests, checking if an execution is finished, listing finished executions, retrieving files created by an execution...

It is named "client API" because it contains the methods that are often directly exposed to human end-users. They may,
for example, have a web-based GUI with buttons such as "I want to run that report", "let's synchronize invoices with accounting", ... which
will in turn submit a job execution request to JQM. This client API contains such a submission method, as well as all the
others the end user may need, such as "is my job done", "cancel this job", and so on. And obviously, what is true for human clients
is also true for automated systems - for example, a scheduler may use this API (even if the :doc:`minimal` may be better suited for this).


.. toctree::
	
	basics
	methods
	query
	hibernateclient
	webservice
