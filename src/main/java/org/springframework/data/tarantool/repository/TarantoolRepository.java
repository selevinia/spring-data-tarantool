package org.springframework.data.tarantool.repository;

import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.Space;

import java.util.List;

/**
 * Tarantool-specific extension of the {@link CrudRepository} interface that allows the specification of a type for the
 * identity of the {@link Space @Space} (or {@link Persistable}) type.
 * Repositories based on {@link TarantoolRepository} can define either a single primary key, use a primary key class or
 * a compound primary key without a primary key class. Types using a compound primary key without a primary key class
 * must use {@link MapId} to declare their key value.
 *
 * @author Alexander Rublev
 * @see MapIdTarantoolRepository
 */
@NoRepositoryBean
public interface TarantoolRepository<T, ID> extends CrudRepository<T, ID> {

    /**
     * Returns all entities sorted by the given options.
     *
     * @param sort must not be {@literal null}.
     * @return all entities sorted by the given options.
     * @throws IllegalArgumentException in case the given {@link Sort} is {@literal null}.
     */
    List<T> findAll(Sort sort);
}
