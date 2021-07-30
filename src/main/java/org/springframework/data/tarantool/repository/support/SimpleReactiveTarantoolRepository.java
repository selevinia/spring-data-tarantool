package org.springframework.data.tarantool.repository.support;

import io.tarantool.driver.api.conditions.Conditions;
import org.reactivestreams.Publisher;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.mapping.BasicTarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.tarantool.repository.ReactiveTarantoolRepository;
import org.springframework.data.tarantool.repository.Sort;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class SimpleReactiveTarantoolRepository<T, ID> implements ReactiveTarantoolRepository<T, ID> {
    private final TarantoolEntityInformation<T, ID> entityInformation;
    private final ReactiveTarantoolOperations operations;
    private final AbstractMappingContext<BasicTarantoolPersistentEntity<?>, TarantoolPersistentProperty> mappingContext;

    public SimpleReactiveTarantoolRepository(TarantoolEntityInformation<T, ID> entityInformation,
                                             ReactiveTarantoolOperations operations) {
        Assert.notNull(entityInformation, "TarantoolEntityInformation must not be null");
        Assert.notNull(operations, "ReactiveTarantoolOperations must not be null");

        this.entityInformation = entityInformation;
        this.operations = operations;
        this.mappingContext = operations.getConverter().getMappingContext();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> Mono<S> save(S entity) {
        Assert.notNull(entity, "The given entity must not be null");

        BasicTarantoolPersistentEntity<?> persistentEntity = this.mappingContext.getPersistentEntity(entity.getClass());
        if (persistentEntity != null && persistentEntity.hasVersionProperty()) {
            if (!this.entityInformation.isNew(entity)) {
                return operations.replace(entity, (Class<S>) entityInformation.getJavaType());
            }
        }
        return operations.insert(entity, (Class<S>) entityInformation.getJavaType());
    }

    @Override
    public <S extends T> Flux<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null");

        return Flux.fromIterable(entities).flatMap(this::save);
    }

    @Override
    public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
        Assert.notNull(entityStream, "The given Publisher of entities must not be null");

        return Flux.from(entityStream).flatMap(this::save);
    }

    @Override
    public Mono<T> findById(ID id) {
        Assert.notNull(id, "The given id must not be null");

        return operations.selectById(id, entityInformation.getJavaType());
    }

    @Override
    public Mono<T> findById(Publisher<ID> id) {
        Assert.notNull(id, "The given Publisher of id must not be null");

        return Mono.from(id).flatMap(this::findById);
    }

    @Override
    public Mono<Boolean> existsById(ID id) {
        Assert.notNull(id, "The given id must not be null");

        return operations.selectById(id, entityInformation.getJavaType()).map(entity -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> existsById(Publisher<ID> id) {
        Assert.notNull(id, "The given Publisher of id must not be null");

        return Mono.from(id).flatMap(this::existsById);
    }

    @Override
    public Flux<T> findAll() {
        return operations.select(entityInformation.getJavaType());
    }

    @Override
    public Flux<T> findAll(Sort sort) {
        if (sort.isAscending()) {
            return operations.select(Conditions.ascending(), entityInformation.getJavaType());
        }

        if (sort.isDescending()) {
            return operations.select(Conditions.descending(), entityInformation.getJavaType());
        }

        throw new IllegalArgumentException(sort + "is not supported");
    }

    @Override
    public Flux<T> findAllById(Iterable<ID> ids) {
        Assert.notNull(ids, "The given Iterable of ids must not be null");

        return findAllById(Flux.fromIterable(ids));
    }

    @Override
    public Flux<T> findAllById(Publisher<ID> idStream) {
        Assert.notNull(idStream, "The given Publisher of ids must not be null");

        return operations.selectByIds(idStream, entityInformation.getJavaType());
    }

    @Override
    public Mono<Long> count() {
        return operations.count(entityInformation.getJavaType());
    }

    @Override
    public Mono<Void> deleteById(ID id) {
        Assert.notNull(id, "The given id must not be null");

        return operations.deleteById(id, entityInformation.getJavaType()).then();
    }

    @Override
    public Mono<Void> deleteById(Publisher<ID> id) {
        Assert.notNull(id, "The given Publisher of id must not be null");

        return Mono.from(id).flatMap(this::deleteById);
    }

    @Override
    public Mono<Void> delete(T entity) {
        Assert.notNull(entity, "The entity must not be null");

        return operations.delete(entity, entityInformation.getJavaType()).then();
    }

    @Override
    public Mono<Void> deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities must not be null");

        return Flux.fromIterable(entities).flatMap(this::delete).then();
    }

    @Override
    public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {
        Assert.notNull(entityStream, "The given Publisher of entities must not be null");

        return Flux.from(entityStream).flatMap(this::delete).then();
    }

    @Override
    public Mono<Void> deleteAll() {
        return operations.truncate(entityInformation.getJavaType()).then();
    }
}
