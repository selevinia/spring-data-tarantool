package org.springframework.data.tarantool.repository.support;

import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.core.mapping.BasicTarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.tarantool.repository.Sort;
import org.springframework.data.tarantool.repository.TarantoolRepository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * Repository base implementation for Tarantool.
 *
 * @author Alexander Rublev
 * @see TarantoolRepository
 */
public class SimpleTarantoolRepository<T, ID> implements TarantoolRepository<T, ID> {
    private final TarantoolEntityInformation<T, ID> entityInformation;
    private final TarantoolOperations operations;
    private final AbstractMappingContext<BasicTarantoolPersistentEntity<?>, TarantoolPersistentProperty> mappingContext;

    public SimpleTarantoolRepository(TarantoolEntityInformation<T, ID> entityInformation,
                                             TarantoolOperations operations) {
        Assert.notNull(entityInformation, "TarantoolEntityInformation must not be null");
        Assert.notNull(operations, "TarantoolOperations must not be null");

        this.entityInformation = entityInformation;
        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();
    }

    @Override
    public <S extends T> S save(S entity) {
        return null; //TODO implement using operations
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        return null; //TODO implement using operations
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.empty(); //TODO implement using operations
    }

    @Override
    public boolean existsById(ID id) {
        return false; //TODO implement using operations
    }

    @Override
    public Iterable<T> findAll() {
        return null; //TODO implement using operations
    }

    @Override
    public List<T> findAll(Sort sort) {
        return null; //TODO implement using operations
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        return null; //TODO implement using operations
    }

    @Override
    public long count() {
        return 0; //TODO implement using operations
    }

    @Override
    public void deleteById(ID id) {
        //TODO implement using operations
    }

    @Override
    public void delete(T entity) {
        //TODO implement using operations
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        //TODO implement using operations
    }

    @Override
    public void deleteAll() {
        //TODO implement using operations
    }

}
