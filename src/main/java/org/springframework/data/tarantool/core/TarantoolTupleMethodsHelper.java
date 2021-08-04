package org.springframework.data.tarantool.core;

import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.api.tuple.TarantoolNullField;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.api.tuple.operations.TupleOperations;
import io.tarantool.driver.protocol.TarantoolIndexQuery;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Common class to accumulate methods to create TarantoolClient {@link Conditions} an {@link TupleOperations}
 *
 * @author Alexander Rublev
 */
public class TarantoolTupleMethodsHelper {
    private final TarantoolConverter tarantoolConverter;
    private final TarantoolConverterAware tarantoolConverterAware;

    /**
     * Create new IndexQueryCreator
     *
     * @param tarantoolConverter      tarantool converter to use
     * @param tarantoolConverterAware helper class which should be aware of converter
     */
    public TarantoolTupleMethodsHelper(TarantoolConverter tarantoolConverter, TarantoolConverterAware tarantoolConverterAware) {
        Assert.notNull(tarantoolConverter, "TarantoolConverter must not be null");
        Assert.notNull(tarantoolConverterAware, "TarantoolConverterAware must not be null");

        this.tarantoolConverter = tarantoolConverter;
        this.tarantoolConverterAware = tarantoolConverterAware;
    }

    /**
     * Create Tarantool Driver {@link Conditions}, which are should be primary index query
     *
     * @param tuple       TarantoolTuple to use
     * @param entityClass provided entity class
     * @return primary index query {@link Conditions}
     */
    public Conditions primaryIndexQuery(TarantoolTuple tuple, Class<?> entityClass) {
        List<Object> indexParts = new ArrayList<>();
        TarantoolPersistentEntity<?> persistentEntity = tarantoolConverter.getMappingContext().getRequiredPersistentEntity(entityClass);
        if (persistentEntity.hasCompositePrimaryKey()) {
            TarantoolPersistentProperty idProperty = persistentEntity.getRequiredIdProperty();
            TarantoolPersistentEntity<?> idPersistentEntity = tarantoolConverter.getMappingContext().getRequiredPersistentEntity(idProperty.getType());

            idPersistentEntity.forEach(property -> {
                Object value = tuple.getObject(property.getFieldName())
                        .orElseThrow(() -> new MappingException(String.format("Id property field %s not found in tuple", property.getFieldName())));
                indexParts.add(value);
            });
        } else if (!persistentEntity.hasIdProperty()) {
            persistentEntity.forEach(property -> {
                if (property.isPrimaryKeyField()) {
                    Object value = tuple.getObject(property.getFieldName())
                            .orElseThrow(() -> new MappingException(String.format("Id property field %s not found in tuple", property.getFieldName())));
                    indexParts.add(value);
                }
            });
        } else {
            TarantoolPersistentProperty idProperty = persistentEntity.getRequiredIdProperty();
            Object value = tuple.getObject(idProperty.getFieldName())
                    .orElseThrow(() -> new MappingException(String.format("Id property %s not found in tuple", idProperty.getFieldName())));
            indexParts.add(value);
        }
        if (indexParts.isEmpty()) {
            throw new MappingException(String.format("Can't retrieve id fields for query for entity %s", persistentEntity.getType().getSimpleName()));
        }
        return Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, indexParts);
    }

    /**
     * Create Tarantool Driver {@link Conditions}, which are should be primary index query
     *
     * @param source entity object
     * @param <T>    entity class parameter
     * @return primary index query {@link Conditions}
     */
    public <T> Conditions primaryIndexQuery(T source) {
        TarantoolPersistentEntity<?> persistentEntity = tarantoolConverter.getMappingContext().getRequiredPersistentEntity(source.getClass());
        Object id = persistentEntity.getIdentifierAccessor(source).getRequiredIdentifier();
        return primaryIndexQuery(id, persistentEntity);
    }

    /**
     * Create Tarantool Driver {@link Conditions}, which are should be primary index query
     *
     * @param id          identifier property value for entity
     * @param entityClass provided entity class
     * @param <ID>        identifier class parameter
     * @return primary index query {@link Conditions}
     */
    public <ID> Conditions primaryIndexQueryById(ID id, Class<?> entityClass) {
        TarantoolPersistentEntity<?> persistentEntity = tarantoolConverter.getMappingContext().getRequiredPersistentEntity(entityClass);
        return primaryIndexQuery(id, persistentEntity);
    }

    /**
     * Create Tarantool Driver {@link Conditions}, which are should be primary index query
     *
     * @param id               identifier property value for entity
     * @param persistentEntity TarantoolPersistentEntity entity
     * @param <ID>             identifier class parameter
     * @return primary index query {@link Conditions}
     */
    public <ID> Conditions primaryIndexQuery(ID id, TarantoolPersistentEntity<?> persistentEntity) {
        List<Object> indexParts = new ArrayList<>();
        if (persistentEntity.hasCompositePrimaryKey()) {
            TarantoolPersistentEntity<?> idPersistentEntity = tarantoolConverter.getMappingContext().getRequiredPersistentEntity(id.getClass());
            ConvertingPropertyAccessor<ID> idPropertyAccessor = new ConvertingPropertyAccessor<>(idPersistentEntity.getPropertyAccessor(id), tarantoolConverter.getConversionService());
            idPersistentEntity.forEach(property -> indexParts.add(idPropertyAccessor.getProperty(property)));
        } else if (id instanceof MapId) {
            MapId mapId = (MapId) id;
            persistentEntity.forEach(property -> {
                if (property.isPrimaryKeyField()) {
                    indexParts.add(mapId.get(property.getName()));
                }
            });
        } else {
            indexParts.add(id);
        }
        if (indexParts.isEmpty()) {
            throw new MappingException(String.format("Can't retrieve id fields for query for entity %s", persistentEntity.getType().getSimpleName()));
        }
        return Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, tarantoolConverterAware.mappedTValues(indexParts));
    }

    /**
     * Prepare Tarantool TupleOperations to use for update
     *
     * @param tuple source tuple
     * @return prepared TupleOperations instance
     */
    public TupleOperations prepareUpdateOperations(TarantoolTuple tuple) {
        AtomicReference<TupleOperations> operations = new AtomicReference<>();
        TupleOperations.fromTarantoolTuple(tuple).asList().stream()
                .filter(operation -> !(operation.getValue() instanceof TarantoolNullField))
                .forEach(operation -> {
                    if (operations.get() == null) {
                        operations.set(TupleOperations.set(operation.getFieldIndex(), operation.getValue()));
                    } else {
                        operations.get().addOperation(operation);
                    }
                });
        return operations.get();
    }

}
