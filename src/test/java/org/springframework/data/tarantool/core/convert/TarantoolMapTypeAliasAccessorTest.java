package org.springframework.data.tarantool.core.convert;

import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.Alias;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TarantoolMapTypeAliasAccessorTest {

    @Test
    void shouldWriteAndReadAlias() {
        String testTypeKey = "test_type_key";
        String testTypeValue = "test_type_value";

        TarantoolMapTypeAliasAccessor accessor = new TarantoolMapTypeAliasAccessor(testTypeKey);

        Map<String, Object> map = new HashMap<>();
        accessor.writeTypeTo(map, testTypeValue);
        assertThat(map.keySet()).hasSize(1);
        assertThat(map.get(testTypeKey)).isEqualTo(testTypeValue);

        Alias alias = accessor.readAliasFrom(map);
        assertThat(alias.getValue()).isEqualTo(testTypeValue);
    }
}
