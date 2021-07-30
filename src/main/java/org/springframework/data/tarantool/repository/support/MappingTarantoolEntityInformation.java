package org.springframework.data.tarantool.repository.support;

import org.springframework.data.repository.core.support.PersistentEntityInformation;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.util.Assert;

/**
 * Tarantool-specific entity metadata
 *
 * @author Alexander Rublev
 */
public class MappingTarantoolEntityInformation<T, ID> extends PersistentEntityInformation<T, ID> implements TarantoolEntityInformation<T, ID> {

    private final TarantoolPersistentEntity<T> persistentEntity;

    public MappingTarantoolEntityInformation(TarantoolPersistentEntity<T> persistentEntity) {
        super(persistentEntity);
        this.persistentEntity = persistentEntity;
    }

    @Override
    public String getIdAttribute() {
        return persistentEntity.getRequiredIdProperty().getName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ID getId(T entity) {
        Assert.notNull(entity, "Entity must not be null");
        return (ID) persistentEntity.getIdentifierAccessor(entity).getIdentifier();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<ID> getIdType() {
        if (persistentEntity.hasIdProperty()) {
            return (Class<ID>) persistentEntity.getRequiredIdProperty().getType();
        }
        return (Class<ID>) MapId.class;
    }

    @Override
    public String getSpaceName() {
        return persistentEntity.getSpaceName();
    }
}
