package org.springframework.data.tarantool.core;

import io.tarantool.driver.TarantoolVersion;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.operations.TupleOperations;
import io.tarantool.driver.exceptions.TarantoolSpaceOperationException;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.mappers.TarantoolTupleSingleResultMapperFactory;
import io.tarantool.driver.mappers.ValueConverter;
import io.tarantool.driver.metadata.TarantoolSpaceMetadata;
import org.msgpack.value.Value;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.tarantool.core.mapping.event.BeforeSaveCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Primary implementation of {@link TarantoolOperations}
 *
 * @author Alexander Rublev
 */
public class TarantoolTemplate implements ApplicationContextAware, TarantoolOperations {
    private final TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient;
    private final TarantoolConverter tarantoolConverter;
    private final TarantoolExceptionTranslator exceptionTranslator;
    private final MessagePackMapper messagePackMapper;
    private final TarantoolTupleMethodsHelper tupleMethodsHelper;
    private @Nullable EntityCallbacks entityCallbacks;

    public TarantoolTemplate(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient) {
        this(tarantoolClient, TarantoolConverter.newConverter(), new DefaultTarantoolExceptionTranslator());
    }

    public TarantoolTemplate(TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient,
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
            setEntityCallbacks(EntityCallbacks.create(applicationContext));
        }
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
            return (T) entityCallbacks.callback(BeforeConvertCallback.class, object, spaceName);
        }

        return object;
    }

    protected <T> T maybeCallBeforeSave(T object, TarantoolTuple tuple, String spaceName) {
        if (null != entityCallbacks) {
            return (T) entityCallbacks.callback(BeforeSaveCallback.class, object, tuple, spaceName);
        }

        return object;
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
    public <T, ID> List<T> selectByIds(List<ID> ids, Class<T> entityClass) {
        Assert.notNull(ids, "List of ids must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        List<CompletableFuture<TarantoolResult<TarantoolTuple>>> futures = ids.stream().map(id -> {
            Conditions query = tupleMethodsHelper.primaryIndexQueryById(id, entityClass);
            return execute(entityClass, spaceOps -> spaceOps.select(query));
        }).collect(Collectors.toList());

        return unwrap(CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> futures.stream()
                        .map(f -> f.join().stream().findFirst().orElse(null))
                        .collect(Collectors.toList())))
                .stream()
                .map(t -> tupleToEntity(t, entityClass))
                .collect(Collectors.toList());
    }

    @Override
    public <T> T selectOne(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return unwrap(execute(entityClass, spaceOps -> spaceOps.select(query)))
                .stream()
                .filter(tuples -> tuples.size() > 0)
                .findFirst()
                .map(t -> tupleToEntity(t, entityClass))
                .orElse(null);
    }

    @Override
    public <T> List<T> select(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        return unwrap(execute(entityClass, spaceOps -> spaceOps.select(query)))
                .stream()
                .map(t -> tupleToEntity(t, entityClass))
                .collect(Collectors.toList());
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

        TarantoolSpaceMetadata spaceMetadata = spaceMetadata(entityClass).orElseThrow(() -> new MappingException(String.format("Space metadata not found for space %s", spaceName)));
        TarantoolTuple tuple = entityToTuple(maybeCallBeforeConvert(entityToUse, spaceName), messagePackMapper, spaceMetadata);
        maybeCallBeforeSave(entityToUse, tuple, spaceName);

        return unwrap(execute(spaceName, spaceOps -> spaceOps.insert(tuple))).stream()
                .findFirst()
                .map(tuples -> tupleToEntity(tuples, entityClass))
                .orElse(null);
    }

    @Override
    public <T> T replace(T entity, Class<T> entityClass) {
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        T entityToUse = entityToUpdate(entity);

        TarantoolSpaceMetadata spaceMetadata = spaceMetadata(entityClass).orElseThrow(() -> new MappingException(String.format("Space metadata not found for space %s", spaceName)));
        TarantoolTuple tuple = entityToTuple(maybeCallBeforeConvert(entityToUse, spaceName), messagePackMapper, spaceMetadata);
        maybeCallBeforeSave(entityToUse, tuple, spaceName);

        return unwrap(execute(spaceName, spaceOps -> spaceOps.replace(tuple))).stream()
                .findFirst()
                .map(tuples -> tupleToEntity(tuples, entityClass))
                .orElse(null);
    }

    @Override
    public <T> List<T> update(Conditions query, T entity, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        T entityToUse = entityToUpdate(entity);

        TarantoolSpaceMetadata spaceMetadata = spaceMetadata(entityClass).orElseThrow(() -> new MappingException(String.format("Space metadata not found for space %s", spaceName)));
        TarantoolTuple tuple = entityToTuple(maybeCallBeforeConvert(entityToUse, spaceName), messagePackMapper, spaceMetadata);
        maybeCallBeforeSave(entityToUse, tuple, spaceName);

        TupleOperations operations = tupleMethodsHelper.prepareUpdateOperations(tuple);
        TarantoolResult<TarantoolTuple> sr = unwrap(execute(spaceName, spaceOps -> spaceOps.select(query)));
        List<CompletableFuture<TarantoolResult<TarantoolTuple>>> futures = sr.stream().map(t -> {
            Conditions conditions = tupleMethodsHelper.primaryIndexQuery(t, entityClass);
            return execute(spaceName, spaceOps -> spaceOps.update(conditions, operations));
        }).collect(Collectors.toList());

        return unwrap(CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> futures.stream()
                        .map(f -> f.join().stream().findFirst().orElse(null))
                        .collect(Collectors.toList())))
                .stream()
                .map(t -> tupleToEntity(t, entityClass))
                .collect(Collectors.toList());
    }

    @Override
    public <T> T delete(T entity, Class<T> entityClass) {
        Assert.notNull(entity, "Entity must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        Conditions query = tupleMethodsHelper.primaryIndexQuery(entity);
        return unwrap(execute(entityClass, spaceOps -> spaceOps.delete(query))).stream()
                .findFirst()
                .map(tuples -> tupleToEntity(tuples, entityClass))
                .orElse(null);
    }

    @Override
    public <T> List<T> delete(Conditions query, Class<T> entityClass) {
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        String spaceName = spaceName(entityClass);
        return unwrap(execute(spaceName, spaceOps -> spaceOps.select(query))).stream()
                .map(tuple -> {
                    Conditions conditions = tupleMethodsHelper.primaryIndexQuery(tuple, entityClass);
                    return unwrap(execute(spaceName, spaceOps -> spaceOps.delete(conditions)));
                })
                .flatMap(tuples -> tuples.stream().map(t -> tupleToEntity(t, entityClass)))
                .collect(Collectors.toList());
    }

    @Override
    public <T, ID> T deleteById(ID id, Class<T> entityClass) {
        Assert.notNull(id, "Id must not be null");
        Assert.notNull(entityClass, "Entity class must not be null");

        Conditions query = tupleMethodsHelper.primaryIndexQueryById(id, entityClass);
        return unwrap(execute(entityClass, spaceOps -> spaceOps.delete(query))).stream()
                .findFirst()
                .map(tuples -> tupleToEntity(tuples, entityClass))
                .orElse(null);
    }

    @Override
    public <T> boolean truncate(Class<T> entityClass) {
        Assert.notNull(entityClass, "Entity class must not be null");

        if (isProxyClient()) {
            String spaceName = spaceName(entityClass);
            Boolean truncated = unwrap(tarantoolClient.callForSingleResult("crud.truncate", Collections.singletonList(spaceName), Boolean.class));
            if (!truncated) {
                throw new TarantoolSpaceOperationException(String.format("Failed to truncate space %s", spaceName));
            }
            return true;
        }
        throw new UnsupportedOperationException("Truncate operation not supported yet in driver");
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

        Optional<TarantoolSpaceMetadata> spaceMetadata = spaceMetadata(entityClass);
        if (spaceMetadata.isPresent()) {
            TarantoolTupleSingleResultMapperFactory resultMapperFactory = tarantoolClient.getResultMapperFactoryFactory().defaultTupleSingleResultMapperFactory();
            return unwrap(execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper,
                    resultMapperFactory.withDefaultTupleValueConverter(messagePackMapper, spaceMetadata.orElse(null))))).stream()
                    .findFirst()
                    .map(tuples -> tupleToEntity(tuples, entityClass))
                    .orElse(null);
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
        return entityConverter.fromValue(value);
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
        Optional<TarantoolSpaceMetadata> spaceMetadata = spaceMetadata(entityClass);
        if (spaceMetadata.isPresent()) {
            TarantoolTupleSingleResultMapperFactory resultMapperFactory = tarantoolClient.getResultMapperFactoryFactory().defaultTupleSingleResultMapperFactory();
            return unwrap(execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper,
                    resultMapperFactory.withDefaultTupleValueConverter(messagePackMapper, spaceMetadata.orElse(null))))).stream()
                    .map(tuple -> tupleToEntity(tuple, entityClass))
                    .collect(Collectors.toList());
        } else {
            return callForAll(functionName, parameters, value -> tupleToEntity(messagePackMapper.fromValue(value, Map.class), entityClass));
        }
    }

    @Override
    public <T> List<T> callForAll(String functionName, List<?> parameters, ValueConverter<Value, T> entityConverter) {
        ValueConverter<Value, List<Value>> converter = value -> value.isNilValue() ? null : value.asArrayValue().list();
        return unwrap(execute(() -> tarantoolClient.callForSingleResult(functionName, mappedTValues(parameters), messagePackMapper, converter))).stream()
                .map(entityConverter::fromValue)
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<T> callForAll(String functionName, Class<T> entityClass) {
        return callForAll(functionName, Collections.emptyList(), entityClass);
    }

    @Override
    public <T> List<T> callForAll(String functionName, ValueConverter<Value, T> entityConverter) {
        return callForAll(functionName, Collections.emptyList(), entityConverter);
    }

    @Override
    public TarantoolVersion getVersion() {
        return execute(tarantoolClient::getVersion);
    }

    private <T, R> CompletableFuture<R> execute(Class<T> entityClass, Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
        return execute(spaceName(entityClass), operation);
    }

    private <R> CompletableFuture<R> execute(String spaceName, Function<TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>>, CompletableFuture<R>> operation) {
        try {
            return operation.apply(spaceOps(spaceName));
        } catch (Throwable throwable) {
            return CompletableFuture.failedFuture(translateThrowable(throwable));
        }
    }

    private <R> R execute(Supplier<R> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            DataAccessException exception = exceptionTranslator.translateExceptionIfPossible((RuntimeException) e);
            if (exception != null) {
                throw exception;
            } else {
                throw e;
            }
        }
    }

    private <R> R unwrap(Future<R> f) {
        try {
            return f.get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException) {
                DataAccessException dataAccessException = exceptionTranslator.translateExceptionIfPossible((RuntimeException) e.getCause());
                if (dataAccessException != null) {
                    throw dataAccessException;
                }
            }
            throw new DataRetrievalFailureException(e.getMessage(), e.getCause());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Throwable translateThrowable(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            DataAccessException dataAccessException = exceptionTranslator.translateExceptionIfPossible((RuntimeException) throwable);
            if (dataAccessException != null) {
                return dataAccessException;
            }
        }
        return throwable;
    }

    private <T> Optional<TarantoolSpaceMetadata> spaceMetadata(Class<T> entityClass) {
        TarantoolPersistentEntity<?> entityMetadata = tarantoolConverter.getMappingContext().getPersistentEntity(entityClass);
        return entityMetadata != null ? execute(() -> tarantoolClient.metadata().getSpaceByName(entityMetadata.getSpaceName())) : Optional.empty();
    }

}
