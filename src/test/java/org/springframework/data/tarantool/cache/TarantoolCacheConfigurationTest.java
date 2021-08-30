package org.springframework.data.tarantool.cache;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class TarantoolCacheConfigurationTest {

    @Test
    void shouldAllowConfigurationCustomizations() {
        TarantoolCacheConfiguration config = TarantoolCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(10))
                .disableCachingNullValues()
                .prefixCacheNameWith("test")
                .serializeWith(new ObjectSerializer())
                .deserializeWith(new ObjectDeserializer());

        assertThat(config.getTtl()).isEqualTo(Duration.ofSeconds(10));
        assertThat(config.getAllowCacheNullValues()).isFalse();
        assertThat(config.getCacheNamePrefix()).isEqualTo("test");
        assertThat(config.getSerializer()).isInstanceOf(ObjectSerializer.class);
        assertThat(config.getDeserializer()).isInstanceOf(ObjectDeserializer.class);
    }

    private static class ObjectSerializer implements Converter<Object, byte[]> {

        @Nullable
        @Override
        public byte[] convert(Object source) {
            return null;
        }
    }

   private static class ObjectDeserializer implements Converter<byte[], Object> {

        @Nullable
        @Override
        public Object convert(byte[] source) {
            return null;
        }
    }

}
