package org.springframework.data.tarantool.integration.cache;

import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CartridgeTarantoolClientOptions;

public class CartridgeTarantoolNativeCacheTest extends AbstractTarantoolNativeCacheTest {

    @Override
    public TarantoolClientOptions getOptions() {
        return new CartridgeTarantoolClientOptions();
    }
}
