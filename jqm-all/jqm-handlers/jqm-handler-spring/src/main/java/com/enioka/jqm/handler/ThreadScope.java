// Adapted from org.springframework.context.support.SimpleThreadScope (Apache 2)

package com.enioka.jqm.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.NamedThreadLocal;

class ThreadScope implements Scope
{
    private final ThreadLocal<Map<String, Object>> threadScope = new NamedThreadLocal<Map<String, Object>>("SimpleThreadScope")
    {
        @Override
        protected Map<String, Object> initialValue()
        {
            return new HashMap<String, Object>();
        }
    };

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory)
    {
        Map<String, Object> scope = this.threadScope.get();
        Object object = scope.get(name);
        if (object == null)
        {
            object = objectFactory.getObject();
            scope.put(name, object);
        }
        return object;
    }

    @Override
    public Object remove(String name)
    {
        Map<String, Object> scope = this.threadScope.get();
        return scope.remove(name);
    }

    public void closeThread()
    {
        threadScope.remove();
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback)
    {
        // Nothing to do.
    }

    @Override
    public Object resolveContextualObject(String key)
    {
        return null;
    }

    @Override
    public String getConversationId()
    {
        return Thread.currentThread().getName();
    }
}
