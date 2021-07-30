package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.mapping.PersistentEntity;

import java.util.List;
import java.util.Optional;

/**
 * Tarantool specific {@link PersistentEntity} abstraction.
 *
 * @author Alexander Rublev
 */
public interface TarantoolPersistentEntity<T> extends PersistentEntity<T, TarantoolPersistentProperty> {

    /**
     * Tarantool space this entity can be saved to
     * @return not null String
     */
    String getSpaceName();

    /**
     * Get information about {@link Space} annotation on the class
     * @return true, if the {@link Space} annotation is set on the class
     */
    boolean hasSpaceAnnotation();

    /**
     * Get information about {@link PrimaryKeyClass} annotation on the class
     * @return true if the {@link PrimaryKeyClass} annotation is set on the class
     */
    boolean isCompositePrimaryKeyClass();

    /**
     * Get information about {@link PrimaryKey} field
     * @return true if the {@link PrimaryKey} has type, annotated with {@link PrimaryKeyClass}
     */
    boolean hasCompositePrimaryKey();
}
