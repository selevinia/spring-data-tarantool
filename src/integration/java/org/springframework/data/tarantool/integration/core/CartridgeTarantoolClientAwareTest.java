package org.springframework.data.tarantool.integration.core;

import org.junit.jupiter.api.Test;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.CartridgeTarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;

import static org.assertj.core.api.Assertions.assertThat;

public class CartridgeTarantoolClientAwareTest extends AbstractTarantoolClientAwareTest {

    @Override
    public TarantoolClientOptions getOptions() {
        return new CartridgeTarantoolClientOptions();
    }

    @Test
    void shouldCheckIsProxyClient() {
        boolean isProxyClient = tarantoolTemplate.isProxyClient();
        assertThat(isProxyClient).isTrue();
    }
}
