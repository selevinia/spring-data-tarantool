package org.springframework.data.tarantool.core;

import io.tarantool.driver.TarantoolVersion;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.exceptions.TarantoolSpaceOperationException;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.mappers.TarantoolTupleSingleResultMapperFactory;
import io.tarantool.driver.mappers.ValueConverter;
import io.tarantool.driver.metadata.TarantoolSpaceMetadata;
import org.msgpack.value.Value;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.mapping.callback.ReactiveEntityCallbacks;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.event.ReactiveBeforeConvertCallback;
import org.springframework.data.tarantool.core.mapping.event.ReactiveBeforeSaveCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Primary implementation of {@link ReactiveTarantoolOperations}
 *
 * @author Tatiana Blinova
 * @author Alexander Rublev
 */
public class ReactiveTarantoolTemplate implements ApplicationContextAware, ReactiveTarantoolOperations {

    public static final int TARANTOOL_DEFAULT_POOL_SIZE = Optional.ofNullable(System.getProperty("tarantool.schedulers.defaultPoolSize"))
            .map(Integer::parseInt)
            .orElseGet(() -> Runtime.getRuntime().availableProcessors());
    private static final Scheduler TARANTOOL_PARALLEL_SCHEDULER = Schedulers.newParallel("tarantool-parallel-scheduler", TARANTOOL_DEFAULT_POOL_SIZE, true);

    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
    private final TarantoolConverter tarantoolConverter;
    private final TarantoolExceptionTranslator exceptionTranslator;
    private final MessagePackMapper messagePackMapper;
    private final TarantoolTupleMethodsHelper tupleMethodsHelper;
    private @Nullable ReactiveEntityCallbacks entityCallbacks;

    public ReactiveTarantoolTemplate(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
        this(tarantoolClient, MappingTarantoolConverter.newConverter(), new DefaultTarantoolExceptionTranslator());
    }

    public ReactiveTarantoolTemplate(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                                     TarantoolConverter tarantoolConverter,
                                     TarantoolExceptionTranslator exceptionTranslator) {
        this.tarantoolClient = tarantoolClient;
        this.tarantoolConverter = tarantoolConverter;
        this.exceptionTranslator = exceptionTranslator;
        this.messagePackMapper = tarantoolClient.getConfig().getMessagePackMapper();
        this.tupleMethodsHelper = new TarantoolTupleMethodsHelper(tarantoolConverter, this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (entityCallbacks == null) {
            setEntityCallbacks(ReactiveEntityCallbacks.create(applicationContext));
        }
    }

    /**
     * Configure {@link EntityCallbacks} to pre-/post-process entities during persistence operations.
     *
     * @param entityCallbacks context wide callbacks
     */
    public void setEntityCallbacks(@Nullable ReactiveEntityCallbacks entityCallbacks) {
        this.entityCallbacks = entityCallbacks;
    }

    protected <T> Mono<T> maybeCallBeforeConvert(T object, String spaceName) {
        if (null != entityCallbacks) {
            return entityCallbacks.callback(ReactiveBeforeConvertCallback.class, object, spaceName);
        }

        return Mono.just(object);
    }

    protected <T> Mono<T> maybeCallBeforeSave(T object, TarantoolTuple tuple, String spaceName) {
        if (null != entityCallbacks) {
            return entityCallbacks.callback(ReactiveBeforeSaveCallback.class, object, tuple, spaceName);
        }

        return Mono.just(object);
    }

    @Override
    public TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> getClient() {
        return tarantoolClient;
    }

    @Override
    public TarantoolConverter getConverter() {
        return tarantoolConverter;
    }

    @Override
    public <T, ID> Mono<T> selectById(ID id, Class<T> entityClass) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        Conditions query = tupleMethodsHelper.primaryIndexQueryById(id, entityClass);
        return selectOne(query, entityClass);
    }

