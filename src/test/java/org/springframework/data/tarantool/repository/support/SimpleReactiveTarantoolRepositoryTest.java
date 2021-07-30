package org.springframework.data.tarantool.repository.support;

import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;

import java.io.Serializable;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class SimpleReactiveTarantoolRepositoryTest {
    private final TarantoolMappingContext mappingContext = new TarantoolMappingContext();
    private final MappingTarantoolConverter converter = new MappingTarantoolConverter(mappingContext);

    private SimpleReactiveTarantoolRepository<Object, ? extends Serializable> repository;

    @Mock
    private ReactiveTarantoolOperations tarantoolOperations;

    @BeforeEach
    void before() {
        when(tarantoolOperations.getConverter()).thenReturn(converter);
    }

    @Test
    void shouldInsertNewSimpleEntity() {
        TarantoolPersistentEntity<?> entity = converter.getMappingContext().getRequiredPersistentEntity(SimplePerson.class);
        repository = new SimpleReactiveTarantoolRepository<Object, String>(
                new MappingTarantoolEntityInformation(entity),
                tarantoolOperations);

        SimplePerson person = new SimplePerson();
        repository.save(person);

        verify(tarantoolOperations).insert(person, SimplePerson.class);
    }

    @Test
    void shouldInsertNewVersionedEntity() {
        TarantoolPersistentEntity<?> entity = converter.getMappingContext().getRequiredPersistentEntity(VersionedPerson.class);
        repository = new SimpleReactiveTarantoolRepository<Object, String>(
                new MappingTarantoolEntityInformation(entity),
                tarantoolOperations);

        VersionedPerson person = new VersionedPerson();
        repository.save(person);

        verify(tarantoolOperations).insert(person, VersionedPerson.class);
    }

    @Test
    void shouldUpdateExistingVersionedEntity() {
        TarantoolPersistentEntity<?> entity = converter.getMappingContext().getRequiredPersistentEntity(VersionedPerson.class);
        repository = new SimpleReactiveTarantoolRepository<Object, String>(
                new MappingTarantoolEntityInformation(entity),
                tarantoolOperations);

        VersionedPerson person = new VersionedPerson();
        person.setVersion(2);
        repository.save(person);

        verify(tarantoolOperations).replace(person, VersionedPerson.class);
    }

    @Data
    static class SimplePerson {

        @Id
        String id;
    }

    @Data
    static class VersionedPerson {

        @Id
        String id;
        @Version
        long version;
    }
}
