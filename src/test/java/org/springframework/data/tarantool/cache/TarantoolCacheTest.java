package org.springframework.data.tarantool.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TarantoolCacheTest {

    @Mock
    private TarantoolCacheWriter cacheWriter;

    @Mock
    private TarantoolCacheConfiguration cacheConfig;

    @Test
    void shouldCreateCacheWithCorrectName() {
        when(cacheConfig.getSerializer()).thenReturn(o -> new byte[0]);

        TarantoolCache cache1 = new TarantoolCache("cache1", cacheWriter, cacheConfig);
        assertThat(cache1.getName()).isEqualTo("cache1");

        when(cacheConfig.getCacheNamePrefix()).thenReturn("test");
        TarantoolCache cache2 = new TarantoolCache("cache2", cacheWriter, cacheConfig);
        assertThat(cache2.getName()).isEqualTo("test_cache2");
    }
}
