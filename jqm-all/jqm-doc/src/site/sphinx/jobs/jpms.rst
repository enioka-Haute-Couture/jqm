JPMS
##############

.. highlight:: java

This is an advanced topic that most users will never need.

JPMS & payloads
***********************

JPMS (Java Platform Module System, also called Jigsaw or JSR 376 or JEP 261) is the Java modularity framework embedded in the JVM.
While it is rarely seen in the wild it has its use cases.
As JQM goal is to run payloads "as they would be run with java -jar", it also supports JPMS payloads.

Most of the time a JPMS module can be loaded in a normal class loader and work perfectly.
In that case, there is nothing to do - JQM will work in normal "legacy" class loading mode.
This is strictly equivalent to using `java -jar my-module.jar My.Class` - the JPMS system actually
does nothing in this case, without any accessibility check etc.
And let's be honest, this is how most Java programs run today.

However there is an edge case where it is not possible to do so: when a program actively uses
JPMS functionalities, especially `ServiceLoader` to load services with said services only declared
inside the JPMS `module-info.class` module descriptor. In that case, if we load the module as a
normal jar inside a classic ClassLoader, no services will be found by `ServiceLoader` (as in legacy
mode, `ServiceLoader` looks for service descriptors in XML format, not for module descriptors).

Therefore, we must tell JQM to use JPMS mode.
This is done by specifying the payload class name in the new JPMS module qualified format:
instead of specifying `MyPackage.MyClass` you must specify `my.module.name/MyPackage.MyClass`.
In this case JQM will run the class exactly as if it had been run with `java --module-path my-module.jar --module my.module.name/MyPackage.MyClass`.

When JPMS mode is enabled, all the dependencies are loaded inside a new `ModuleLayer`
(which is associated to a dedicated ClassLoader containing exactly the same classes as in non-JPMS mode).
Standard JPMS accessibility rules apply. Since JQM itself is inside the unnamed module,
it has no privileged access to the payload which is inside the new module layer.

Two consequences:

* The payload itself must be accessible to JQM. In other words, the package containing the payload must be exported by the payload module.
  * It is actually natural to export the package: the payload class is by nature a public API
  * As for any other cases (and not only in JPMS) the payload still needs a public no args constructor.
* When injecting a `JobManager` instance, JQM cannot access private fields in a module that does not
  explicitly open its packages to reflection. So either the payload package must be opened
  (to allow JQM to do reflection on the private field), or the `JobManager` field must be public
  (recommended - after all this is a public API).
  * In classic mode, JQM can make private fields public by itself so this was not an issue


JPMS & client libraries
*******************************

The JQM payload API library is itself a JPMS library.
Module name is `com.enioka.jqm.payload.api`.

Therefore a payload module-info.java file may look like (when using the optional JQM payload API)::

    module com.enioka.jqm.tests.jpms
    {
        requires com.enioka.jqm.payload.api;

        exports com.enioka.jqm.tests.jpms;
    }
