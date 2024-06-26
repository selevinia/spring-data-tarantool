package org.springframework.data.tarantool.core;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.metadata.TarantoolSpaceMetadata;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.operations.TupleOperations;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.mappers.converters.ValueConverter;
import io.tarantool.driver.mappers.factories.SingleValueWithTarantoolTupleResultMapperFactory;
import org.msgpack.value.Value;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.tarantool.TarantoolServerConnectionException;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.event.*;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Primary implementation of {@link TarantoolOperations}
 *
 * @author Alexander Rublev
 */
public class TarantoolTemplate extends ExceptionTranslatorSupport implements ApplicationContextAware, ApplicationEventPublisherAware, TarantoolOperations {
    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
    private final TarantoolConverter tarantoolConverter;
    private final MessagePackMapper messagePackMapper;
    private final TarantoolTupleMethodsHelper tupleMethodsHelper;
    private @Nullable
    EntityCallbacks entityCallbacks;
    private @Nullable
    ApplicationEventPublisher eventPublisher;

    public TarantoolTemplate(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
        this(tarantoolClient, MappingTarantoolConverter.newConverter(), new DefaultTarantoolExceptionTranslator());
    }

    public TarantoolTemplate(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
                             TarantoolConverter tarantoolConverter,
                             TarantoolExceptionTranslator exceptionTranslator) {
        super(exceptionTranslator);
        this.tarantoolClient = tarantoolClient;
        this.tarantoolConverter = tarantoolConverter;
        this.messagePackMapper = tarantoolClient.getConfig().getMessagePackMapper();
        this.tupleMethodsHelper = new TarantoolTupleMethodsHelper(tarantoolConverter, this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (entityCallbacks == null) {
            setEntityCallbacks(EntityCallbacks.create(applicationContext));
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    /**
     * Configure {@link EntityCallbacks} to pre-/post-process entities during persistence operations.
     *
     * @param entityCallbacks context wide callbacks
     */
    public void setEntityCallbacks(@Nullable EntityCallbacks entityCallbacks) {
        this.entityCallbacks = entityCallbacks;
    }

    protected <T> T maybeCallBeforeConvert(T object, String spaceName) {
        if (null != entityCallbacks) {
            return entityCallbacks.callback(BeforeConvertCallback.class, object, spaceName);
        }

        return object;
    }

    protected <T> T maybeCallBeforeSave(T object, TarantoolTuple tuple, String spaceName) {
        if (null != entityCallbacks) {
            return entityCallbacks.callback(BeforeSaveCallback.class, object, tuple, spaceName);
        }

        return object;
    }

    protected <E extends TarantoolMappingEvent<T>, T> void maybeEmitEvent(E event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
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
    public <T, ID> T selectById(ID id, Class<T> entityClass) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        Conditions query = tupleMethodsHelper.primaryIndexQueryById(id, entityClass);
        return selectOne(query, entityClass);
    }

    @Override
    public <T, ID> List<T> selectByIds(Iterable<ID> ids, Class<T> entityClass) {
        Assert.notNull(ids, "List of ids must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        List<CompletableFuture<TarantoolResult<TarantoolTuple>>> futures = StreamSupport.stream(ids.spliterator(), false).map(id -> {
            Conditions query = tupleMethodsHelper.primaryIndexQueryById(id, entityClass);
            return execute(entityClass, spaceOps -> spaceOps.select(query));
        }).collect(Collectors.toList());

        return unwrap(CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> futures.stream()
                        .map(f -> f.join().stream().findFirst().orElse(null))
                        .collect(Collectors.toList())))
                .stream()
                .map(t -> t != null ? mapToEntity(t, entityClass) : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public <T> T selectOne(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return unwrap(execute(entityClass, spaceOps -> spaceOps.select(query)))
                .stream()
                .findFirst()
                .map(t -> mapToEntity(t, entityClass))
                .orElse(null);
    }

    @Override
    public <T> List<T> select(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return unwrap(execute(entityClass, spaceOps -> spaceOps.select(query)))
                .stream()
                .map(t -> mapToEntity(t, entityClass))
                .collect(Collectors.toList());
    }

    private <T> T mapToEntity(TarantoolTuple tuple, Class<T> entityClass) {
        String spaceName = spaceName(entityClass);
        maybeEmitEvent(new AfterLoadEvent<>(tuple, entityClass, spaceName));
        T entity = tupleToEntity(tuple, entityClass);
        maybeEmitEvent(new AfterConvertEvent<>(tuple, entity, spaceName));
        return entity;
    }

    @Override
    public <T> List<T> select(Class<T> entityClass) {
        return select(Conditions.any(), entityClass);
    }

    @Override
    public <T> Long count(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return Optional.of(unwrap(execute(entityClass, spaceOps -> spaceOps.select(query))))
                .map(tuples -> (long) tuples.size())
                .orElse(0L);
    }

    @Override
    public <T> Long count(Class<T> entityClass) {
        return count(Conditions.any(), entityClass);
    }

    @Override
    public <T> T insert(T entity, Class<T> entityClass) {
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        T entityToUse = entityToInsert(entity);

        TarantoolSpaceMetadata spaceMetadata = requiredSpaceMetadata(spaceName);
        TarantoolTuple tuple = entityToTuple(maybeCallBeforeConvert(entityToUse, spaceName), messagePackMapper, spaceMetadata);
        maybeCallBeforeSave(entityToUse, tuple, spaceName);
        maybeEmitEvent(new BeforeSaveEvent<>(entityToUse, spaceName));

        return unwrap(execute(spaceName, spaceOps -> spaceOps.insert(tuple)))
                .stream()
                .findFirst()
                .map(t -> {
                    maybeEmitEvent(new AfterSaveEvent<>(entityToUse, spaceName));
                    return tupleToEntity(t, entityClass);
                })
                .orElse(null);
    }

    @Override
    public <T> T replace(T entity, Class<T> entityClass) {
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        T entityToUse = entityToUpdate(entity);

        TarantoolSpaceMetadata spaceMetadata = requiredSpaceMetadata(spaceName);
        TarantoolTuple tuple = entityToTuple(maybeCallBeforeConvert(entityToUse, spaceName), messagePackMapper, spaceMetadata);
        maybeCallBeforeSave(entityToUse, tuple, spaceName);
        maybeEmitEvent(new BeforeSaveEvent<>(entityToUse, spaceName));

        return unwrap(execute(spaceName, spaceOps -> spaceOps.replace(tuple)))
                .stream()
                .findFirst()
                .map(t -> {
                    maybeEmitEvent(new AfterSaveEvent<>(entityToUse, spaceName));
                    return tupleToEntity(t, entityClass);
                })
                .orElse(null);
    }

    @Override
    public <T> List<T> update(Conditions query, T entity, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        T entityToUse = entityToUpdate(entity);

        TarantoolSpaceMetadata spaceMetadata = requiredSpaceMetadata(spaceName);
        TarantoolTuple tuple = entityToTuple(maybeCallBeforeConvert(entityToUse, spaceName), messagePackMapper, spaceMetadata);
        maybeCallBeforeSave(entityToUse, tuple, spaceName);
        maybeEmitEvent(new BeforeSaveEvent<>(entityToUse, spaceName));

        TupleOperations operations = tupleMethodsHelper.prepareUpdateOperations(tuple);
        TarantoolResult<TarantoolTuple> sr = unwrap(execute(spaceName, spaceOps -> spaceOps.select(query)));
        List<CompletableFuture<TarantoolResult<TarantoolTuple>>> futures = sr.stream().map(t -> {
            Conditions conditions = tupleMethodsHelper.primaryIndexQuery(t, entityClass);
            return execute(spaceName, spaceOps -> spaceOps.update(conditions, operations));
        }).collect(Collectors.toList());

        return unwrap(CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> {
                    maybeEmitEvent(new AfterSaveEvent<>(entityToUse, spaceName));
                    return futures.stream()
                            .map(f -> f.join().stream().findFirst().orElse(null))
                            .collect(Collectors.toList());
                }))
                .stream()
                .map(t -> t != null ? tupleToEntity(t, entityClass) : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public <T> T delete(T entity, Class<T> entityClass) {
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        Conditions query = tupleMethodsHelper.primaryIndexQuery(entity);
        return unwrap(execute(entityClass, spaceOps -> spaceOps.delete(query)))
                .stream()
                .findFirst()
                .map(t -> {
                    maybeEmitEvent(new AfterDeleteEvent<>(t, entityClass, spaceName(entityClass)));
                    return tupleToEntity(t, entityClass);
                })
                .orElse(null);
    }

    @Override
    public <T> List<T> delete(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        return unwrap(execute(spaceName, spaceOps -> spaceOps.select(query)))
                .stream()
                .map(tuple -> {
                    Conditions conditions = tupleMethodsHelper.primaryIndexQuery(tuple, entityClass);
                    return unwrap(execute(spaceName, spaceOps -> spaceOps.delete(conditions)));
                })
                .map(tuples -> tuples.stream()
                        .findFirst()
                        .map(t -> {
                            maybeEmitEvent(new AfterDeleteEvent<>(t, entityClass, spaceName));
                            return tupleToEntity(t, entityClass);
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public <T, ID> T deleteById(ID id, Class<T> entityClass) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        Conditions query = tupleMethodsHelper.primaryIndexQueryById(id, entityClass);
        return unwrap(execute(entityClass, spaceOps -> spaceOps.delete(query)))
                .stream()
                .findFirst()
                .map(t -> {
                    maybeEmitEvent(new AfterDeleteEvent<>(t, entityClass, spaceName(entityClass)));
                    return tupleToEntity(t, entityClass);
                })
                .orElse(null);
    }

    @Override
    public <T> boolean truncate(Class<T> entityClass) {
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        unwrap(tarantoolClient.space(spaceName).truncate());
        return true;
    }

    @Override
    public <T> T call(String functionName, Object[] parameters, Class<T> entityClass) {
        return call(functionName, Arrays.asList(parameters), entityClass);
    }

    @Override
    public <T> T call(String functionName, Object[] parameters, ValueConverter<Value, T> entityConverter) {
        return call(functionName, Arrays.asList(parameters), entityConverter);
    }

    @Override
    public <T> T call(String functionName, List<?> parameters, Class<T> entityClass) {
        Assert.hasText(functionName, "Function name must not be null or empty");
        Assert.notNull(parameters, "Parameters must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        TarantoolPersistentEntity<?> entityMetadata = tarantoolConverter.getMappingContext().getPersistentEntity(entityClass);
        Optional<TarantoolSpaceMetadata> spaceMetadata = entityMetadata != null ? spaceMetadata(entityMetadata.getSpaceName()) : Optional.empty();
        if (spaceMetadata.isPresent()) {
            SingleValueWithTarantoolTupleResultMapperFactory resultMapperFactory = tarantoolClient.getResultMapperFactoryFactory().singleValueTupleResultMapperFactory();
            TarantoolResult<TarantoolTuple> result = unwrap(execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper,
                    resultMapperFactory.withSingleValueArrayToTarantoolTupleResultMapper(messagePackMapper, spaceMetadata.orElse(null)))));
            if (result != null) {
                return result.stream()
                        .findFirst()
                        .map(t -> tupleToEntity(t, entityClass))
                        .orElse(null);
            } else {
                return null;
            }
        } else {
            return call(functionName, parameters, valueConverter(messagePackMapper, entityClass));
        }
    }

    @Override
    public <T> T call(String functionName, List<?> parameters, ValueConverter<Value, T> entityConverter) {
        Assert.hasText(functionName, "Function name must not be null or empty");
        Assert.notNull(parameters, "Parameters must not be null");
        Assert.notNull(entityConverter, "Entity converter must not be null");

        ValueConverter<Value, Value> converter = value -> value.isNilValue() ? null : value;
        Value value = unwrap(execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper, converter)));
        return value != null ? entityConverter.fromValue(value) : null;
    }

    @Override
    public <T> T call(String functionName, Class<T> entityClass) {
        return call(functionName, Collections.emptyList(), entityClass);
    }

    @Override
    public <T> T call(String functionName, ValueConverter<Value, T> entityConverter) {
        return call(functionName, Collections.emptyList(), entityConverter);
    }

    @Override
    public <T> List<T> callForAll(String functionName, Object[] parameters, Class<T> entityClass) {
        return callForAll(functionName, Arrays.asList(parameters), entityClass);
    }

    @Override
    public <T> List<T> callForAll(String functionName, Object[] parameters, ValueConverter<Value, T> entityConverter) {
        return callForAll(functionName, Arrays.asList(parameters), entityConverter);
    }

    @Override
    public <T> List<T> callForAll(String functionName, List<?> parameters, Class<T> entityClass) {
        TarantoolPersistentEntity<?> entityMetadata = tarantoolConverter.getMappingContext().getPersistentEntity(entityClass);
        Optional<TarantoolSpaceMetadata> spaceMetadata = entityMetadata != null ? spaceMetadata(entityMetadata.getSpaceName()) : Optional.empty();
        if (spaceMetadata.isPresent()) {
            SingleValueWithTarantoolTupleResultMapperFactory resultMapperFactory = tarantoolClient.getResultMapperFactoryFactory().singleValueTupleResultMapperFactory();
            TarantoolResult<TarantoolTuple> result = unwrap(execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper,
                    resultMapperFactory.withSingleValueArrayToTarantoolTupleResultMapper(messagePackMapper, spaceMetadata.orElse(null)))));
            if (result != null) {
                return result.stream()
                        .map(tuple -> tupleToEntity(tuple, entityClass))
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        } else {
            return callForAll(functionName, parameters, value -> tupleToEntity(messagePackMapper.fromValue(value, Map.class), entityClass));
        }
    }

    @Override
    public <T> List<T> callForAll(String functionName, List<?> parameters, ValueConverter<Value, T> entityConverter) {
        ValueConverter<Value, List<Value>> converter = value -> value.isNilValue() ? null : value.asArrayValue().list();
        List<Value> result = unwrap(execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper, converter)));
        if (result != null) {
            return result.stream()
                    .map(entityConverter::fromValue)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public <T> List<T> callForAll(String functionName, Class<T> entityClass) {
        return callForAll(functionName, Collections.emptyList(), entityClass);
    }

    @Override
    public <T> List<T> callForAll(String functionName, ValueConverter<Value, T> entityConverter) {
        return callForAll(functionName, Collections.emptyList(), entityConverter);
    }

    private <T, R> CompletableFuture<R> execute(Class<T> entityClass, Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
        return execute(spaceName(entityClass), operation);
    }

    private <R> CompletableFuture<R> execute(String spaceName, Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
        try {
            return operation.apply(spaceOperations(spaceName));
        } catch (Throwable throwable) {
            return CompletableFuture.failedFuture(dataAccessException(throwable));
        }
    }

    private <R> R execute(Supplier<R> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw dataAccessException(e);
        }
    }

    private <R> R unwrap(Future<R> f) {
        try {
            return f.get();
        } catch (ExecutionException e) {
            throw dataAccessException(e.getCause());
        } catch (InterruptedException e) {
            throw new TarantoolServerConnectionException(e.getMessage(), e);
        }
    }
}
