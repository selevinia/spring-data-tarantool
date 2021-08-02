package org.springframework.data.tarantool.core;

import io.tarantool.driver.ProxyTarantoolClient;
import io.tarantool.driver.TarantoolVersion;
import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.space.TarantoolSpaceOperations;
import io.tarantool.driver.api.tuple.TarantoolTuple;

/**
 * Common interface to accumulate methods to interact with TarantoolClient
 *
 * @author Alexander Rublev
 */
public interface TarantoolClientAware {

    /**
     * Return Tarantool Client used for this instance
     *
     * @return entity converter
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
     * @param spaceName name of giving space
     * @return Tarantool space operations
     */
    default TarantoolSpaceOperations<TarantoolTuple, TarantoolResult<TarantoolTuple>> spaceOps(String spaceName) {
        return getClient().space(spaceName);
    }

    /**
     * Return Tarantool server version
     *
     * @return version
     */
    TarantoolVersion getVersion();

}
