package org.springframework.data.tarantool.core;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.TarantoolTupleImpl;
import io.tarantool.driver.mappers.MessagePackMapper;
import io.tarantool.driver.mappers.ValueConverter;
import io.tarantool.driver.metadata.TarantoolSpaceMetadata;
import org.msgpack.value.Value;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.TarantoolSimpleTypeHolder;

import java.util.List;
import java.util.Map;

/**
 * Common interface to accumulate methods to interact with TarantoolConverter
 *
 * @author Alexander Rublev
 */
public interface TarantoolConverterAware {

    /**
     * Return the entity converter used for this instance
     *
     * @return entity converter
     */
    TarantoolConverter getConverter();

    /**
     * Determine name of Tarantool space
     *
     * @param entityClass entity class to use
     * @param <T>         entity class parameter
     * @return name of space
     */
    default <T> String spaceName(Class<T> entityClass) {
        return getConverter().getMappingContext().getRequiredPersistentEntity(entityClass).getSpaceName();
    }

    /**
     * Convert entity to Tarantool tuple
     *
     * @param entity        entity instance to convert
     * @param mapper        Tarantool MessagePackMapper mapper to use
     * @param spaceMetadata Tarantool Space Metadata for corresponding entity
     * @param <T>           entity class parameter
     * @return Tarantool tuple
     */
    default <T> TarantoolTuple entityToTuple(T entity, MessagePackMapper mapper, TarantoolSpaceMetadata spaceMetadata) {
        TarantoolTuple tuple = new TarantoolTupleImpl(mapper, spaceMetadata);
        getConverter().write(entity, tuple);
        return tuple;
    }

    /**
     * Convert Tarantool tuple to entity
     *
     * @param tuple       Tarantool tuple to convert
     * @param entityClass entity class to use
     * @param <T>         entity class parameter
     * @return converted entity
     */
    default <T> T tupleToEntity(Object tuple, Class<T> entityClass) {
        return getConverter().read(entityClass, tuple);
    }

    /**
     * Convert multiple values to Tarantool writable objects
     *
     * @param values list of values to convert
     * @return list of Tarantool objects
     */
    default List<?> mappedTValues(List<?> values) {
        return (List<?>) getConverter().convertToWritableType(values);
    }

    /**
     * Prepare entity to use in insert operation
     *
     * @param entity just entity to prepare
     * @param <T>    entity class parameter
     * @return prepared entity
     */
    default <T> T entityToInsert(T entity) {
        AdaptableEntity<T> source = AdaptableMappedEntity.of(entity, getConverter().getMappingContext(), getConverter().getConversionService());
        return source.isVersionedEntity() ? source.initializeVersionProperty() : entity;
    }

    /**
     * Prepare entity to use in update operation
     *
     * @param entity just entity to prepare
     * @param <T>    entity class parameter
     * @return prepared entity
     */
    default <T> T entityToUpdate(T entity) {
        AdaptableEntity<T> source = AdaptableMappedEntity.of(entity, getConverter().getMappingContext(), getConverter().getConversionService());
        if (source.isVersionedEntity()) {
            return source.incrementVersion();
        } else {
            return entity;
        }
    }

    /**
     * Prepare Tarantool value converter to use in call operations
     *
     * @param mapper      Tarantool MessagePackMapper mapper to use
     * @param entityClass entity class to use
     * @param <T>         entity class parameter
     * @return value converter
     */
    default <T> ValueConverter<Value, T> valueConverter(MessagePackMapper mapper, Class<T> entityClass) {
        if (TarantoolSimpleTypeHolder.HOLDER.isSimpleType(entityClass)) {
            return value -> mapper.fromValue(value, entityClass);
        } else {
            return value -> tupleToEntity(mapper.fromValue(value, Map.class), entityClass);
        }
    }
}
