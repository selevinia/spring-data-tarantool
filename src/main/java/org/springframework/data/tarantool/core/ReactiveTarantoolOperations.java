package org.springframework.data.tarantool.core;

import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.mappers.ValueConverter;
import org.msgpack.value.Value;
import org.reactivestreams.Publisher;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Interface that specifies a basic set of Reactive Tarantool operations. Implemented by {@link ReactiveTarantoolTemplate}.
 *
 * @author Tatiana Blinova
 * @author Alexander Rublev
 */
public interface ReactiveTarantoolOperations extends TarantoolConverterAware, TarantoolClientAware {

    /**
     * Get an entity by the given id and map it to an object of the given type.
     * Target space will be derived automatically from the entity class.
     * Default converter {@link MappingTarantoolConverter} will be used unless a custom one is specified.
     *
     * @param <T>         target entity type
     * @param <ID>        target entity index type
     * @param id          Entity identifier
     * @param entityClass Desired type of the result object
     * @return The converted object
     */
    <T, ID> Mono<T> selectById(ID id, Class<T> entityClass);

    /**
     * Get an entities by the given ids and map them to objects of the given type.
     * Target space will be derived automatically from the entity class.
     * Default converter {@link MappingTarantoolConverter} will be used unless a custom one is specified.
     *
     * @param <T>         target entity type
     * @param <ID>        target entity index type
     * @param ids         Entity identifier
     * @param entityClass Desired type of the result object
     * @return The converted object
     */
    <T, ID> Flux<T> selectByIds(Publisher<ID> ids, Class<T> entityClass);

    /**
     * Map the results of a query over a space for the entity class to a single instance of an object of the
     * specified type. Target space will be derived automatically from the entity class.
     * Default value mappers {@link MappingTarantoolConverter} will be used unless a custom one is specified.
     *
     * @param <T>         target entity type
     * @param query       Query object that encapsulates the search criteria
     * @param entityClass Desired type of the result object
     * @return The converted object
     */
    <T> Mono<T> selectOne(Conditions query, Class<T> entityClass);

    /**
     * Map the results of a query over a space for the entity class to a Flux of objects of the specified type.
     * Target space will be derived automatically from the entity class.
     * Default value mappers {@link MappingTarantoolConverter} will be used unless a custom one is specified.
     *
     * @param <T>         target entity type
     * @param query       Query object that encapsulates the search criteria
     * @param entityClass Desired type of the result object
     * @return The Flux of converted objects
     */
    <T> Flux<T> select(Conditions query, Class<T> entityClass);

    /**
     * Get all entities from a space and map them to a Flux of objects of specified type. The space is determined automatically
     * from the entity class.
     * Default converter {@link MappingTarantoolConverter} will be used unless a custom one is specified.
     *
     * @param <T>         target entity type
     * @param entityClass Desired type of the result object
     * @return The Flux of converted objects
     */
    <T> Flux<T> select(Class<T> entityClass);

    /**
     * Count the number of records matching the specified query. The space is determined automatically
     * from the entity class.
     *
     * @param <T>         target entity type
     * @param query       Query object that encapsulates the search criteria
     * @param entityClass Desired type of the result object
     * @return Number of records
     */
    <T> Mono<Long> count(Conditions query, Class<T> entityClass);

    /**
     * Count the number of records in space. The space is determined automatically
     * from the entity class.
     *
     * @param <T>         target entity type
     * @param entityClass Desired type of the result object
     * @return Number of records
     */
    <T> Mono<Long> count(Class<T> entityClass);

    /**
     * Insert a record into a space. The space is determined automatically by the entity class.
     *
     * @param <T>         target entity type
     * @param entity      The object to save
     * @param entityClass Desired type of the result object
     * @return The inserted object
     */
    <T> Mono<T> insert(T entity, Class<T> entityClass);

    /**
     * Replace a record into a space. The space is determined automatically by the entity class. If the record doesn't
     * exist, it will be inserted.
     *
     * @param <T>         target entity type
     * @param entity      The object to save
     * @param entityClass Desired type of the result object
     * @return The inserted object
     */
    <T> Mono<T> replace(T entity, Class<T> entityClass);

    /**
     * Update all records selected by the specified conditions. The space is determined automatically by the
     * entity class. Warning: executing this operation on a large data set may cause OutOfMemory error or take
     * significant time to complete.
     *
     * @param query       tuple selection conditions
     * @param entity      entity with new data for update
     * @param entityClass target class of the result objects
     * @param <T>         target entity type
     * @return Flux of updated objects
     */
    <T> Flux<T> update(Conditions query, T entity, Class<T> entityClass);

    /**
     * Remove a record from a space corresponding to the specified entity type.
     *
     * @param <T>         target entity type
     * @param entity      Target entity (must have the id property)
     * @param entityClass Desired type of the result object
     * @return Removed entity value
     */
    <T> Mono<T> delete(T entity, Class<T> entityClass);

    /**
     * Map the results of a query over a space for the entity class to a Flux of objects of the specified type. All entities
     * found are returned and removed from the space. Target space will be derived automatically from the entity class.
     * Default converter {@link MappingTarantoolConverter} will be used unless a custom one is specified.
     *
     * @param <T>         target entity type
     * @param query       Query object that encapsulates the search criteria
     * @param entityClass Desired type of the result object
     * @return The Flux of converted objects
     */
    <T> Flux<T> delete(Conditions query, Class<T> entityClass);

