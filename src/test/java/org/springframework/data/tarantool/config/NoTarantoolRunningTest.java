package org.springframework.data.tarantool.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.tarantool.TarantoolServerConnectionException;
import org.springframework.data.tarantool.core.ReactiveTarantoolTemplate;
import org.springframework.data.tarantool.domain.User;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringJUnitConfig
public class NoTarantoolRunningTest {

    @Configuration
    static class Config extends AbstractReactiveTarantoolConfiguration {

    }

    @Autowired
    private ReactiveTarantoolTemplate tarantoolTemplate;

    @Test
    public void startsUpWithoutATarantoolRunning() {
        assertThat(tarantoolTemplate.getClass().getName()).isEqualTo("org.springframework.data.tarantool.core.ReactiveTarantoolTemplate");
    }

    @Test
    public void failsDataAccessWithoutATarantoolRunning() {
        assertThatExceptionOfType(TarantoolServerConnectionException.class)
                .isThrownBy(() -> tarantoolTemplate.call("count", User.class));
    }

}
