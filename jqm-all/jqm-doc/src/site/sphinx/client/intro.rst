Introduction
#####################

A "client" is an external agent (Java program, shell script, ...) that needs to interact with the root function of JQM: job queueing and execution [#f1]_. JQM
offers multiple ways to do so, each being tailored to a specific type of client.

* a minimal web service API with very simple signatures, designed for scripts and the like, called the **simple API**
* a full **client API** designed for more evolved programs. It is a superset of the minimal API (and actually directly reuses some of its methods). It has two (functionally equivalent) implementations:
	* a set of (language agnostic) REST web-services
	* a direct-to-database JPA2 implementation
* a minimal command line utility (**CLI**)
* for payloads running inside a JQM engine only, it is also possible to access a subset of the full client API as exposed through an object injected by the engine. it is called the **engine API**.

.. [#f1] Therefore, all administrative functions (restart a JQM engine, modify a job parameter, ...) are fully excluded from this section. They are detailed inside a dedicated section.