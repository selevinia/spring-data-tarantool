package org.springframework.data.tarantool.core.mapping;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class TarantoolMappingContextTest {

    @Test
    void shouldGetRequiredPersistentEntity() {
        TarantoolMappingContext mappingContext = new TarantoolMappingContext();

        assertThat(mappingContext.getPersistentEntities().size()).isEqualTo(0);

        BasicTarantoolPersistentEntity<?> entityWithSpace = mappingContext.getPersistentEntity(SpaceMessage.class);
        assertThat(entityWithSpace).isNotNull();
        assertThat(entityWithSpace.hasSpaceAnnotation()).isTrue();
        assertThat(entityWithSpace.getSpaceName()).isEqualTo("messages");

        BasicTarantoolPersistentEntity<?> simpleEntity = mappingContext.getPersistentEntity(SimpleMessage.class);
        assertThat(simpleEntity).isNotNull();
        assertThat(simpleEntity.hasSpaceAnnotation()).isFalse();

        BasicTarantoolPersistentEntity<?> notAnEntity = mappingContext.getPersistentEntity(LocalDateTime.class);
        assertThat(notAnEntity).isNull();

        assertThat(mappingContext.getPersistentEntities().size()).isEqualTo(2);
    }

    @Space("messages")
    private static class SpaceMessage {
    }

    private static class SimpleMessage {
    }
}
