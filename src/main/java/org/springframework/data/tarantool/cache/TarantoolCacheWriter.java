package org.springframework.data.tarantool.cache;

import org.springframework.lang.Nullable;

import java.time.Duration;

/**
 * {@link TarantoolCacheWriter} provides low level access to Tarantool space operations used for
 * caching.
 * The {@link TarantoolCacheWriter} may be shared by multiple cache implementations and is responsible for writing / reading
 * binary data to / from Tarantool.
 *
 * @author Alexander Rublev
 */
public interface TarantoolCacheWriter extends CacheStatisticsProvider {

    /**
     * Get value from Tarantool stored for the given key.
     *
     * @param name the cache name must not be {@literal null}.
     * @param key must not be {@literal null}.
     * @return {@literal null} if key does not exist.
     */
    @Nullable
    byte[] get(String name, byte[] key);

    /**
     * Write the given key/value pair to Tarantool and set the expiration time if defined.
     *
     * @param name  The cache name must not be {@literal null}.
     * @param key   The key for the cache entry. Must not be {@literal null}.
     * @param value The value stored for the key. Must not be {@literal null}.
     * @param ttl   Optional expiration time. Can be {@literal null}.
     */
    void put(String name, byte[] key, byte[] value, @Nullable Duration ttl);

    /**
     * Write the given value to Tarantool if the key does not already exist.
     *
     * @param name  The cache name must not be {@literal null}.
     * @param key   The key for the cache entry. Must not be {@literal null}.
     * @param value The value stored for the key. Must not be {@literal null}.
     * @param ttl   Optional expiration time. Can be {@literal null}.
     * @return {@literal null} if the value has been written, the value stored for the key if it already exists.
     */
    @Nullable
    byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl);

    /**
     * Remove the given key from Tarantool.
     *
     * @param name The cache name must not be {@literal null}.
     * @param key  The key for the cache entry. Must not be {@literal null}.
     */
    void remove(String name, byte[] key);

    /**
     * Remove all keys.
     */
    void clear(String name);

    /**
     * Reset all statistics counters and gauges for this cache.

     * @param name  The cache name must not be {@literal null}.
     */
    void clearStatistics(String name);

    /**
     * Obtain a {@link TarantoolCacheWriter} using the given {@link CacheStatisticsCollector} to collect metrics.
     *
     * @param cacheStatisticsCollector must not be {@literal null}.
     * @return new instance of {@link TarantoolCacheWriter}.
     */
    TarantoolCacheWriter withStatisticsCollector(CacheStatisticsCollector cacheStatisticsCollector);

}
