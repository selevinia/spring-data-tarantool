package org.springframework.data.tarantool.cache;

import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;

/**
 * {@link org.springframework.cache.Cache} implementation using for Tarantool as underlying store.
 * Use {@link TarantoolCacheManager} to create {@link TarantoolCache} instances.
 *
 * @author Tatiana Blinova
 */
public class TarantoolCache extends AbstractValueAdaptingCache {

    private final String name;
    private final TarantoolCacheWriter cacheWriter;
    private final TarantoolCacheConfiguration cacheConfig;

    public TarantoolCache(String name, TarantoolCacheWriter cacheWriter, TarantoolCacheConfiguration cacheConfig) {
        super(cacheConfig.getAllowCacheNullValues());

        Assert.notNull(name, "Name must not be null");
        Assert.notNull(cacheWriter, "CacheWriter must not be null");
        Assert.notNull(cacheConfig, "CacheConfig must not be null");

        this.name = name;
        this.cacheWriter = cacheWriter;
        this.cacheConfig = cacheConfig;
    }

    @Override
    protected Object lookup(Object key) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return super.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public boolean evictIfPresent(Object key) {
        return super.evictIfPresent(key);
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean invalidate() {
        return super.invalidate();
    }
}
