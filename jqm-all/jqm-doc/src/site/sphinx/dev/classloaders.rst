Classloading
################

JQM obeys a very simple classloading architecture, respecting the design goal of simplicity and robustness (to the expense of PermGen space).

The engine classloader stack is as follows (bottom of the stack is at the bottom of the table):

+----------------------------------------------------------+------------------------------------------------------------------------------+
| JNDI class loader (JQM provided - type URLClassloader)   | Payload class loader (JQM provided - type JarClassLoader).                   |
| Loads everything inside JQM_ROOT/ext                     | Loads the libs of payloads from .m2 or from the payload's "lib" directory    |
+----------------------------------------------------------+------------------------------------------------------------------------------+
| System class loader (JVM provided - type AppClassLoader)                                                                                |
+----------------------------------------------------------+------------------------------------------------------------------------------+
| Extension class loader (JVM provided - no need in JQM)                                                                                  |
+----------------------------------------------------------+------------------------------------------------------------------------------+
| Bootstrap class loader (JVM provided)                                                                                                   |
+----------------------------------------------------------+------------------------------------------------------------------------------+

		
The general idea is:

* The engine uses the classic JVM-provided AppClassLoader for everything concerning its internal business
* Every payload launch has its own classloader, **totally independent from the engine classloader** (it is created with a null parent - which means 
  its parent is the bootstrap classloader). This classloader is garbage collected at the end of the run.
* JNDI resources in singleton mode (see :doc:`../jobs/resources`) must be loaded by th engine from the jars inside JQM_ROOT/ext. 
  This is impossible to do from the AppClassLoader (its class path is fixed once and for all - one cannot add elements to it), so an URLClassLoader
  is used.

  
Advantages:

* The engine is totally transparent to payloads, as the engine libraries are inside a classloader which is not accessible to payloads.
* It allows to have multiple incompatible versions of the same library running simultaneously in different payloads.
* It still allows for the exposition to the payload of an API implemented inside the engine through the use of a proxy class, a 
  pattern designed explicitely for that use case.
* Easily allows for hot swap of libs and paylaods.
* Avoids having to administer classloader hierarchies and keeps paylaods independant from one another.

Cons:

* It is costly in terms of PermGen: if multiple payloads use the same library, it will be loaded once per payload, which is a waste of memory.
* In case the payload does something stupid which prevents the garbage collection of at least one of its objects, the classloader will not be able
  to be garbage collected. This a huge memory leak (usually called a classloader leak). The *one known example*: registering a JDBC driver
  inside the static bootstrap-loaded DriverManager. This keeps a reference to the payload-context driver inside the bootstrap-context, and prevents
  collection. This special case is the reason why singleton mode should always be used for JDBC resources.
* There is a bug inside the Sun JVM 6: even if garbage collected, a classloader will leave behind an open file descriptor. This will effectively 
  prevent hot swap of libs on Windows.


All in all, this solution is not perfect (the classloader leak is a permanent threat) but has so many benefits in terms of simplicity that
it was chosen. This way, there is no need to wonder if a payload can run alongside another - the answer is always yes. There is no need
to deal with libraries - they are either in libs ir in ext, and it just works. The engine is invisible - payloads can consider it as a pure JVM,
so no specific development is required.
There result is also robust, as payloads have virtually no access to the engine and can't set it off tracks.