    @Override
    public <T, ID> Flux<T> selectByIds(Publisher<ID> ids, Class<T> entityClass) {
        Assert.notNull(ids, "Publisher of ids must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return Flux.from(ids)
                .parallel(TARANTOOL_DEFAULT_POOL_SIZE)
                .runOn(TARANTOOL_PARALLEL_SCHEDULER)
                .flatMap(id -> selectById(id, entityClass))
                .sequential();
    }

    @Override
    public <T> Mono<T> selectOne(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return execute(entityClass, spaceOps -> spaceOps.select(query))
                .filter(tuples -> tuples.size() > 0)
                .map(tuples -> tupleToEntity(tuples.get(0), entityClass));
    }

    @Override
    public <T> Flux<T> select(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return execute(entityClass, spaceOps -> spaceOps.select(query))
                .publishOn(TARANTOOL_PARALLEL_SCHEDULER)
                .flatMapIterable(tuples -> tuples)
                .map(tuple -> tupleToEntity(tuple, entityClass));
    }

    @Override
    public <T> Flux<T> select(Class<T> entityClass) {
        return select(Conditions.any(), entityClass);
    }

    @Override
    public <T> Mono<Long> count(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return execute(entityClass, spaceOps -> spaceOps.select(query))
                .map(tuples -> (long) tuples.size()); // TODO - len() or count() not implemented yet in driver
    }

    @Override
    public <T> Mono<Long> count(Class<T> entityClass) {
        return count(Conditions.any(), entityClass);
    }

    @Override
    public <T> Mono<T> insert(T entity, Class<T> entityClass) {
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        T entityToUse = entityToInsert(entity);
        String spaceName = spaceName(entityClass);
        TarantoolSpaceMetadata spaceMetadata = requiredSpaceMetadata(spaceName);
        return maybeCallBeforeConvert(entityToUse, spaceName)
                .flatMap(ent -> {
                    TarantoolTuple tuple = entityToTuple(ent, messagePackMapper, spaceMetadata);
                    return maybeCallBeforeSave(ent, tuple, spaceName)
                            .map(e -> tuple);
                })
                .flatMap(tuple -> execute(spaceName, spaceOps -> spaceOps.insert(tuple)))
                .filter(tuples -> tuples.size() > 0)
                .map(tuples -> tupleToEntity(tuples.get(0), entityClass));
    }

    @Override
    public <T> Mono<T> replace(T entity, Class<T> entityClass) {
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        T entityToUse = entityToUpdate(entity);
        String spaceName = spaceName(entityClass);
        TarantoolSpaceMetadata spaceMetadata = requiredSpaceMetadata(spaceName);
        return maybeCallBeforeConvert(entityToUse, spaceName)
                .flatMap(ent -> {
                    TarantoolTuple tuple = entityToTuple(ent, messagePackMapper, spaceMetadata);
                    return maybeCallBeforeSave(ent, tuple, spaceName)
                            .map(e -> tuple);
                })
                .flatMap(tuple -> execute(spaceName, spaceOps -> spaceOps.replace(tuple)))
                .filter(tuples -> tuples.size() > 0)
                .map(tuples -> tupleToEntity(tuples.get(0), entityClass));
    }

    @Override
    public <T> Flux<T> update(Conditions query, T entity, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        T entityToUse = entityToUpdate(entity);
        String spaceName = spaceName(entityClass);
        TarantoolSpaceMetadata spaceMetadata = requiredSpaceMetadata(spaceName);
        return maybeCallBeforeConvert(entityToUse, spaceName)
                .flatMap(ent -> {
                    TarantoolTuple tuple = entityToTuple(ent, messagePackMapper, spaceMetadata);
                    return maybeCallBeforeSave(ent, tuple, spaceName)
                            .map(e -> tuple);
                })
                .map(tupleMethodsHelper::prepareUpdateOperations)
                .flatMapMany(tupleOperations ->
                        execute(spaceName, spaceOps -> spaceOps.select(query))
                                .publishOn(TARANTOOL_PARALLEL_SCHEDULER)
                                .flatMapIterable(tuples -> tuples)
                                .parallel(TARANTOOL_DEFAULT_POOL_SIZE)
                                .runOn(TARANTOOL_PARALLEL_SCHEDULER)
                                .map(tuple -> tupleMethodsHelper.primaryIndexQuery(tuple, entityClass))
                                .flatMap(conditions -> execute(spaceName, spaceOps -> spaceOps.update(conditions, tupleOperations)))
                                .filter(tuples -> tuples.size() > 0)
                                .map(tuples -> tupleToEntity(tuples.get(0), entityClass))
                                .sequential()
                );
    }

    @Override
    public <T> Mono<T> delete(T entity, Class<T> entityClass) {
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        Conditions query = tupleMethodsHelper.primaryIndexQuery(entity);
        return execute(entityClass, spaceOps -> spaceOps.delete(query))
                .filter(tuples -> tuples.size() > 0)
                .map(tuples -> tupleToEntity(tuples.get(0), entityClass));
    }

    @Override
    public <T> Flux<T> delete(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        return execute(spaceName, spaceOps -> spaceOps.select(query))
                .publishOn(TARANTOOL_PARALLEL_SCHEDULER)
                .flatMapIterable(tuples -> tuples)
                .parallel(TARANTOOL_DEFAULT_POOL_SIZE)
                .runOn(TARANTOOL_PARALLEL_SCHEDULER)
                .map(tuple -> tupleMethodsHelper.primaryIndexQuery(tuple, entityClass))
                .flatMap(conditions -> execute(spaceName, spaceOps -> spaceOps.delete(conditions)))
                .filter(tuples -> tuples.size() > 0)
                .map(tuples -> tupleToEntity(tuples.get(0), entityClass))
                .sequential();
    }

    @Override
    public <T, ID> Mono<T> deleteById(ID id, Class<T> entityClass) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        Conditions query = tupleMethodsHelper.primaryIndexQueryById(id, entityClass);
        return execute(entityClass, spaceOps -> spaceOps.delete(query))
                .filter(tuples -> tuples.size() > 0)
                .map(tuples -> tupleToEntity(tuples.get(0), entityClass));
    }

