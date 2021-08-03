package org.springframework.data.tarantool.repository.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.domain.User;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TarantoolQueryMethodTest {
    private TarantoolMappingContext context;

    @BeforeEach
    void setUp() {
        context = new TarantoolMappingContext();
    }

    @Test
    void shouldConsiderMethodAsCollectionQuery() throws Exception {
        TarantoolQueryMethod queryMethod = queryMethod(SampleRepository.class, "method");

        assertThat(queryMethod.isStreamQuery()).isFalse();
        assertThat(queryMethod.isCollectionQuery()).isTrue();
    }

    @Test
    void shouldConsiderMonoMethodAsEntityQuery() throws Exception {
        TarantoolQueryMethod queryMethod = queryMethod(SampleRepository.class, "single");

        assertThat(queryMethod.isCollectionQuery()).isFalse();
        assertThat(queryMethod.isQueryForEntity()).isTrue();
    }

    private TarantoolQueryMethod queryMethod(Class<?> repository, String name, Class<?>... parameters) throws Exception {
        Method method = repository.getMethod(name, parameters);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        return new TarantoolQueryMethod(method, new DefaultRepositoryMetadata(repository), factory, context);
    }

    @SuppressWarnings("unused")
    interface SampleRepository extends Repository<User, UUID> {

        List<User> method();

        User single();
    }
}
