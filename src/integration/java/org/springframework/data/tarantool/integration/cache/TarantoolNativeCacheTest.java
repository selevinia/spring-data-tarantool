package org.springframework.data.tarantool.integration.cache;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.tarantool.cache.DefaultTarantoolNativeCache;
import org.springframework.data.tarantool.cache.TarantoolNativeCache;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.TestConfigProvider;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.tarantool.integration.config.TestConfigProvider.clientFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TarantoolNativeCacheTest {
    private TarantoolNativeCache cache;

    @BeforeAll
    void setUp() {
        cache = new DefaultTarantoolNativeCache("test",
                clientFactory(new SingleNodeTarantoolClientOptions()).createClient(),
                TestConfigProvider.converter(TestConfigProvider.mappingContext()));
        cache.remove();
    }

    @Test
    void shouldPutAndGet() {
        cache.put("test-key".getBytes(), "test-value-one".getBytes(), null);

        byte[] cached = cache.get("test-key".getBytes());
        assertThat(cached).isNotNull();
        assertThat(new String(cached)).isEqualTo("test-value-one");

        cache.put("test-key".getBytes(), "test-value-two".getBytes(), null);

        byte[] updated = cache.get("test-key".getBytes());
        assertThat(updated).isNotNull();
        assertThat(new String(updated)).isEqualTo("test-value-two");
    }

    @Test
    void shouldPutAndRemove() {
        cache.put("test-key".getBytes(), "test-value".getBytes(), null);

        byte[] cached = cache.get("test-key".getBytes());
        assertThat(cached).isNotNull();
        assertThat(new String(cached)).isEqualTo("test-value");

        cache.remove("test-key".getBytes());

        byte[] removed = cache.get("test-key".getBytes());
        assertThat(removed).isNull();
    }

    @Test
    void shouldPutIfAbsent() {
        cache.putIfAbsent("test-key".getBytes(), "test-value-one".getBytes(), null);

        byte[] cached = cache.get("test-key".getBytes());
        assertThat(cached).isNotNull();
        assertThat(new String(cached)).isEqualTo("test-value-one");

        cache.putIfAbsent("test-key".getBytes(), "test-value-two".getBytes(), null);

        byte[] updated = cache.get("test-key".getBytes());
        assertThat(updated).isNotNull();
        assertThat(new String(updated)).isEqualTo("test-value-one");
    }

    @Test
    void shouldGetEmpty() {
        byte[] cached = cache.get("test-empty-key".getBytes());
        assertThat(cached).isNull();
    }

    @Test
    @SneakyThrows
    void shouldGetEmptyWhenExpired() {
        cache.put("test-key".getBytes(), "test-value".getBytes(), Duration.ofMillis(500));

        Thread.sleep(500);

        byte[] cached = cache.get("test-key".getBytes());
        assertThat(cached).isNull();
    }
}
