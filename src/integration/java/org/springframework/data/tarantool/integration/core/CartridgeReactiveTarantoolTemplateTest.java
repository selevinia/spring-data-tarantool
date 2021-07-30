package org.springframework.data.tarantool.integration.core;

import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CartridgeTarantoolClientOptions;

/**
 * Runner class for reactive template tests for standard cartridge Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.cartridge.yml may be used.
 */
public class CartridgeReactiveTarantoolTemplateTest extends AbstractReactiveTarantoolTemplateTest {

    @Override
    public TarantoolClientOptions getOptions() {
        return new CartridgeTarantoolClientOptions();
    }
}
