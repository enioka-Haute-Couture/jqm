Payload basics
#########################

JQM is a specialized application server dedicated to ease the management of Java batch jobs. 
Application servers usually have two main aspects: on one hand they bring in frameworks to help writing the business programs,
on the other they try to ease daily operations. For example, JBoss or Glassfish provide an implementation of the EE6 framework for building 
web applications, and provide many administration utilities to deploy applications, monitor them, load balance them, etc.

JQM's philosophy is that **all existing Java programs should be reusable as is**, and that programmers should be free to use whatever frameworks 
they want (if any at all). Therefore, JQM nearly totally forgoes the "framework" part and concentrates on
the management part. For great frameworks created for making batch jobs easier to write, look at Spring batch, a part of Spring, or JSR 352, a part of EE7.
As long as the required libraries are provided, JQM can run :term:`payloads<payload>` based on all these frameworks.

**This section aims at giving all the keys to developers in order to create great batch jobs for JQM**. This may seem in contradiction with 
what was just said: why have a "develop for JQM" chapter if JQM runs any Java code?

* First, as in all application server containers, there a a few guidelines to respect, such as packaging rules.
* Then, as an option, JQM provides a few APIs that can be of help to batch jobs, such as getting the ID of the run or the caller name.

But this document must insist: unless there is a need to use the APIs, there is no need to develop specifically for JQM. **JQM
runs standard JSE code**. 

.. highlight:: java

Payloads types
********************

There are three :term:`payload<payload>` types: programs with a good old main (the preferred method for newly written jobs), and two 
types designed to allow reuse of even more existing binaries: Runnable implementers & JobBase extenders.

Main
---------

This a classic class containing a "static void main(String[] args)" function.

In that case, JQM will simply launch the main function. If there are some arguments defined (default arguments in the :term:`JobDef` or
arguments given at enqueue time) their value will be put inside the String[] parameter *ordered by key name*.

There is no need for any dependencies towards any JQM libraries in that case - direct reuse of existing code is possible.

This would run perfectly, without any specific dependencies or imports::

    public class App
    {
        public static void main(String[] args)
        {
            System.out.println("main function of payload");
        }
    }


.. note:: It is not necessary to make jars executable. The jar manifest is ignored by JQM.

Runnable
--------------

Some existing code is already written to be run as a thread, implementing the Runnable interface. If these classes have a no-argument
constructor (this is not imposed by the Runnable interface as interfaces cannot impose a constructor), JQM can instantiate 
and launch them. In that case, the run() method from the interface is executed. As it takes no arguments, it is not possible to access
parameters without using JQM specific methods as described later in this chapter.

This would run perfectly, without any specific dependencies or imports::

    public class App implements Runnable
    {
        @Override
        public void run()
        {
            System.out.println("run method of runnable payload");
        }
    }

Explicit JQM job
-------------------

.. warning:: This is deprecated and should not be used for new payloads

This type of job only exists for ascending compatibility with a former limited JQM version. It consisted in subclassing class JobBase,
overloading method start() and keeping a no-arg constructor. Parameters were accessible through a number of accessors of the base class.

For example (note the import and the use of an accessor from the base class)::

    import com.enioka.jqm.api.JobBase;
    
    public class App extends JobBase
    {
        @Override
        public void start()
        {
            System.out.println("Date: " + new Date());
            System.out.println("Job application name: " + this.getApplicationName());
        }
    }


It requires the following dependency (Maven)::

    <dependency>
        <groupId>com.enioka.jqm</groupId>
        <artifactId>jqm-api</artifactId>
        <version>${jqm.version}</version>
    </dependency>

.. _accessing_jqm_api:

Accessing the JQM engine API
**********************************

Sometimes, a job will need to directly interact with JQM, for operations such as:

* :term:`enqueue` a new :term:`Job Request`
* get the different IDs that identify a :term:`Job Instance` (i.e. a run)
* get a resource (see :doc:`resources`)
* get the optional data that was given at :term:`enqueue` time
* report progress to an end user
* ...

For this, an interface exists called :class:`JobManager` inside jar jqm-api.jar. Using it is trivial: 
just create a field (static or not) inside your job class (whatever type - Main, Runnable or JQM) and the engine 
will **inject an implementation ready for use**.

.. note:: the 'explicit JQM jobs' payload type already has one :class:`JobManager` field named jm defined in the base class JobBase - it would have
    been stupid not to define it as the API must be imported anyway for that payload type. 

The dependency is::

    <dependency>
        <groupId>com.enioka.jqm</groupId>
        <artifactId>jqm-api</artifactId>
        <version>${jqm.version}</version>
        <scope>provided</scope>
    </dependency>

For more details, please read :doc:`engineapi`.

.. note:: the scope given here is provided. It means it will be present for compilation but not at runtime. Indeed, JQM always provides the jqm-api.jar to
   its payloads without them needing to package it. That being said, packaging it (default 'compile' scope) is harmless as it will be ignored at runtime in
   favour of the engine-provided one.
    
Creating files
******************

An important use case for JQM is the generation of files (such as reports) at the direct request of an end-user through a web interface (or other interfaces).
It happens when generating the file is too long or resource intensive for a web application server (these are not made 
to handle 'long' processes), or blocking a thread for a user
is unacceptable: the generation must be deported elsewhere. JQM has methods to do just that.

In this case, the :term:`payload` simply has to be the file generation code. However, JQM is a distributed system, so
unless it is forced into a single node deployment, the end user has no idea where the file was generated and cannot directly retrieve it. 
The idea is to notify JQM of a file creation, so that JQM will take it (remove it from the work directory) and reference it. 
It is then be made available to clients through a small HTTP GET that is leveraged by the engine itself (and can be proxied).

