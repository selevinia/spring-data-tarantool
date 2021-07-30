package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.mapping.PersistentProperty;

/**
 * Tarantool specific {@link org.springframework.data.mapping.PersistentProperty} extension.
 *
 * @author Alexander Rublev
 */
public interface TarantoolPersistentProperty extends PersistentProperty<TarantoolPersistentProperty> {

    /**
     * The name of the single column to which the property is persisted.
     */
    String getFieldName();

    /**
     * Whether the property is a composite primary key.
     */
    boolean isCompositePrimaryKey();

    /**
     * Whether the property is a field of a composite primary key
     */
    boolean isPrimaryKeyField();
}
