package org.springframework.data.tarantool.core.mapping;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicMapIdTest {

    @Test
    void shouldCreateMapIdFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("field1", "value1");
        map.put("field2", 2);

        BasicMapId basicMapId = new BasicMapId(map);
        assertThat(basicMapId)
                .containsEntry("field1", map.get("field1"))
                .containsEntry("field2", map.get("field2"));
    }
}
