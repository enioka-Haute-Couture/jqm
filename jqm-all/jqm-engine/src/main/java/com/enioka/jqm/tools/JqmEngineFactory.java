package com.enioka.jqm.tools;

import com.enioka.jqm.jdbc.Db;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * The only public way to create an engine. It is exposed to allow different engine packagings in other JQM artifacts (and one day may
 * become the entry point of an "embedded JQM library").
 *
 */
public class JqmEngineFactory
{
    /**
     * Creates and start an engine representing the node named as the given parameter.
     * 
     * @param name
     *            name of the node, as present in the configuration (case sensitive)
     * @param handler
     *            can be null. A set of callbacks hooked on different engine life cycle events.
     * @return an object allowing to stop the engine.
     */
    public static JqmEngineOperations startEngine(String name, JqmEngineHandler handler)
    {
        JqmEngine e = new JqmEngine();
        e.start(name, handler);
        return e;
    }

    /**
     * If you already have a Db object pointing to the JQM database to use (an existing datasource) you can give it here and all the engines
     * will use it.
     * 
     * @param db
     *            the encapsulated datasource.
     */
    public static void setDatasource(Db db)
    {
        Helpers.setDb(db);
    }
}
