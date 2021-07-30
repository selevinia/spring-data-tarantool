package org.springframework.data.tarantool.repository.support;

import org.springframework.data.repository.core.EntityInformation;

/**
 * Tarantool specific {@link EntityInformation}.
 *
 * @author Alexander Rublev
 */
public interface TarantoolEntityInformation<T, ID> extends EntityInformation<T, ID>, TarantoolEntityMetadata<T> {

    /**
     * Returns the attribute that the id will be persisted to.
     *
     * @return not null String
     */
    String getIdAttribute();
}
