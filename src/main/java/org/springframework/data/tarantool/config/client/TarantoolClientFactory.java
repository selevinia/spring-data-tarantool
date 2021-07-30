package org.springframework.data.tarantool.config.client;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;

/**
 * Facade interface for Tarantool Client producer
 *
 * @author Alexander Rublev
 */
public interface TarantoolClientFactory {

    /**
     * Produce Tarantool Client
     * @return TarantoolClient instance
     */
    TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> createClient();

}
