package org.springframework.data.tarantool.repository.support;

import org.springframework.data.repository.core.EntityMetadata;

/**
 * Extension of {@link EntityMetadata} to additionally expose the Tarantool space name an entity shall be persisted to.
 *
 * @param <T> type to ger metadata
 * @author Alexander Rublev
 */
public interface TarantoolEntityMetadata<T> extends EntityMetadata<T> {

    /**
     * Returns the name of the Tarantool space the entity shall be persisted to.
     *
     * @return not null String
     */
    String getSpaceName();
}
