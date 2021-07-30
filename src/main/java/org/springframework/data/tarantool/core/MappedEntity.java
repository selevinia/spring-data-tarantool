package org.springframework.data.tarantool.core;

import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.lang.Nullable;

/**
 * Implementation of representation of information about an entity.
 *
 * @author Alexander Rublev
 */
public class MappedEntity<T> implements Entity<T> {
    protected final TarantoolPersistentEntity<?> entity;
    protected final PersistentPropertyAccessor<T> propertyAccessor;

    protected MappedEntity(TarantoolPersistentEntity<?> entity, PersistentPropertyAccessor<T> propertyAccessor) {
        this.entity = entity;
        this.propertyAccessor = propertyAccessor;
    }

    @Override
    public boolean isVersionedEntity() {
        return entity.hasVersionProperty();
    }

    @Nullable
    @Override
    public Object getVersion() {
        return propertyAccessor.getProperty(entity.getRequiredVersionProperty());
    }

    @Override
    public T getBean() {
        return propertyAccessor.getBean();
    }

    @Override
    public boolean isNew() {
        return entity.isNew(getBean());
    }
}
