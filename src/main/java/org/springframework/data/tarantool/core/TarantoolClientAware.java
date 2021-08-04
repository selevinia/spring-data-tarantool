package org.springframework.data.tarantool.core;

import io.tarantool.driver.ProxyTarantoolClient;
import io.tarantool.driver.TarantoolVersion;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import io.tarantool.driver.metadata.TarantoolSpaceMetadata;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mapping.MappingException;

import java.util.Optional;

/**
 * Common interface to accumulate methods to interact with TarantoolClient
 *
 * @author Alexander Rublev
 */
public interface TarantoolClientAware {

    /**
     * Return Tarantool Client used for this instance
     *
     * @return entity client
     */
    TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> getClient();

    /**
     * Check tarantool client type operations use
     *
     * @return true if ProxyTarantoolClient used
     */
    default boolean isProxyClient() {
        return getClient() instanceof ProxyTarantoolClient;
    }

    /**
     * Provide Tarantool space operations for giving space name
     *
     * @param spaceName name of giving space
     * @return Tarantool space operations
     */
    default TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>> spaceOperations(String spaceName) {
        return getClient().space(spaceName);
    }

    /**
     * Provide Tarantool space metadata for giving space name. If not found then throw MappingException
     *
     * @param spaceName name of giving space
     * @return Tarantool space metadata
     */
    default <T> TarantoolSpaceMetadata requiredSpaceMetadata(String spaceName) {
        return spaceMetadata(spaceName).orElseThrow(() -> new MappingException(String.format("Space metadata not found for space %s", spaceName)));
    }

    /**
     * Provide Tarantool space metadata for giving space name
     *
     * @param spaceName name of giving space
     * @return Tarantool space metadata
     */
    default <T> Optional<TarantoolSpaceMetadata> spaceMetadata(String spaceName) {
        try {
            return getClient().metadata().getSpaceByName(spaceName);
        } catch (Exception e) {
            throw dataAccessException(e);
        }
    }

    /**
     * Return Tarantool server version
     *
     * @return version
     */
    TarantoolVersion getVersion();

    /**
     * Translate any Tarantool client exception to DataAccessException
     *
     * @return translated exception
     */
    DataAccessException dataAccessException(Throwable throwable);

}
