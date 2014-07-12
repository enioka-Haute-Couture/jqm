CLI API
#################

The Command Line Interface has a few options that allow any program to launch a job instance and run a few other commands. The CLI is actually described in the :doc:`/admin/cli` chapter of the administration section.

.. warning:: the CLI creates a JVM with a full JDBC pool on each call call. This is horribly inefficient. A new CLI based on the web services is being considered.


Engine API
#############

This is a subset of the Client API designed to be usable from payload running inside a JQM engine without any required libraries besides an interface named JobManager.

Its main purpose is to avoid needing the full client library plus Hibernate (a full 20MB of perm gen space, plus a very long initialization time...) just for doing client operations - why not simply use the
already initialized client API used by the engine itself? As there is a bit of classloading proxying magic involved, the signatures are not strictly the same to the ones of the client API but near enough so as
not be lost when going from one to the other.

TL;DR: inside a JQM payload, use the engine API, not the full client API (unless needing a method not exposed by the engine API).

This engine API is detailed in a chapter of the "creating payloads" section: :doc:`/jobs/engineapi`.
