package org.springframework.data.tarantool.integration.cache;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.tarantool.cache.DefaultTarantoolNativeCache;
import org.springframework.data.tarantool.cache.TarantoolNativeCache;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.domain.Address;
import org.springframework.data.tarantool.integration.domain.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.tarantool.integration.config.TestConfigProvider.clientFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TarantoolNativeCacheTest {
    private TarantoolNativeCache cache;

    @BeforeAll
    void setUp() {
        cache = new DefaultTarantoolNativeCache("test",
                clientFactory(new SingleNodeTarantoolClientOptions()).createClient(),
                new MappingTarantoolConverter());
    }

    @Test
    void shouldPutAndGet() {
        cache.put("test-key".getBytes(), "test-value".getBytes(), null);

        byte[] cached = cache.get("test-key".getBytes());
        assertThat(new String(cached)).isEqualTo("test-value");
    }

}
