 Java 6
#######

To build with a Java 6 SDK :

Add Google central mirror (as central does not support TLS 1.1 anymore).

```
    <mirrors>
        <mirror>
            <id>google-maven-central</id>
            <name>GCS Maven Central mirror EU</name>
            <url>https://maven-central.storage.googleapis.com/maven2</url>
            <mirrorOf>central</mirrorOf>
        </mirror>
    </mirrors>
```

Use maven 3.2.5, last version to support JDK 1.6.

Use Zulu 1.6 SDK (no more Oracle support).

Maven 3.2.5 seems to be lost on Windows - settings file must be sepcified like this: `mvn -gs C:/Users/marsu/.m2/settings.xml install -DskipTests`

Note that JS build is disabled with JDK6. Use your own nodejs/npm.

Also : do not do it.
