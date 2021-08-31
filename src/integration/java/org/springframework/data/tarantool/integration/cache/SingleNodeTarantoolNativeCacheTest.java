package org.springframework.data.tarantool.integration.cache;

import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;

public class SingleNodeTarantoolNativeCacheTest extends AbstractTarantoolNativeCacheTest {

    @Override
    public TarantoolClientOptions getOptions() {
        return new SingleNodeTarantoolClientOptions();
    }
}
