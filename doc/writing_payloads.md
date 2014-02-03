# Writing Payloads

The goal of JQM is to run some Java code containing some business logic. This chapter describes how to write the Java code 
that will be really be executed. The philosophy is: JQM is able to run most existing code without adaptations, and taking advantage 
of some JQM functionalities is easy to add to any code be it new or existing.

## Payloads types

There are three payload types

### Main

This a classic class containing a "static void main" function (with or without a single parameter of type String[]).

In that case, JQM will simply launch the main function. If there are some arguments defined (default arguments in the JobDef or
arguments given at enqueue time) their value will be put inside the String[] parameter *ordered by key name*.

There is no need for any dependencies in that case - direct reuse of existing code is possible.

This would run perfectly, without any specific dependencies or imports:
```java
public class App
{
    public static void main(String[] args)
    {
        System.out.println("main function of payload");
    }
}
```

> :grey_exclamation: It is not necessary to make your jar executable. The jar manifest is ignored by JQM.

### Runnable

Some existing code is written to be run as a thread, implementing the Runnable interface. If these classes have a no-argument
constructor (this is not imposed by the Runnable interface as interfaces cannot impose a constructor), JQM can instanciate 
and launch them . In that case, the run() method from the interface is executed - therefore it is not possible to access
parameters without using JQM advanced function described later in this chapter.

This would run perfectly, without any specific dependencies or imports:
```java
public class App implements Runnable
{
    @Override
    public void run()
    {
        System.out.println("run method of runnable payload");
    }
}
```

### Explicit JQM job

This type of job only exists for ascending compatibility with a former limited JQM version. It consisted in subclassing class JobBase,
 overloading method start() and keeping a no-arg constructor. Parameters are accessible through a number of accessors of the base class.

For example (note the import and the use of an accessor):
```java
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
```

It requires the following dependency (Maven):
```XML
<dependency>
	<groupId>com.enioka.jqm</groupId>
	<artifactId>jqm-api</artifactId>
	<version>${jqm.version}</version>
</dependency>
```

## Accessing the JQM engine API

Often, a job will need to interact with JQM, for operations such as:

* enqueue a new job
* get the different IDs that identify a job instance (i.e. a run)
* get a resource (see [getting resources](resources.md))
* get the optional data that was given at enqueue time
* report progress to an end user
* ...

For this, an interface exists called JobManager inside jar jqm-api.jar. Using it is trivial: 
just create a field (static or not) inside your job class (whatever type - Main, Runnable or JQM) and the engine 
will **inject an implementation ready for use**.

> :grey_exclamation: the JQM payload type already has one JobManager field named jm defined in the base class JobBase - it would have
been stupid not to define it while the API is always present for that payload type. 

## Creating files

An important use case for JQM is the generation of reports at the direct request of an end-user through a web interface.
This report is too long to generate on the application server (timeout), or blocking a thread for a user
is unacceptable: the generation must be deported elsewhere. JQM has methods to do that.

The report generation is the payload - but how should the file be sent to the end user? JQM is a distributed system, so
unless it is forced in a single node deployment, the end user has no idea where the file was generated. (and it is definitely not
on the application server, so not easy to access from the web interface). The idea is to notify JQM of a file creation, so that
JQM will take it (remove it from the work directory) and reference it. It is then be made available to clients through a small
HTTP GET that is leveraged by the client API (the API used by the application server).

TL;DR: when a file is created that should be accessible to remote clients, use JobManager.addDeliverable

> work directories are obtained through JobManager.getWorkDir. These are purged after execution.

Exemple:
```java
import java.io.FileWriter;
import java.io.PrintWriter;
import com.enioka.jqm.api.JobBase;

public class App extends JobBase
{
    @Override
    public void start()
    {
        String file = this.getParameters().get("filepath");
        String fileName = this.getParameters().get("fileName");
        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(file + fileName));
            out.println("Hello World!");
            out.close();
            addDeliverable(file + fileName, "JobGenADeliverableFamily");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
```

## Going to the culling

Payloads are run inside a thread by the JQM engine. Alas, Java threads have one caveat: they cannot be cleanly killed. 
Therefore, there is no obvious way to allow a user to kill a job instance that has gone haywire. To provide some measure
of relief, the engine API provides a method called *yield* that, when called, will do nothing but give briefly control
of the job's thread to the engine. This allows the engine to check if the job should be killed (it throws an exception to 
do so). Now, if the job instance really has entered an infinete loop where yield is not called, it won't help much. It is more
to allow killing instances that run well (user has changed his mind, etc.).

To ease the use of the kill function, all other engine API methods actually call yield before doing their own work.

Finally, for voluntarily killing a running payload, it is possible to do much of the same: throwing a runtime exception.
Note that System.exit is forbidden by the Java security manager inside paylaods - it would stop the whole JQM engine, which
would be rather impolite towards other running job instances.

## Full exemple

This fully commented payload uses nearly all the API.
```java
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
```

## Limitations

Nearly all JSE Java code can run inside JQM, with the following limitations:

* no system.exit allowed - calling this will trigger a security exeption.
* ... This list will be updated when limits are discovered. For now this is it!

## Staying reasonable

JQM is some sort of light application server - therefore the same guidelines apply.

* Don't play (too much) with classloaders. This is allowed because some frameworks require them (such as Hibernate)
and we wouldn't want existing code using these frameworks to fail just because we are being too strict.
* Don't create threads. A thread is an unmanageable object in Java - if it blocks for whatever reason, the whole pplication server
has to be restarted, impacting other jobs/users. They are only allowed for the same reason as for creating classloaders.