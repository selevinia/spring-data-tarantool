package org.springframework.data.tarantool.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * Tarantool specific {@link org.springframework.data.repository.Repository} interface with reactive support.
 *
 * @author Alexander Rublev
 */
@NoRepositoryBean
public interface ReactiveTarantoolRepository<T, ID> extends ReactiveCrudRepository<T, ID> {

    /**
     * Returns all entities sorted by the given options.
     *
     * @param sort must not be {@literal null}.
     * @return all entities sorted by the given options.
     * @throws IllegalArgumentException in case the given {@link Sort} is {@literal null}.
     */
    Flux<T> findAll(Sort sort);
}
