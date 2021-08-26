package org.springframework.data.tarantool.cache;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * Immutable {@link TarantoolCacheConfiguration} helps customize {@link TarantoolCache} behaviour.
 * Start with {@link TarantoolCacheConfiguration#defaultCacheConfig()} and customize {@link TarantoolCache} behaviour from there on.
 *
 * @author Tatiana Blinova
 */
public class TarantoolCacheConfiguration {

    private final Duration ttl;
    private final boolean cacheNullValues;
    private final Converter<Object, byte[]> serializer;
    private final Converter<byte[], Object> deserializer;

    private TarantoolCacheConfiguration(Duration ttl, boolean cacheNullValues) {
        Assert.notNull(ttl, "TTL duration must not be null");

        this.ttl = ttl;
        this.cacheNullValues = cacheNullValues;
        this.serializer = new SerializingConverter();
        this.deserializer = new DeserializingConverter();
    }

    public static TarantoolCacheConfiguration defaultCacheConfig() {
        return new TarantoolCacheConfiguration(Duration.ZERO, true);
    }

    public TarantoolCacheConfiguration entryTtl(Duration ttl) {
        Assert.notNull(ttl, "TTL duration must not be null");

        return new TarantoolCacheConfiguration(ttl, cacheNullValues);
    }

    public TarantoolCacheConfiguration disableCachingNullValues() {
        return new TarantoolCacheConfiguration(ttl, false);
    }

    public Duration getTtl() {
        return ttl;
    }

    public boolean getAllowCacheNullValues() {
        return cacheNullValues;
    }

    public Converter<Object, byte[]> getSerializer() {
        return serializer;
    }

    public Converter<byte[], Object> getDeserializer() {
        return deserializer;
    }
}
