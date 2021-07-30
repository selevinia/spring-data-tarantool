package org.springframework.data.tarantool.core;

import org.springframework.lang.Nullable;

/**
 * A representation of information about an entity.
 *
 * @author Alexander Rublev
 */
public interface Entity<T> {

    /**
     * Returns whether the entity is versioned, i.e. if it contains a version property.
     */
    boolean isVersionedEntity();

    /**
     * Returns the value of the version if the entity has a version property, {@literal null} otherwise.
     */
    @Nullable
    Object getVersion();

    /**
     * Returns the underlying bean.
     */
    T getBean();

    /**
     * Returns whether the entity is considered to be new.
     */
    boolean isNew();

}
