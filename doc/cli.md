# Command Line Interface (CLI)

Once a purely debug feature, JQM now offers a standard CLI for basic operations.

```bash
usage: java -jar jqm-engine.jar -createnode <nodeName> | -enqueue <applicationname> | -exportallqueues <xmlpath> | -h | -importjobdef <xmlpath> |
       -importqueuefile <xmlpath> | -startnode <nodeName> | -v
 -createnode <nodeName>       create a JQM node of this name (init the database if needed
 -enqueue <applicationname>   name of the application to launch
 -exportallqueues <xmlpath>   export all queue definitions into an XML file
 -h,--help                    display help
 -importjobdef <xmlpath>      path of the XML configuration file to import
 -importqueuefile <xmlpath>   import all queue definitions from an XML file
 -startnode <nodeName>        name of the JQM node to start
 -v,--version                 display JQM engine version
```