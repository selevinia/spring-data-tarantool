package org.springframework.data.tarantool.repository.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.domain.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ReactiveTarantoolQueryMethodTest {
    private TarantoolMappingContext context;

    @BeforeEach
    void setUp() {
        context = new TarantoolMappingContext();
    }

    @Test
    void shouldConsiderMethodAsStreamQuery() throws Exception {
        TarantoolQueryMethod queryMethod = queryMethod(SampleRepository.class, "method");

        assertThat(queryMethod.isStreamQuery()).isTrue();
    }

    @Test
    void shouldConsiderMethodAsCollectionQuery() throws Exception {
        TarantoolQueryMethod queryMethod = queryMethod(SampleRepository.class, "method");

        assertThat(queryMethod.isCollectionQuery()).isTrue();
    }

    @Test
    void shouldConsiderMonoMethodAsEntityQuery() throws Exception {
        TarantoolQueryMethod queryMethod = queryMethod(SampleRepository.class, "mono");

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

        Flux<User> method();

        Mono<User> mono();
    }
}
