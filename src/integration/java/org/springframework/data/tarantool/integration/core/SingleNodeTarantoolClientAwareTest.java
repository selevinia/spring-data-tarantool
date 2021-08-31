package org.springframework.data.tarantool.integration.core;

import org.junit.jupiter.api.Test;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.integration.config.SingleNodeTarantoolClientOptions;

import static org.assertj.core.api.Assertions.assertThat;

public class SingleNodeTarantoolClientAwareTest extends AbstractTarantoolClientAwareTest {

    @Override
    public TarantoolClientOptions getOptions() {
        return new SingleNodeTarantoolClientOptions();
    }

    @Test
    void shouldCheckIsProxyClient() {
        boolean isProxyClient = tarantoolTemplate.isProxyClient();
        assertThat(isProxyClient).isFalse();
    }

}
