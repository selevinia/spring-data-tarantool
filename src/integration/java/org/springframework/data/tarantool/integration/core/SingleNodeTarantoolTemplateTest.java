package org.springframework.data.tarantool.integration.core;

import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;

/**
 * Runner class for template tests for single node Tarantool installation.
 * To run test cartridge using Docker, file docker-compose.single-node.yml may be used.
 */
public class SingleNodeTarantoolTemplateTest extends AbstractTarantoolTemplateTest {

    @Override
    public TarantoolClientOptions getOptions() {
        return new SingleNodeTarantoolClientOptions();
    }
}
