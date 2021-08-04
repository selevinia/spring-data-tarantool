package org.springframework.data.tarantool.repository.support;

import io.tarantool.driver.api.conditions.Conditions;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.repository.Sort;
import org.springframework.data.tarantool.repository.TarantoolRepository;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
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

    public SimpleTarantoolRepository(TarantoolEntityInformation<T, ID> entityInformation,
                                     TarantoolOperations operations) {
        Assert.notNull(entityInformation, "TarantoolEntityInformation must not be null");
        Assert.notNull(operations, "TarantoolOperations must not be null");

        this.entityInformation = entityInformation;
        this.operations = operations;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "The given entity must not be null");

        if (this.entityInformation.isNew(entity)) {
            return operations.insert(entity, (Class<S>) entityInformation.getJavaType());
        }
        return operations.replace(entity, (Class<S>) entityInformation.getJavaType());
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null");

        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public Optional<T> findById(ID id) {
        Assert.notNull(id, "The given id must not be null");

        return Optional.ofNullable(operations.selectById(id, entityInformation.getJavaType()));
    }

    @Override
    public boolean existsById(ID id) {
        Assert.notNull(id, "The given id must not be null");

        return Optional.ofNullable(operations.selectById(id, entityInformation.getJavaType())).isPresent();
    }

    @Override
    public Iterable<T> findAll() {
        return operations.select(entityInformation.getJavaType());
    }

    @Override
    public List<T> findAll(Sort sort) {
        if (sort.isAscending()) {
            return operations.select(Conditions.ascending(), entityInformation.getJavaType());
        }

        if (sort.isDescending()) {
            return operations.select(Conditions.descending(), entityInformation.getJavaType());
        }

        throw new IllegalArgumentException(sort + "is not supported");
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        Assert.notNull(ids, "The given Iterable of ids must not be null");

        if (!ids.iterator().hasNext()) {
            return Collections.emptyList();
        }

        return operations.selectByIds(ids, entityInformation.getJavaType());
    }

    @Override
    public long count() {
        return operations.count(entityInformation.getJavaType());
    }

    @Override
    public void deleteById(ID id) {
        Assert.notNull(id, "The given id must not be null");

        operations.deleteById(id, entityInformation.getJavaType());
    }

    @Override
    public void delete(T entity) {
        Assert.notNull(entity, "The entity must not be null");

        operations.delete(entity, entityInformation.getJavaType());
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null");

        entities.forEach(entity -> operations.delete(entity, entityInformation.getJavaType()));
    }

    @Override
    public void deleteAll() {
        operations.truncate(entityInformation.getJavaType());
    }

}
