package org.springframework.data.tarantool.core.convert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TarantoolJsr310ConvertersTest {

    @BeforeEach
    void setUp() {
        System.setProperty("user.timezone", "Europe/Moscow");
    }

    @Test
    void shouldConvertNumberToLocalDateTime() {
        LocalDateTime converted = TarantoolJsr310Converters.NumberToLocalDateTimeConverter.INSTANCE.convert(1_619_816_400_000L);
        assertThat(converted).isEqualTo(LocalDate.of(2021, 5, 1).atStartOfDay());
    }

    @Test
    void shouldConvertLocalDateTimeToLong() {
        Long converted = TarantoolJsr310Converters.LocalDateTimeToLongConverter.INSTANCE.convert(LocalDate.of(2021, 5, 1).atStartOfDay());
        assertThat(converted).isEqualTo(1_619_816_400_000L);
    }

    @Test
    void shouldConvertNumberToLocalDate() {
        LocalDate converted = TarantoolJsr310Converters.NumberToLocalDateConverter.INSTANCE.convert(1);
        assertThat(converted).isEqualTo(LocalDate.of(1970, 1, 1));
    }

    @Test
    void shouldConvertLocalDateToLong() {
        Long converted = TarantoolJsr310Converters.LocalDateToLongConverter.INSTANCE.convert(LocalDate.of(2021, 5, 1));
        assertThat(converted).isEqualTo(1_619_816_400_000L);
    }

    @Test
    void shouldConvertNumberToLocalTime() {
        LocalTime converted = TarantoolJsr310Converters.NumberToLocalTimeConverter.INSTANCE.convert(3_723_000_000_000L);
        assertThat(converted).isEqualTo(LocalTime.of(1, 2, 3));
    }

    @Test
    void shouldConvertLocalTimeToLong() {
        Long converted = TarantoolJsr310Converters.LocalTimeToLongConverter.INSTANCE.convert(LocalTime.MIDNIGHT);
        assertThat(converted).isZero();

        converted = TarantoolJsr310Converters.LocalTimeToLongConverter.INSTANCE.convert(LocalTime.of(1, 2, 3));
        assertThat(converted).isEqualTo(3_723_000_000_000L);
    }

    @Test
    void shouldConvertNumberToInstant() {
        ZoneId systemDefaultZoneId = ZoneId.systemDefault();
        Instant converted = TarantoolJsr310Converters.NumberToInstantConverter.INSTANCE.convert(1_619_816_400_000L);
        assertThat(converted).isEqualTo(LocalDate.of(2021, 5, 1).atStartOfDay().atZone(systemDefaultZoneId).toInstant());
    }

    @Test
    void shouldConvertInstantToLong() {
        ZoneId systemDefaultZoneId = ZoneId.systemDefault();
        Long converted = TarantoolJsr310Converters.InstantToLongConverter.INSTANCE.convert(LocalDate.of(2021, 5, 1).atStartOfDay().atZone(systemDefaultZoneId).toInstant());
        assertThat(converted).isEqualTo(1_619_816_400_000L);
    }
}
