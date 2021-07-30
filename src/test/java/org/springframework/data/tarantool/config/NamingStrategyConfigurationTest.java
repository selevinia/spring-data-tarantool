package org.springframework.data.tarantool.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.BasicTarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.Field;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
public class NamingStrategyConfigurationTest {

    @Configuration
    static class Config extends AbstractReactiveTarantoolConfiguration {

    }

    @Autowired
    private TarantoolConverter mappingTarantoolConverter;

    @Test
    void shouldDeriveFieldName() {
        BasicTarantoolPersistentEntity<?> entity = mappingTarantoolConverter.getMappingContext().getPersistentEntity(Person.class);
        assertThat(entity.getRequiredIdProperty().getFieldName()).isEqualTo("first_name");
    }

    @Test
    void shouldDeriveAnnotatedFieldName() {
        BasicTarantoolPersistentEntity<?> entity = mappingTarantoolConverter.getMappingContext().getPersistentEntity(SomePerson.class);
        assertThat(entity.getRequiredIdProperty().getFieldName()).isEqualTo("some_name");
    }

    @Test
    void shouldDeriveBareFieldName() {
        BasicTarantoolPersistentEntity<?> entity = mappingTarantoolConverter.getMappingContext().getPersistentEntity(AnotherPerson.class);
        assertThat(entity.getRequiredIdProperty().getFieldName()).isEqualTo("name");
    }

    private static class Person {

        @Id
        String firstName;
    }

    private static class SomePerson {

        @Id
        @Field("some_name")
        String firstName;
    }

    private static class AnotherPerson {

        @Id
        String name;
    }

}
