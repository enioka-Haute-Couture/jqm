package com.enioka.jqm.tester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enioka.jqm.client.api.JqmClient;
import com.enioka.jqm.model.JobDefParameter;
import com.enioka.jqm.model.JobDef.PathType;
import com.enioka.jqm.tester.api.JqmAsynchronousTester;
import com.enioka.jqm.tester.api.TestJobDefinition;

/**
 * See {@link TestJobDefinition}
 */
class TestJobDefinitionImpl implements TestJobDefinition
{
    // Identity
    String description;
    String name;

    // Where the payload is
    String javaClassName;
    String path = "/dev/null";
    PathType pathType = PathType.MEMORY;

    // How it should run
    String queueName;
    boolean highlander = false;

    // Parameters
    Map<String, String> parameters = new HashMap<>();

    // Optional classifiers
    String application, module, keyword1, keyword2, keyword3;

    // Class loading options
    String specificIsolationContext;
    boolean childFirstClassLoader = false;
    List<String> hiddenJavaClasses = new ArrayList<>();
    boolean classLoaderTracing = false;

    // Internal
    JqmAsynchronousTesterOsgi tester;

    ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTION
    ///////////////////////////////////////////////////////////////////////////

    private TestJobDefinitionImpl(JqmAsynchronousTesterOsgi tester)
    {
        this.tester = tester;
    }

    /**
     * This creates a job definition from a given class. This class must be present inside the current class path, and will be loaded by the
     * engine with the current class loader. This means that most specific class loading option (child first, ...) are mostly useless in
     * this case.
     *
     * @param name
     *            the name to give the new job definition. This is the name that is later reused in client APIs, such as
     *            {@link JqmClient#enqueue(String, String)}.
     * @param description
     *            a free text describing the job definition
     * @param testedClass
     *            the class containing the job to run.
     * @return the object itself (fluid API)
     */
    public static TestJobDefinitionImpl createFromClassPath(String name, String description, Class<? extends Object> testedClass,
            JqmAsynchronousTesterOsgi tester)
    {
        TestJobDefinitionImpl res = new TestJobDefinitionImpl(tester);
        res.name = name;
        res.description = description;
        res.javaClassName = testedClass.getCanonicalName();
        res.path = "/dev/null";
        res.pathType = PathType.MEMORY;
        return res;
    }

    /**
     * Create a job definition from a class inside an existing jar file. This creates the exact replica of what happens inside a production
     * JQM cluster, including taking into account all the various class loading options (child first, external library directory...).
     *
     * @param name
     *            the name to give the new job definition. This is the name that is later reused in client APIs, such as
     *            {@link JqmClient#enqueue(String, String)}.
     * @param description
     *            a free text describing the job definition
     * @param testedClassCanonicalName
     *            the class containing the job to run (full canonical name, i.e. including package name)
     * @param jarPath
     *            path to the jar file, relative to the current directory.
     * @return
     */
    public static TestJobDefinitionImpl createFromJar(String name, String description, String testedClassCanonicalName, String jarPath,
            JqmAsynchronousTesterOsgi tester)
    {
        TestJobDefinitionImpl res = new TestJobDefinitionImpl(tester);
        res.name = name;
        res.description = description;
        res.javaClassName = testedClassCanonicalName;
        res.path = jarPath;
        res.pathType = PathType.FS;
        return res;
    }

    ///////////////////////////////////////////////////////////////////////////
    // FLUID API
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public JqmAsynchronousTester addJobDefinition()
    {
        tester.addJobDefinition(this);
        return tester;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Compulsory parameters

    String getDescription()
    {
        return description;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    String getName()
    {
        return name;
    }

    @Override
    public TestJobDefinition setName(String name)
    {
        if (name == null || name.isEmpty())
        {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        this.name = name;
        return this;
    }

    String getJavaClassName()
    {
        return javaClassName;
    }

    void setJavaClassName(String javaClassName)
    {
        this.javaClassName = javaClassName;
    }

    String getPath()
    {
        return path;
    }

    void setPath(String path)
    {
        this.path = path;
    }

    PathType getPathType()
    {
        return pathType;
    }

    void setPathType(PathType pathType)
    {
        this.pathType = pathType;
    }

    String getQueueName()
    {
        return queueName;
    }

    // end compulsory parameters
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public TestJobDefinition setQueueName(String queueName)
    {
        this.queueName = queueName;
        return this;
    }

    boolean isHighlander()
    {
        return highlander;
    }

    @Override
    public TestJobDefinition enableHighlander()
    {
        this.highlander = true;
        return this;
    }

    List<JobDefParameter> getParameters()
    {
        List<JobDefParameter> prms = new ArrayList<>(this.parameters.size());
        for (Map.Entry<String, String> e : this.parameters.entrySet())
        {
            JobDefParameter prm = new JobDefParameter();
            prm.setKey(e.getKey());
            prm.setValue(e.getValue());
            prms.add(prm);
        }
        return prms;
    }

    @Override
    public TestJobDefinition addParameter(String key, String value)
    {
        this.parameters.put(key, value);
        return this;
    }

    String getApplication()
    {
        return application;
    }

    @Override
    public TestJobDefinition setApplication(String application)
    {
        this.application = application;
        return this;
    }

    String getModule()
    {
        return module;
    }

    @Override
    public TestJobDefinition setModule(String module)
    {
        this.module = module;
        return this;
    }

    String getKeyword1()
    {
        return keyword1;
    }

    @Override
    public TestJobDefinition setKeyword1(String keyword1)
    {
        this.keyword1 = keyword1;
        return this;
    }

    String getKeyword2()
    {
        return keyword2;
    }

    @Override
    public TestJobDefinition setKeyword2(String keyword2)
    {
        this.keyword2 = keyword2;
        return this;
    }

    String getKeyword3()
    {
        return keyword3;
    }

    @Override
    public TestJobDefinition setKeyword3(String keyword3)
    {
        this.keyword3 = keyword3;
        return this;
    }

    String getSpecificIsolationContext()
    {
        return specificIsolationContext;
    }

    @Override
    public TestJobDefinition setSpecificIsolationContext(String specificIsolationContext)
    {
        this.specificIsolationContext = specificIsolationContext;
        return this;
    }

    boolean isChildFirstClassLoader()
    {
        return childFirstClassLoader;
    }

    @Override
    public TestJobDefinition setChildFirstClassLoader(boolean childFirstClassLoader)
    {
        this.childFirstClassLoader = childFirstClassLoader;
        return this;
    }

    String getHiddenJavaClasses()
    {
        String res = "";
        for (String s : hiddenJavaClasses)
        {
            res += s + ",";
        }
        if (hiddenJavaClasses.size() > 0)
        {
            return res.substring(0, res.length() - 2);
        }
        return "";
    }

    @Override
    public TestJobDefinition addHiddenJavaClasses(String canonicalClassName)
    {
        this.hiddenJavaClasses.add(canonicalClassName);
        return this;
    }

    boolean isClassLoaderTracing()
    {
        return classLoaderTracing;
    }

    @Override
    public TestJobDefinition enableClassLoaderTracing()
    {
        this.classLoaderTracing = true;
        return this;
    }
}
