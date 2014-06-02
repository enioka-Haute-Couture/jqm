Administration web services
##################################

The web console is actually only an HTML5 client built on top of some generic administration web services - it has no priviledged access
to any resources that could not be accessed to in any other ways. 

These services are REST-style services and are a standard JQM API. It is deployed along with the client web services and the console (in the same war file).

They also offer the different verbs:

For container URLs:

* GET = obtain an instance (on /container/id URL) or a list of instances (on /container URL)
* POST = add an instance or update it if there is already an instance with the same ID (null ID means create an object)
* PUT = replace the whole collection. Objects that are not in the POST part of the request are dropped, other are created or updated (null ID means create an object)

For element URLs:

* GET = get the instance
* PUT = create or update the instance (null ID means create an object)
* DELETE = removes the object for ever

.. note:: the API never returns anything on POST/PUT/DELETE operations. On GET, it will output JSON (application/json). By setting the "accept" header in the request, it is also possible to obtain application/xml.

+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| URL                   | GET | POST | PUT | DELETE | Description                                                                                                                                           |
+=======================+=====+======+=====+========+=======================================================================================================================================================+ 
| /q                    | X   | X    | X   |        | Container for queues. Example of return JSON for a GET::                                                                                              |
|                       |     |      |     |        |                                                                                                                                                       |
|                       |     |      |     |        |     [{"defaultQueue":true,"description":"default queue","id":2,"name":"DEFAULT"},{"defaultQueue":false,"description":"meuh","id":3,"name":"MEUH"}]    |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /q/{id}               | X   |      | X   | X      | A queue. For creating one, you may PUT this (note the absence of ID)::                                                                                |
|                       |     |      |     |        |                                                                                                                                                       |
|                       |     |      |     |        |     {"defaultQueue":false,"description":"my new queue","name":"SUPERQUEUE"}                                                                           |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /qmapping             | X   | X    | X   |        | Container for the deployments of queues on nodes. nodeName & queueName cannot be set - they are only GUI helpers. Example of return JSON for a GET::  |
|                       |     |      |     |        |                                                                                                                                                       |
|                       |     |      |     |        |     [{"id":9,"nbThread":5,"nodeId":1,"nodeName":"JEUX","pollingInterval":1000,"queueId":2,"queueName":"DEFAULT"}                                      |
|                       |     |      |     |        |      {"id":154,"nbThread":10,"nodeId":1,"nodeName":"JEUX","pollingInterval":60000,"queueId":3,"queueName":"MEUH"}]                                    |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /qmapping/{id}        | X   |      | X   | X      | The deployment of a queue on a node. For creating one, you may PUT this (note the absence of ID. nodeName, queueName would be ignored if set)::       |
|                       |     |      |     |        |                                                                                                                                                       |
|                       |     |      |     |        |     {"nbThread":5,"nodeId":1,"pollingInterval":1000,"queueId":2}                                                                                      |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /jndi                 | X   | X    | X   |        | Container for resource definitions with JNDI aliases. Example of return JSON for a GET::                                                              |
|                       |     |      |     |        |                                                                                                                                                       |
|                       |     |      |     |        |     [{"parameters":[{"id":184,"key":"PATH","value":"C:/TEMP/HOUBA"}],"auth":"CONTAINER","description":"file or directory",                            |
|                       |     |      |     |        |     "factory":"com.enioka.jqm.providers.FileFactory","id":183,"name":"fs/filename","singleton":false,"type":"java.io.File.File"}]                     |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /jndi/{id}            | X   |      | X   | X      | Resource definitions with JNDI alias                                                                                                                  |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /prm                  | X   | X    | X   |        | Cluster parameters container. Example of return JSON for a GET::                                                                                      |
|                       |     |      |     |        |                                                                                                                                                       |
|                       |     |      |     |        |     [{"id":3,"key":"mavenRepo","value":"http://repo1.maven.org/maven2/"},{"id":4,"key":"defaultConnection","value":"jdbc/jqm"},{"id":5,               |
|                       |     |      |     |        |     "key":"deadline","value":"10"},{"id":6,"key":"logFilePerLaunch","value":"true"},{"id":7,"key":"internalPollingPeriodMs","value":"10000"},         |
|                       |     |      |     |        |     {"id":8,"key":"aliveSignalMs","value":"60000"},{"id":187,"key":"name","value":"enter value"}]                                                     |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /prm/{id}             | X   |      | X   | X      | Cluster parameter                                                                                                                                     |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /node                 | X   |      |     |        | Cluster nodes (JQM engines)   ::                                                                                                                      |
|                       |     |      |     |        |                                                                                                                                                       |
|                       |     |      |     |        |     [{"dns":"localhost","id":1,"jmxRegistryPort":0,"jmxServerPort":0,"jobRepoDirectory":"C:\\TEMP\\jqm-1.1.4-SNAPSHOT/jobs/","name":"JEUX",           |
|                       |     |      |     |        |     "outputDirectory":"C:\\TEMP\\jqm-1.1.4-SNAPSHOT/outputfiles/","port":63821,"rootLogLevel":"INFO"}]                                                |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /node/{id}            | X   |      |     |        | A cluster node (a JQM engine). Creation should be done by running the createnode command line at service setup.                                       |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /jd                   | X   |  X   | X   |        | Job Defintions.  Sample GET result::                                                                                                                  |
|                       |     |      |     |        |                                                                                                                                                       |
|                       |     |      |     |        |     [{"type":"jobDefDto","parameters":[{"id":11,"key":"p1","value":"1"},{"id":12,"key":"p2","value":"2"}],"application":"JQM",                        |
|                       |     |      |     |        |     "applicationName":"DemoFibo1","canBeRestarted":true,"description":"Demonstrates the use of parameters and engine API (computes the Fibonacci      |
|                       |     |      |     |        |     suite).","highlander":false,"id":10,"jarPath":"jqm-test-fibo/jqm-test-fibo.jar","javaClassName":"com.enioka.jqm.tests.App","keyword1":            |
|                       |     |      |     |        |     "EngineAPI","keyword2":"parameters","keyword3":"","module":"Demos","queueId":2}  ]                                                                |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| /jd/{id}              | X   |      | X   | X      |                                                                                                                                                       |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /user                 | X   |  X   | X   |        | User (as in REST services/GUI user                                                                                                                    |
|                       |     |      |     |        |                                                                                                                                                       |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| /user/{id}            | X   |      | X   | X      |                                                                                                                                                       |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+
| /role                 | X   |  X   | X   |        | User roles                                                                                                                                            |
|                       |     |      |     |        |                                                                                                                                                       |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| /role/{id}            | X   |      | X   | X      |                                                                                                                                                       |
+-----------------------+-----+------+-----+--------+-------------------------------------------------------------------------------------------------------------------------------------------------------+

.. note:: queues and job definitions are also available through the client API. However, the client version is different with less data exposed and no
  possibility to update anything.