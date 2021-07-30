package org.springframework.data.tarantool.repository.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.domain.User;
import org.springframework.data.tarantool.repository.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ReactiveTarantoolRepositoryQueryTest {

    @Mock
    private ReactiveTarantoolOperations operations;

    private TarantoolMappingContext context;

    @BeforeEach
    void setUp() {
        context = new TarantoolMappingContext();
    }

    @Test
    void shouldCreateSimpleQuery() throws Exception {
        ReactiveDirectTarantoolQuery query = getQuery(SampleRepository.class, "findByLastname", String.class);
        assertThat(query.getExecution()).isInstanceOf(ReactiveDirectTarantoolQuery.DirectTarantoolQueryExecution.SingleEntityExecution.class);
    }

    @Test
    void shouldCreateCollectionQuery() throws Exception {
        ReactiveDirectTarantoolQuery query = getQuery(SampleRepository.class, "findAllUsers");
        assertThat(query.getExecution()).isInstanceOf(ReactiveDirectTarantoolQuery.DirectTarantoolQueryExecution.CollectionExecution.class);
    }

    private ReactiveDirectTarantoolQuery getQuery(Class<?> repository, String name, Class<?>... parameters) throws Exception {
        Method method = repository.getMethod(name, parameters);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        TarantoolQueryMethod queryMethod = new TarantoolQueryMethod(method, new DefaultRepositoryMetadata(repository), factory, context);
        return new ReactiveDirectTarantoolQuery(queryMethod, operations);
    }

    @SuppressWarnings("unused")
    private interface SampleRepository extends Repository<User, UUID> {

        @Query(function = "find_by_last_name")
        Mono<User> findByLastname(String lastname);

        @Query(function = "find_all_users")
        Flux<User> findAllUsers();
    }
}