    /**
     * Remove a record from a space corresponding to the specified entity type.
     *
     * @param <T>         target entity type
     * @param <ID>        target entity index type
     * @param id          Target entity ID
     * @param entityClass Desired type of the result object
     * @return Removed entity value
     */
    <T, ID> Mono<T> deleteById(ID id, Class<T> entityClass);

    /**
     * Truncate space (remove all data records in the space on each node where it persists).
     * The space is determined automatically from the entity class.
     *
     * @param entityClass Desired type of the result object
     * @param <T> target entity type
     * @return {@code true} if space truncated, otherwise {@code Mono.error}
     */
    <T> Mono<Boolean> truncate(Class<T> entityClass);

    /**
     * Call a function defined in Tarantool instance API which returns one entity as result.
     *
     * @param <T>          target entity type
     * @param functionName callable API function name
     * @param parameters   function parameters
     * @param entityClass  Desired type of the result object
     * @return function call result
     */
    <T> Mono<T> call(String functionName, Object[] parameters, Class<T> entityClass);

    /**
     * Call a function defined in Tarantool instance API which returns some MessagePack value as result. The given
     * entity converter will be used for converting the result value into an entity.
     *
     * @param <T>             target entity type
     * @param functionName    callable API function name
     * @param parameters      function parameters
     * @param entityConverter converter from MessagePack value to the result entity type
     * @return function call result
     */
    <T> Mono<T> call(String functionName, Object[] parameters, ValueConverter<Value, T> entityConverter);

    /**
     * Call a function defined in Tarantool instance API which returns one entity as result.
     *
     * @param <T>          target entity type
     * @param functionName callable API function name
     * @param parameters   function parameters
     * @param entityClass  Desired type of the result object
     * @return function call result
     */
    <T> Mono<T> call(String functionName, List<?> parameters, Class<T> entityClass);

    /**
     * Call a function defined in Tarantool instance API which returns some MessagePack value as result. The given
     * entity converter will be used for converting the result value into an entity.
     *
     * @param <T>             target entity type
     * @param functionName    callable API function name
     * @param parameters      function parameters
     * @param entityConverter converter from MessagePack value to the result entity type
     * @return function call result
     */
    <T> Mono<T> call(String functionName, List<?> parameters, ValueConverter<Value, T> entityConverter);

    /**
     * Call a function defined in Tarantool instance API which returns one entity as result.
     *
     * @param <T>          target entity type
     * @param functionName callable API function name
     * @param entityClass  Desired type of the result object
     * @return function call result
     */
    <T> Mono<T> call(String functionName, Class<T> entityClass);

    /**
     * Call a function defined in Tarantool instance API which returns some MessagePack value as result. The given
     * entity converter will be used for converting the result value into an entity.
     *
     * @param <T>             target entity type
     * @param functionName    callable API function name
     * @param entityConverter converter from MessagePack value to the result entity type
     * @return function call result
     */
    <T> Mono<T> call(String functionName, ValueConverter<Value, T> entityConverter);

    /**
     * Call a function defined in Tarantool instance API which returns a list of entities as result.
     *
     * @param <T>          target entity type
     * @param functionName callable API function name
     * @param parameters   function parameters
     * @param entityClass  Desired type of the result object
     * @return function call result
     */
    <T> Flux<T> callForAll(String functionName, Object[] parameters, Class<T> entityClass);

    /**
     * Call a function defined in Tarantool instance API which returns a list of MessagePack values as result. The given
     * entity converter will be used for converting each value in the result into an entity.
     *
     * @param <T>             target entity type
     * @param functionName    callable API function name
     * @param parameters      function parameters
     * @param entityConverter converter from MessagePack value to the result entity type
     * @return function call result
     */
    <T> Flux<T> callForAll(String functionName, Object[] parameters, ValueConverter<Value, T> entityConverter);

    /**
     * Call a function defined in Tarantool instance API which returns a list of entities as result.
     *
     * @param <T>          target entity type
     * @param functionName callable API function name
     * @param parameters   function parameters
     * @param entityClass  Desired type of the result object
     * @return function call result
     */
    <T> Flux<T> callForAll(String functionName, List<?> parameters, Class<T> entityClass);

    /**
     * Call a function defined in Tarantool instance API which returns a list of MessagePack values as result. The given
     * entity converter will be used for converting each value in the result into an entity.
     *
     * @param <T>             target entity type
     * @param functionName    callable API function name
     * @param parameters      function parameters
     * @param entityConverter converter from MessagePack value to the result entity type
     * @return function call result
     */
    <T> Flux<T> callForAll(String functionName, List<?> parameters, ValueConverter<Value, T> entityConverter);

    /**
     * Call a function defined in Tarantool instance API which returns a list of entities as result.
     *
     * @param <T>          target entity type
     * @param functionName callable API function name
     * @param entityClass  Desired type of the result object
     * @return function call result
     */
    <T> Flux<T> callForAll(String functionName, Class<T> entityClass);

    /**
     * Call a function defined in Tarantool instance API which returns a list of MessagePack values as result. The given
     * entity converter will be used for converting each value in the result into an entity.
     *
     * @param <T>             target entity type
     * @param functionName    callable API function name
     * @param entityConverter converter from MessagePack value to the result entity type
     * @return function call result
     */
    <T> Flux<T> callForAll(String functionName, ValueConverter<Value, T> entityConverter);

}