The method to do so is :meth:`JobManager.addDeliverable` from the :doc:`engineapi`.

.. note:: Work/temp directories are obtained through :meth:`JobManager.getWorkDir`. These are purged after execution. Use of temporary Java 
    files is strongly discouraged - these are purged only on JVM exit, which on the whole never happens inside an application server.

Example::

    import java.io.FileWriter;
    import java.io.PrintWriter;

    public class App implements Runnable
    {
        private JobManager jm;
        
        @Override
        public void run()
        {
            String dir = jm.getWorkDir();
            String fileName = dir + "/temp.txt";
            try
            {
                PrintWriter out = new PrintWriter(fileName);
                out.println("Hello World!");
                out.close();
                addDeliverable(fileName, "ThisIsATag");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

.. _culling:

Going to the culling
**********************

Payloads are run inside a thread by the JQM engine. Alas, Java threads have one caveat: they cannot be cleanly killed. 
Therefore, there is no obvious way to allow a user to kill a job instance that has gone haywire. To provide some measure
of relief, the :doc:`engineapi` provides a method called :meth:`JobManager.yield` that, when called, will do nothing but give briefly control
of the job's thread to the engine. This allows the engine to check if the job should be killed (it throws an exception
as well as sets the thread's interruption status to do so). Now, if the job instance really has entered an infinite loop where 
yield is not called nor is the interruption status read, it won't help much. It is more to allow killing instances that 
run well (user has changed his mind, etc.).

To ease the use of the kill function, all other engine API methods actually call yield before doing their own work.

Finally, for voluntarily killing a running payload, it is possible to do much of the same: throwing a runtime exception.
Note that System.exit is forbidden by the Java security manager inside payloads - it would stop the whole JQM engine, which
would be rather impolite towards other running job instances.

Full example
*******************

This fully commented payload uses nearly all the API. ::

    import com.enioka.jqm.api.JobManager;

    public class App
    {
        // This will be injected by the JQM engine - it could be named anything
        private static JobManager jm;

        public static void main(String[] args)
        {
            System.out.println("main function of payload");

            // Using JQM variables
            System.out.println("run method of runnable payload with API");
            System.out.println("JobDefID: " + jm.jobApplicationId());
            System.out.println("Application: " + jm.application());
            System.out.println("JobName: " + jm.applicationName());
            System.out.println("Default JDBC: " + jm.defaultConnect());
            System.out.println("Keyword1: " + jm.keyword1());
            System.out.println("Keyword2: " + jm.keyword2());
            System.out.println("Keyword3: " + jm.keyword3());
            System.out.println("Module: " + jm.module());
            System.out.println("Session ID: " + jm.sessionID());
            System.out.println("Restart enabled: " + jm.canBeRestarted());
            System.out.println("JI ID: " + jm.jobInstanceID());
            System.out.println("Parent JI ID: " + jm.parentID());
            System.out.println("Nb of parameters: " + jm.parameters().size());

            // Sending info to the user
            jm.sendProgress(10);
            jm.sendMsg("houba hop");

            // Working with a temp directory
            File workDir = jm.getWorkDir();
            System.out.println("Work dir is " + workDir.getAbsolutePath());

            // Creating a file made available to the end user (PDF, XLS, ...)
            PrintWriter writer;
            File dest = new File(workDir, "marsu.txt");
            try
            {
                writer = new PrintWriter(dest, "UTF-8");
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                return;
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
                return;
            }
            writer.println("The first line");
            writer.println("The second line");
            writer.close();
            try
            {
                jm.addDeliverable(dest.getAbsolutePath(), "TEST");
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return;
            }

            // Using parameters & enqueue (both sync and async)
            if (jm.parameters().size() == 0)
            {
                jm.sendProgress(33);
                Map<String, String> prms = new HashMap<String, String>();
                prms.put("rr", "2nd run");
                System.out.println("creating a new async job instance request");
                int i = jm.enqueue(jm.applicationName(), null, null, null, jm.application(), jm.module(), null, null, null, prms);
                System.out.println("New request is number " + i);

                jm.sendProgress(66);
                prms.put("rrr", "3rd run");
                System.out.println("creating a new sync job instance request");
                jm.enqueueSync(jm.applicationName(), null, null, null, jm.application(), jm.module(), null, null, null, prms);
                System.out.println("New request is number " + i + " and should be done now");
                jm.sendProgress(100);
            }
        }
    }


Limitations
***************

Nearly all JSE Java code can run inside JQM, with the following limitations:

* no system.exit allowed - calling this will trigger a security exeption.
* ... This list will be updated when limits are discovered. For now this is it!

.. versionchanged:: 1.2.1
    JQM used to use a thread pool for running its job instances before version 1.2.1. This had the consequence of making thread local variables very dangerous
    to use. It does not any more - the performance gain was far too low to justify the impact.


Staying reasonable
***********************

JQM is some sort of light application server - therefore the same type of guidelines apply.

* Don't play (too much) with class loaders. Creating and swapping them is allowed because some frameworks require them (such as Hibernate)
  and we wouldn't want existing code using these frameworks to fail just because we are being too strict.
* Don't create threads. A thread is an unmanageable object in Java - if it blocks for whatever reason, the whole application server
  has to be restarted, impacting other jobs/users. They are only allowed for the same reason as for creating class loaders.
* Be wary of bootstrap static contexts. Using static elements is all-right as long as the static context is from your class loader (in our case, it means 
  classes from your own code or dependencies). Messing with
  static elements from the bootstrap class loader is opening the door to weird interactions between jobs running in parallel. For example, loading a JDBC
  driver does store such static elements, and should be frowned upon (use a shared JNDI JDBC resource for this).
* Don't redefine System.setOut and System.setErr - if you do so, you will loose the log created by JQM from your console output. See :doc:`logging`.
