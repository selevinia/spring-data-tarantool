package org.springframework.data.tarantool.integration.cache;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.tarantool.cache.DefaultTarantoolCacheWriter;
import org.springframework.data.tarantool.cache.TarantoolCacheWriter;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.TestConfigProvider;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.tarantool.integration.config.TestConfigProvider.clientFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTarantoolCacheWriterTest {
    private TarantoolCacheWriter cacheWriter;
    private TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> client;
    private static final String name = "integration_test";

    public abstract TarantoolClientOptions getOptions();

    @BeforeAll
    void setUp() {
        client = clientFactory(getOptions()).createClient();

        cacheWriter = new DefaultTarantoolCacheWriter(client, TestConfigProvider.converter(TestConfigProvider.mappingContext()));
        cacheWriter.clear(name);
    }

    @Test
    void shouldPutAndGet() {
        cacheWriter.put(name, "test-key".getBytes(), "test-value-one".getBytes(), null);

        byte[] cached = cacheWriter.get(name, "test-key".getBytes());
        assertThat(cached).isNotNull();
        assertThat(new String(cached)).isEqualTo("test-value-one");

        cacheWriter.put(name, "test-key".getBytes(), "test-value-two".getBytes(), null);

        byte[] updated = cacheWriter.get(name, "test-key".getBytes());
        assertThat(updated).isNotNull();
        assertThat(new String(updated)).isEqualTo("test-value-two");
    }

    @Test
    void shouldPutAndGetMessageValue() {
        SerializingConverter serializer = new SerializingConverter();
        DeserializingConverter deserializer = new DeserializingConverter();

        Message messageOne = new Message("one", "test-value-one");

        cacheWriter.put(name, "test-key".getBytes(), Objects.requireNonNull(serializer.convert(messageOne)), null);

        byte[] cached = cacheWriter.get(name, "test-key".getBytes());
        assertThat(cached).isNotNull();
        assertThat(deserializer.convert(cached)).isEqualTo(messageOne);

        Message messageTwo = new Message("two", "test-value-two");

        cacheWriter.put(name, "test-key".getBytes(), Objects.requireNonNull(serializer.convert(messageTwo)), null);

        byte[] updated = cacheWriter.get(name, "test-key".getBytes());
        assertThat(updated).isNotNull();
        assertThat(deserializer.convert(updated)).isEqualTo(messageTwo);
    }

    @Test
    void shouldPutAndRemove() {
        cacheWriter.put(name, "test-key".getBytes(), "test-value".getBytes(), null);

        byte[] cached = cacheWriter.get(name, "test-key".getBytes());
        assertThat(cached).isNotNull();
        assertThat(new String(cached)).isEqualTo("test-value");

        cacheWriter.remove(name, "test-key".getBytes());

        byte[] removed = cacheWriter.get(name, "test-key".getBytes());
        assertThat(removed).isNull();
    }

    @Test
    void shouldPutIfAbsent() {
        cacheWriter.putIfAbsent(name, "test-key".getBytes(), "test-value-one".getBytes(), null);

        byte[] cached = cacheWriter.get(name, "test-key".getBytes());
        assertThat(cached).isNotNull();
        assertThat(new String(cached)).isEqualTo("test-value-one");

        cacheWriter.putIfAbsent(name, "test-key".getBytes(), "test-value-two".getBytes(), null);

        byte[] updated = cacheWriter.get(name, "test-key".getBytes());
        assertThat(updated).isNotNull();
        assertThat(new String(updated)).isEqualTo("test-value-one");
    }

    @Test
    void shouldGetEmpty() {
        byte[] cached = cacheWriter.get(name, "test-empty-key".getBytes());
        assertThat(cached).isNull();
    }

    @Test
    @SneakyThrows
    void shouldGetEmptyWhenExpired() {
        cacheWriter.put(name, "test-key".getBytes(), "test-value".getBytes(), Duration.ofMillis(1000));

        byte[] cached = cacheWriter.get(name, "test-key".getBytes());
        assertThat(cached).isNotNull();

        Integer count = client.callForSingleResult(String.format("box.space.%s:len", name), Integer.class).get();
        assertThat(count).isEqualTo(1);

        Thread.sleep(1000);

        byte[] expired = cacheWriter.get(name, "test-key".getBytes());
        assertThat(expired).isNull();

        count = client.callForSingleResult(String.format("box.space.%s:len", name), Integer.class).get();
        assertThat(count).isEqualTo(0);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message implements Serializable {
        private String id;
        private String text;
    }
}
