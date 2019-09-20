Understanding the execution context
######################################

JQM has a basic promise: *your code runs as if it were running inside a standalone JVM*. That's all.
If your code runs fine with java -jar my.jar (or My.class...), you are all set. Your code will never
see anything from the engine (like the libraries the engine itself use - everything is fully hidden),
nor from other jobs which may run at the same time. It really behaves as if a brand new JVM had been created
just for your job instance (and a new one is created for each different launch).

This chapter is an advanced topic useful if you want to go beyond that and weaken the isolation.

The default mode: isolation
*******************************

As written above, the default mode is "every launch is fully isolated". It works this way: a different class loader
is created for each launch. It only has access to the classes inside the job (the job jar file, its optional "lib" 
directory and its optional Maven dependencies) and the "ext" directory, which contains libraries shared by all job 
definitions.

At the end of each launch, the class loader is garbage collected and never reused.

This means all libraries and classes are loaded on each and every launch. There is no static context which is kept between
launches. This is exactly what happens when a user launches a program inside a JVM, and the JVM stops at the end of the execution.

Also, only the classes which are one of the supported job types (with a static main method, implementing Runnable or implementing JobBase) can run out of the box.

Changing the default mode
******************************

A few parameters can be set to change the default behaviour - i.e. the execution context of all jobs which do not request a specific execution context.
Two modes are possible: 

* a single shared execution context for all job definitions inside all jars
* one execution context for all jobs inside the same jar (therefore one execution context per jar file)

See the global parameters documentation for more details.


Advanced mode: context definition
**************************************

.. highlight:: xml

It is actually possible to specify options defining the execution context inside the deployment descriptor (see :doc:`packaging`).

Here is a full example, explained below::

	<context>
		<name>MyPrettyContext</name>
		<childFirst>true</childFirst>
		<hiddenJavaClasses>java.maths.*</hiddenJavaClasses>
		<tracingEnabled>false</tracingEnabled>
		<persistent>true</persistent>
		
		<runners>com.enioka.jqm.tools.LegacyRunner,com.enioka.jqm.tools.MainRunner,com.enioka.jqm.tools.RunnableRunner</runners>
		
		<eventHandlers>
			<handler>
				<className>com.enioka.handlers.filterOne</className>
				<event>JI_STARTING</event>
				<parameters>
					<parameter>
						<key>keyname</key>
						<value>value</value>
					</parameter>
					<parameter>
						<key>keyname2</key>
						<value>value2</value>
					</parameter>
				</parameters>
			</handler>
		</eventHandlers>
	</context>

Inside the "context" tag the only compulsory information is "name". Everything else is optional and has default values.

A context is defined at the root level of the deployment descriptor. It can be used by any number of job definitions, by using the name of the context::

	<jobDefinition>
		<name>Fibo</name>
		<description>Test based on the Fibonachi suite</description>
		<canBeRestarted>true</canBeRestarted>
		<javaClassName>com.enioka.jqm.tests.App</javaClassName>
		<application>CrmBatchs</application>
		<module>Consolidation</module>
		<keyword1>nightly</keyword1>
		<highlander>false</highlander>
		<executionContext>MyPrettyContext</executionContext>
		<parameters>
			<parameter>
				<key>p1</key>
				<value>1</value>
			</parameter>
			<parameter>
				<key>p2</key>
				<value>2</value>
			</parameter>
		</parameters>
	</jobDefinition>

(note the "executionContext" tag).

Class loading order
+++++++++++++++++++++

A normal JSE class loader is parent first - that is, if a class exists in a lower layer of the class loading hierarchy, 
it will be loaded even if your own jar provides a class of the same package + name.

For example, if your jar contains a java.util.String class, it will never be loaded as it's defined in the JDK itself,
the lowest level and therefore the highest priority.

Sometimes, you will want to give priority to your own classes. This is done by setting "childFirst" to "true". In that case,
a class will be loaded from the lower levels only if not defined in your job (and its libraries).

A similar effect can be obtained by simply hiding classes, see next paragraph.

Default is "false" - meaning parent first.

Hiding Java classes
+++++++++++++++++++++

Changing the class loading loading priority is radical, sometimes you just want to override a small set of classes. To do that, 
just put a comma-separated list of regular expressions inside the "hiddenJavaClasses" tag. Classes which match at least one of the regular expressions will never ever
be loaded from a source outside your own jar and libraries.

Default is no exclusions.

Class loading tracing
+++++++++++++++++++++++

To debug "why isn't my library loaded" issues, you can enable a trace by setting the "tracingEnabled" parameter to "true".
The trace is written in the log (and stdout if active).

Default is "false" - meaning disabled.

Context persistence
+++++++++++++++++++++++++

By default, the context is destroyed at the end of a run. This means there is no possibility to set anything static in a first
run and retrieve it in a further job. While this is most often an excellent programming principle (no side effects possible!), it may
be detrimental to some programs. For example, initializing a JPA provider such as Hibernate has a huge cost be it in memory
or CPU cycles, which is why the JPA context (the EntityManagerFactory - EMF) is usually a shared static singleton. But as the context is
thrown out at the end of each execution, with it goes the static context too, and the EMF has to be re-created on each run.

To avoid this, a context can be set as persistent. Just set "persistent" to "true". In that case the context will be created the
first time it is needed, and kept forever afterwards.

.. warning:: enabling context persistence also means side effects become possible once again, as well as many other issues like some memory leaks
	which otherwise would just disappear with the context. To be enabled only by users who fully understand the implications!

.. note:: if a same context is referenced by multiple job definitions, and this context is persistent, it means that at runtime the same context is used
	by multiple job instances coming from different job definitions! This is often what is desired - sharing a static context between multiple job types.
	But it of course also increases the risk of unforeseen side effects.
	
The default is "true" when a context is specified. If a job definition is not associated with a specific context, the default is false.
	
Runners
+++++++++++

The runners are the agents responsible for actually launching the job instances. The example above actually give the default value, which
is a comma-separated list of the three runners corresponding to the three different types of supported job definitions: 

* com.enioka.jqm.tools.LegacyRunner runs any class which implements the "JobBase" interface
* com.enioka.jqm.tools.MainRunner runs any class with a "static main" method
* com.enioka.jqm.tools.RunnableRunner runs any class which implements the Runnable interface and has a default no arguments constructor.

This list allows to restrict the job types available inside the context.

Note that the runners only exist to define "how to start" a job instance. They cannot do more, and they actually run in a very limited
bubble with only access to themselves and the JDK.

Event handlers
++++++++++++++++

A common requirement is to be able to run code at different times in the life cycle of a job instance. JQM allows this for one type of event, 
when a job instance is about to start.

The handlers run in the same context as the job instance itself. It means the class of the handler is inside the class path of the job instance itself.
It is the responsibility of the developer to check there are no conflicts between his own code and the handler code.

The handler parameters are key/value pairs, with unique keys.

.. warning:: handlers are provided by the job definition itself, not by the engine. They MUST be present inside the available libraries 
	(be it from a Maven dependency, a jar inside the "lib" directory, inside the über-jar...)

For an example of the use of an interpretor in the context of a Spring application, see :doc:`spring` where one is used to bootstrap the Spring context
(much like when a listener is often used when dealing with Spring in a servlet container).

