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

    private TarantoolCacheConfiguration(Duration ttl, boolean cacheNullValues,
                                        Converter<Object, byte[]> serializer, Converter<byte[], Object> deserializer) {
        Assert.notNull(ttl, "TTL duration must not be null");

        this.ttl = ttl;
        this.cacheNullValues = cacheNullValues;
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    public static TarantoolCacheConfiguration defaultCacheConfig() {
        return new TarantoolCacheConfiguration(Duration.ZERO, true, new SerializingConverter(), new DeserializingConverter());
    }

    public TarantoolCacheConfiguration entryTtl(Duration ttl) {
        Assert.notNull(ttl, "TTL duration must not be null");

        return new TarantoolCacheConfiguration(ttl, cacheNullValues, serializer, deserializer);
    }

    public TarantoolCacheConfiguration disableCachingNullValues() {
        return new TarantoolCacheConfiguration(ttl, false, serializer, deserializer);
    }

    public TarantoolCacheConfiguration serializeWith(Converter<Object, byte[]> serializer) {
        Assert.notNull(serializer, "Converter for serializing must not be null");

        return new TarantoolCacheConfiguration(ttl, cacheNullValues, serializer, deserializer);
    }

    public TarantoolCacheConfiguration deserializeWith(Converter<byte[], Object> deserializer) {
        Assert.notNull(serializer, "Converter for deserializing must not be null");

        return new TarantoolCacheConfiguration(ttl, cacheNullValues, serializer, deserializer);
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
