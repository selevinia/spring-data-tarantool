package org.springframework.data.tarantool.repository.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.Repository;
import org.springframework.data.tarantool.core.ReactiveTarantoolTemplate;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.BasicTarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.domain.User;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ReactiveTarantoolRepositoryFactoryTest {

    @Mock
    private BasicTarantoolPersistentEntity entity;

    @Mock
    private TarantoolConverter converter;

    @Mock
    private TarantoolMappingContext mappingContext;

    @Mock
    private ReactiveTarantoolTemplate template;

    @BeforeEach
    void setUp() {
        when(template.getConverter()).thenReturn(converter);
        when(converter.getMappingContext()).thenReturn(mappingContext);
    }

    @Test
    void shouldUseMappingTarantoolEntityInformationIfMappingContextSet() {
        when(mappingContext.getRequiredPersistentEntity(User.class)).thenReturn(entity);
        ReactiveTarantoolRepositoryFactory repositoryFactory = new ReactiveTarantoolRepositoryFactory(template);

        TarantoolEntityInformation<User, Serializable> entityInformation = repositoryFactory.getEntityInformation(User.class);
        assertThat(entityInformation).isInstanceOf(MappingTarantoolEntityInformation.class);
    }

    @Test
    void shouldCreatesRepositoryWithIdTypeString() {
        when(mappingContext.getRequiredPersistentEntity(User.class)).thenReturn(entity);
        ReactiveTarantoolRepositoryFactory repositoryFactory = new ReactiveTarantoolRepositoryFactory(template);

        SimpleUserRepository repository = repositoryFactory.getRepository(SimpleUserRepository.class);
        assertThat(repository).isNotNull();
    }

    interface SimpleUserRepository extends Repository<User, String> {
    }
}
