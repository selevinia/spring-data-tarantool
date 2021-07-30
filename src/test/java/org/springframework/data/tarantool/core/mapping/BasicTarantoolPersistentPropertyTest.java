package org.springframework.data.tarantool.core.mapping;

import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.ReflectionUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicTarantoolPersistentPropertyTest {

    @Test
    void shouldUseAnnotatedPrimaryKeyFieldName() {
        assertThat(getPropertyFor(Timeline.class, "id").getFieldName()).hasToString("identifier");
    }

    @Test
    void shouldUseAnnotatedFieldName() {
        assertThat(getPropertyFor(Timeline.class, "text").getFieldName()).hasToString("message");
    }

    @Test
    void shouldUsePropertyNameForUnannotatedProperty() {
        assertThat(getPropertyFor(Timeline.class, "time").getFieldName()).hasToString("time");
    }

    @Test
    void shouldBeIdProperty() {
        TarantoolPersistentProperty property = getPropertyFor(Timeline.class, "id");
        assertThat(property.isIdProperty()).isTrue();
    }

    @Test
    void shouldBeCompositePrimaryKeyProperty() {
        TarantoolPersistentProperty property = getPropertyFor(TypedTimeline.class, "id");
        assertThat(property.isCompositePrimaryKey()).isTrue();
    }

    @Test
    void shouldBePrimaryKeyFieldProperty() {
        assertThat(getPropertyFor(CompositeKey.class, "id").isPrimaryKeyField()).isTrue();
        assertThat(getPropertyFor(CompositeKey.class, "type").isPrimaryKeyField()).isTrue();
    }

    private TarantoolPersistentProperty getPropertyFor(Class<?> type, String fieldName) {
        java.lang.reflect.Field field = ReflectionUtils.findField(type, fieldName);

        return new BasicTarantoolPersistentProperty(Property.of(ClassTypeInformation.from(type), field), getEntity(type),
                TarantoolSimpleTypeHolder.HOLDER, PropertyNameFieldNamingStrategy.INSTANCE);
    }

    private <T> BasicTarantoolPersistentEntity<T> getEntity(Class<T> type) {
        return new BasicTarantoolPersistentEntity<>(ClassTypeInformation.from(type));
    }

    private static class Timeline {
        @PrimaryKey("identifier")
        String id;

        LocalDateTime time;

        @Field("message")
        String text;
    }

    private static class TypedTimeline {
        @PrimaryKey
        CompositeKey id;

        LocalDateTime time;
    }

    @PrimaryKeyClass
    private static class CompositeKey {
        @PrimaryKeyField("identifier")
        String id;

        @PrimaryKeyField
        String type;
    }
}
