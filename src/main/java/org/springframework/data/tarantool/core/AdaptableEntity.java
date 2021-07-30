package org.springframework.data.tarantool.core;

import org.springframework.lang.Nullable;

/**
 * Extended information about an entity.
 *
 * @author Alexander Rublev
 */
public interface AdaptableEntity<T> extends Entity<T> {

    /**
     * Initializes the version property of the of the current entity if available.
     *
     * @return the entity with the version property updated if available.
     */
    T initializeVersionProperty();

    /**
     * Increments the value of the version property if available.
     *
     * @return the entity with the version property incremented if available.
     */
    T incrementVersion();

    /**
     * Returns the current version value if the entity has a version property.
     *
     * @return the current version or {@literal null} in case it's uninitialized or the entity doesn't expose a version
     *         property.
     */
    @Nullable
    Number getVersion();

}