    @Override
    public <T> Mono<Boolean> truncate(Class<T> entityClass) {
        Assert.notNull(entityClass, "Entity class must not be null");

        if (isProxyClient()) {
            String spaceName = spaceName(entityClass);
            return execute(() -> tarantoolClient.callForSingleResult("crud.truncate", Collections.singletonList(spaceName), Boolean.class))
                    .flatMap(result -> {
                        if (result) {
                            return Mono.just(true);
                        } else {
                            return Mono.error(new TarantoolSpaceOperationException(String.format("Failed to truncate space %s", spaceName)));
                        }
                    });
        }
        return Mono.error(new UnsupportedOperationException("Truncate operation not supported yet in driver"));
    }

    @Override
    public <T> Mono<T> call(String functionName, Object[] parameters, Class<T> entityClass) {
        return call(functionName, Arrays.asList(parameters), entityClass);
    }

    @Override
    public <T> Mono<T> call(String functionName, Object[] parameters, ValueConverter<Value, T> entityConverter) {
        return call(functionName, Arrays.asList(parameters), entityConverter);
    }

    @Override
    public <T> Mono<T> call(String functionName, List<?> parameters, Class<T> entityClass) {
        Assert.hasText(functionName, "Function name must not be null or empty");
        Assert.notNull(parameters, "Parameters must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        TarantoolPersistentEntity<?> entityMetadata = tarantoolConverter.getMappingContext().getPersistentEntity(entityClass);
        Optional<TarantoolSpaceMetadata> spaceMetadata = entityMetadata != null ? spaceMetadata(entityMetadata.getSpaceName()) : Optional.empty();
        if (spaceMetadata.isPresent()) {
            TarantoolTupleSingleResultMapperFactory resultMapperFactory = tarantoolClient.getResultMapperFactoryFactory().defaultTupleSingleResultMapperFactory();
            return execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper,
                    resultMapperFactory.withDefaultTupleValueConverter(messagePackMapper, spaceMetadata.orElse(null))))
                    .filter(tuples -> tuples.size() > 0)
                    .map(tuples -> tupleToEntity(tuples.get(0), entityClass));
        } else {
            return call(functionName, parameters, valueConverter(messagePackMapper, entityClass));
        }
    }

    @Override
    public <T> Mono<T> call(String functionName, List<?> parameters, ValueConverter<Value, T> entityConverter) {
        Assert.hasText(functionName, "Function name must not be null or empty");
        Assert.notNull(parameters, "Parameters must not be null");
        Assert.notNull(entityConverter, "Entity converter must not be null");

        ValueConverter<Value, Value> converter = value -> value.isNilValue() ? null : value;
        return execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper, converter))
                .map(entityConverter::fromValue);
    }

    @Override
    public <T> Mono<T> call(String functionName, Class<T> entityClass) {
        return call(functionName, Collections.emptyList(), entityClass);
    }

    @Override
    public <T> Mono<T> call(String functionName, ValueConverter<Value, T> entityConverter) {
        return call(functionName, Collections.emptyList(), entityConverter);
    }

    @Override
    public <T> Flux<T> callForAll(String functionName, Object[] parameters, Class<T> entityClass) {
        return callForAll(functionName, Arrays.asList(parameters), entityClass);
    }

    @Override
    public <T> Flux<T> callForAll(String functionName, Object[] parameters, ValueConverter<Value, T> entityConverter) {
        return callForAll(functionName, Arrays.asList(parameters), entityConverter);
    }

    @Override
    public <T> Flux<T> callForAll(String functionName, List<?> parameters, Class<T> entityClass) {
        Assert.hasText(functionName, "Function name must not be null or empty");
        Assert.notNull(parameters, "Parameters must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        TarantoolPersistentEntity<?> entityMetadata = tarantoolConverter.getMappingContext().getPersistentEntity(entityClass);
        Optional<TarantoolSpaceMetadata> spaceMetadata = entityMetadata != null ? spaceMetadata(entityMetadata.getSpaceName()) : Optional.empty();
        if (spaceMetadata.isPresent()) {
            TarantoolTupleSingleResultMapperFactory resultMapperFactory = tarantoolClient.getResultMapperFactoryFactory().defaultTupleSingleResultMapperFactory();
            return execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper, resultMapperFactory.withDefaultTupleValueConverter(messagePackMapper, spaceMetadata.orElse(null))))
                    .publishOn(TARANTOOL_PARALLEL_SCHEDULER)
                    .flatMapIterable(tuples -> tuples)
                    .map(tuple -> tupleToEntity(tuple, entityClass));
        } else {
            return callForAll(functionName, parameters, value -> tupleToEntity(messagePackMapper.fromValue(value, Map.class), entityClass));
        }
    }

    @Override
    public <T> Flux<T> callForAll(String functionName, List<?> parameters, ValueConverter<Value, T> entityConverter) {
        Assert.hasText(functionName, "Function name must not be null or empty");
        Assert.notNull(parameters, "Parameters must not be null");
        Assert.notNull(entityConverter, "Entity converter must not be null");

        ValueConverter<Value, List<Value>> converter = value -> value.isNilValue() ? null : value.asArrayValue().list();
        return execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper, converter))
                .publishOn(TARANTOOL_PARALLEL_SCHEDULER)
                .flatMapIterable(values -> values)
                .map(entityConverter::fromValue);
    }

    @Override
    public <T> Flux<T> callForAll(String functionName, Class<T> entityClass) {
        return callForAll(functionName, Collections.emptyList(), entityClass);
    }

    @Override
    public <T> Flux<T> callForAll(String functionName, ValueConverter<Value, T> entityConverter) {
        return callForAll(functionName, Collections.emptyList(), entityConverter);
    }

    @Override
    public TarantoolVersion getVersion() {
        return executeSerial(tarantoolClient::getVersion);
    }

    @Override
    public DataAccessException dataAccessException(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            DataAccessException dataAccessException = exceptionTranslator.translateExceptionIfPossible((RuntimeException) throwable);
            if (dataAccessException != null) {
                return dataAccessException;
            }
        }
        return new DataRetrievalFailureException(throwable.getMessage(), throwable);
    }

    private <T, R> Mono<R> execute(Class<T> entityClass, Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
        return execute(spaceName(entityClass), operation);
    }

    private <R> Mono<R> execute(String spaceName, Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
        return Mono.fromFuture(() -> {
            try {
                return operation.apply(spaceOperations(spaceName));
            } catch (Throwable throwable) {
                return CompletableFuture.failedFuture(throwable);
            }
        }).onErrorMap(this::dataAccessException);
    }

    private <R> Mono<R> execute(Supplier<CompletableFuture<R>> supplier) {
        return Mono.fromFuture(() -> {
            try {
                return supplier.get();
            } catch (Throwable throwable) {
                return CompletableFuture.failedFuture(throwable);
            }
        }).onErrorMap(this::dataAccessException);
    }

    private <R> R executeSerial(Supplier<R> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw dataAccessException(e);
        }
    }
}
