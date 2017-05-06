Using Spring
#############################

This gives a rundown on how to efficiently use Spring inside JQM. This can of course be an inspiration for other big "container" frameworks.

There are multiple possibilities, and this page shows how to use two of them. They are presented here in order of increasing complexity, which is also the order of decreasing recommendation.

By doing nothing special
**************************

It has been said before, by default launching a new job instance in a JQM server is like launching a new JVM: if a Spring job already works from the command line, it will work in JQM without adaptation.

There are different ways to create Spring programs, but they all boil down to: create a Spring context, load configuration inside the context, create the job bean from the context and launch it.

A most common example is by using Spring Boot, which hides most boilerplate code. The main method is simply::

	import org.springframework.boot.SpringApplication;

	public class Application
	{
		public static void main(String[] args)
		{
			SpringApplication.run(MyJob.class, args);
		}
	}

And the job implements CommandLineRunner, which will automatically instantiate the bean and run it on context creation::

	@Import(ContextConfig.class)
	@SpringBootApplication
	public class MyJobClass implements CommandLineRunner
	{
		@Autowired
		private MyService myServiceToInject;

		@Override
		public void run(String... args) throws Exception
		{
			myServiceToInject.doSomething();
			System.out.println("Job is done!");
		}
	}

In terms of job definition, the Application class is the JQM entry point. JQM knows nothing about Spring, it is just another main method to run.

Advantages:

* direct code reuse from CLI batch jobs
* just another job definition - nothing special to do
* free to initialize and configure Spring in any way: annotations, XML, packages to scan or ignore...

Cons:

* the Spring context is recreated on each launch, which is costly.

This is the recommended way of using Spring inside JQM, in the "keep it simple" philosophy.

.. note:: a full working sample is included inside the JQM integration tests. It is named "jqm-test-spring-1". (it also uses JPA with a JNDI resource handled by the JQM JNDI directory)


By having JQM set the context 
******************************************

In this option, there is only one Spring context for all job definitions using Spring. The jobs themselves (payload code) 
do no Spring context initialization - they just use Spring features (injection...) and do not care where they do come from.

This option is the direct equivalent of what happens inside a servlet container (Tomcat...) when using Spring: the context 
is actually initialized by a servlet initialization listener, and the application code just uses Spring, never creating a SpringContext itself.

JQM uses the same method, with an event handler. It also has a specialized runner which retrieves the job bean from the Spring context
and runs it (it must implement Runnable).

The payload can be defined like this::

	package com.compagny.project;
	
	@Component
	public class MyJobClass implements Runnable
	{
		@Autowired
		private MyService myServiceToInject;
		
		@Resource(name = "runtimeParameters")
		private Map<String, String> parameters;

		@Override
		public void run()
		{
			myServiceToInject.doSomething();
			System.out.println("Job is done!");
		}
	}

and there is no need for an encapsulation class like the Application class of the previous methods: JQM directly runs the job bean.

It is necessary to add the handler and runner to the execution context inside the deployment descriptor::

	<?xml version="1.0" encoding="UTF-8"?>
	<jqm>
		<jar>
			<path>directory/springjobs.jar</path>
			<jobdefinitions>
				<jobDefinition>
					<name>FirstJob</name>
					<description>Does something</description>
					<canBeRestarted>true</canBeRestarted>
					<javaClassName>com.compagny.project.MyJobClass</javaClassName>
					<module>BatchJobs</module>
					<highlander>false</highlander>
					<executionContext>MainSharedSpringContext</executionContext>
				</jobDefinition>
			</jobdefinitions>
		</jar>

		<context>
			<name>MainSharedSpringContext</name>
			<childFirst>false</childFirst>
			<hiddenJavaClasses></hiddenJavaClasses>
			<tracingEnabled>false</tracingEnabled>
			<persistent>true</persistent>
			
			<runners>com.enioka.jqm.runner.spring.AnnotationSpringRunner</runners>
			<eventHandlers>
				<handler>
					<className>com.enioka.jqm.handler.AnnotationSpringContextBootstrapHandler</className>
					<event>JI_STARTING</event>
					<parameters>
						<parameter>
							<key>additionalScan</key>
							<value>com.compagny.project</value>
						</parameter>
					</parameters>
				</handler>
			</eventHandlers>
		</context>
	</jqm>

The handler will intercept the "job instance is starting" event and initialize if needed an AnnotationConfigApplicationContext. All parameters are optional:

* additionalScan: a set of base packages to scan for annotations. Example: com.compagny.project,com.compagny.otherpackage
* beanNameGenerator: a fully qualified class implementing the BeanNameGenerator interface with a no-args constructor to use for creating the names of the beans
* contextDisplayName: name of the context in the logs
* contextId: id of the context bean
* allowCircularReferences: if "true", the context will allow circular references.


If no parameters are given, the job class (the first one to run) itself will be added to the Spring context, so if is a @Configuration it will be enabled.

The handler must be present in the job dependencies. In this case, it is provided with JQM, and the artifact is "com.enioka.jqm:jqm-runner-spring:${jqmversion}"

.. warning:: it is not possible to extend a Spring context after it has been initialized ("refreshed" in Spring-talk). So you must take care to put
	all your jobs inside the same class path so they are present during the initial scan. Basically, it means packaging all jobs in a single jar (or a single jar + set of libs). As this
	is the most common packaging method in the JQM ecosystem (since it is the simplest), and as the Spring world very often uses ûber-jars, this 
	should not be seen as a huge limitation.

Note we have only selected a single runner, which is provided by JQM: com.enioka.jqm.runner.spring.Runner. Depending on your needs, you may want
to add the other runners (if you do not launch only Spring jobs in the same execution context for example).

Finally you may have noted in the sample that we had a @Resource(name = "runtimeParameters") Map: the runner actually registers a named bean to allow 
access to the job instance parameters through the Spring APIs. This bean is scoped on the thread, so you'll obviously get different values in different 
job instances even if they run at the same time in the same runtime context.

.. note:: a full working sample is included inside the JQM integration tests. It is named "jqm-test-spring-2". It's deployment descriptor is named "xmlspring.xml".

