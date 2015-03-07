package com.enioka.jqm.webui.shiro;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

public class UserCacheManager extends AbstractCacheManager
{
    @SuppressWarnings("rawtypes")
    @Override
    protected Cache createCache(String name) throws CacheException
    {
        return new UserCache<Object, Object>(name);
    }
}
