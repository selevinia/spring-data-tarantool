package org.springframework.data.tarantool.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.lang.Nullable;

import java.time.Duration;

/**
 * {@link TarantoolCacheWriter} provides low level access to Tarantool space operations used for
 * caching.
 * The {@link TarantoolCacheWriter} may be shared by multiple cache implementations and is responsible for writing / reading
 * data to / from Tarantool.
 *
 * @author Tatiana Blinova
 */
public interface TarantoolCacheWriter {

    TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> getTarantoolClient();

    /**
     * Write the given key/value pair to Tarantool and set the expiration time if defined.
     *
     * @param name  The cache name must not be {@literal null}.
     * @param key   The key for the cache entry. Must not be {@literal null}.
     * @param value The value stored for the key. Must not be {@literal null}.
     * @param ttl   Optional expiration time. Can be {@literal null}.
     */
    void put(String name, Object key, Object value, @Nullable Duration ttl);

    /**
     * Get value from Tarantool stored for the given key.
     *
     * @param name must not be {@literal null}.
     * @param key  must not be {@literal null}.
     * @return {@literal null} if key does not exist.
     */
    @Nullable
    Object get(String name, Object key);

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
    Object putIfAbsent(String name, Object key, Object value, @Nullable Duration ttl);

    /**
     * Remove the given key from Tarantool.
     *
     * @param name The cache name must not be {@literal null}.
     * @param key  The key for the cache entry. Must not be {@literal null}.
     */
    void remove(String name, Object key);

    /**
     * Remove all keys.
     *
     * @param name The cache name must not be {@literal null}.
     */
    void remove(String name);
}
