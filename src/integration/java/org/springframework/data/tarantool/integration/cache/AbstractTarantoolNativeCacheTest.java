package org.springframework.data.tarantool.integration.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.tarantool.cache.DefaultTarantoolNativeCache;
import org.springframework.data.tarantool.cache.TarantoolNativeCache;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.TestConfigProvider;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.tarantool.integration.config.TestConfigProvider.clientFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTarantoolNativeCacheTest {
    private TarantoolNativeCache cache;
    private TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client;

    public abstract TarantoolClientOptions getOptions();

    @BeforeAll
    void setUp() {
        client = clientFactory(getOptions()).createClient();

        cache = new DefaultTarantoolNativeCache("test", "integration", client,
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
        cache.put("test-key".getBytes(), "test-value".getBytes(), Duration.ofMillis(1000));

        byte[] cached = cache.get("test-key".getBytes());
        assertThat(cached).isNotNull();

        Integer count = client.callForSingleResult("box.space.integration_test:len", Integer.class).get();
        assertThat(count).isEqualTo(1);

        Thread.sleep(1000);

        byte[] expired = cache.get("test-key".getBytes());
        assertThat(expired).isNull();

        count = client.callForSingleResult("box.space.integration_test:len", Integer.class).get();
        assertThat(count).isEqualTo(0);
    }
}
