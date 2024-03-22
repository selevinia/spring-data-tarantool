package org.springframework.data.tarantool.integration.core;

import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CartridgeTarantoolClientOptions;

/**
 * Runner class for template tests for standard cartridge Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.cartridge.yml may be used.
 */
public class CartridgeTarantoolTemplateTest extends AbstractTarantoolTemplateTest {

    @Override
    public TarantoolClientOptions getOptions() {
        return new CartridgeTarantoolClientOptions();
    }

    @Override
    public String getVersion() {
        return "2.9";
    }
}
