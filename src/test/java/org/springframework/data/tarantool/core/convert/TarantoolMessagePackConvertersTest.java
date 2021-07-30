package org.springframework.data.tarantool.core.convert;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class TarantoolMessagePackConvertersTest {

    @Test
    void shouldConvertIntegerToLong() {
        Long converted = TarantoolMessagePackConverters.IntegerToLongConverter.INSTANCE.convert(1);
        assertThat(converted).isEqualTo(1L);
    }

    @Test
    void shouldConvertShortToLong() {
        Long converted = TarantoolMessagePackConverters.ShortToLongConverter.INSTANCE.convert((short) 2);
        assertThat(converted).isEqualTo(2L);
    }

    @Test
    void shouldConvertShortToInteger() {
        Integer converted = TarantoolMessagePackConverters.ShortToIntegerConverter.INSTANCE.convert((short) 3);
        assertThat(converted).isEqualTo(3);
    }

    @Test
    void shouldConvertFloatToDouble() {
        Double converted = TarantoolMessagePackConverters.FloatToDoubleConverter.INSTANCE.convert(4.1f);
        assertThat(converted).isEqualTo(4.1, within(0.01));
    }
}
