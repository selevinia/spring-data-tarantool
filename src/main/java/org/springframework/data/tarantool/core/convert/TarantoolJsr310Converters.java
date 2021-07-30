package org.springframework.data.tarantool.core.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tarantool date converters
 *
 * @author Tatiana Blinova
 */
public class TarantoolJsr310Converters {

    public static Collection<Converter<?, ?>> getConvertersToRegister() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(NumberToLocalDateTimeConverter.INSTANCE);
        converters.add(LocalDateTimeToLongConverter.INSTANCE);
        converters.add(NumberToLocalDateConverter.INSTANCE);
        converters.add(LocalDateToLongConverter.INSTANCE);
        converters.add(NumberToLocalTimeConverter.INSTANCE);
        converters.add(LocalTimeToLongConverter.INSTANCE);
        converters.add(NumberToInstantConverter.INSTANCE);
        converters.add(InstantToLongConverter.INSTANCE);
        converters.add(Jsr310Converters.ZoneIdToStringConverter.INSTANCE);
        converters.add(Jsr310Converters.StringToZoneIdConverter.INSTANCE);
        converters.add(Jsr310Converters.DurationToStringConverter.INSTANCE);
        converters.add(Jsr310Converters.StringToDurationConverter.INSTANCE);
        converters.add(Jsr310Converters.PeriodToStringConverter.INSTANCE);
        converters.add(Jsr310Converters.StringToPeriodConverter.INSTANCE);
        converters.add(Jsr310Converters.StringToLocalDateConverter.INSTANCE);
        converters.add(Jsr310Converters.StringToLocalDateTimeConverter.INSTANCE);
        converters.add(Jsr310Converters.StringToInstantConverter.INSTANCE);
        return converters;
    }

    @ReadingConverter
    public enum NumberToLocalDateTimeConverter implements Converter<Number, LocalDateTime> {

        INSTANCE;

        @Override
        public LocalDateTime convert(Number source) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(source.longValue()), ZoneId.systemDefault());
        }
    }

    @WritingConverter
    public enum LocalDateTimeToLongConverter implements Converter<LocalDateTime, Long> {

        INSTANCE;

        @Override
        public Long convert(LocalDateTime source) {
            return source.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }

    @ReadingConverter
    public enum NumberToLocalDateConverter implements Converter<Number, LocalDate> {

        INSTANCE;

        @Override
        public LocalDate convert(Number source) {
            return LocalDate.ofInstant(Instant.ofEpochMilli(source.longValue()), ZoneId.systemDefault());
        }
    }

    @WritingConverter
    public enum LocalDateToLongConverter implements Converter<LocalDate, Long> {

        INSTANCE;

        @Override
        public Long convert(LocalDate source) {
            return source.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }

    @ReadingConverter
    public enum NumberToLocalTimeConverter implements Converter<Number, LocalTime> {

        INSTANCE;

        @Override
        public LocalTime convert(Number source) {
            return LocalTime.ofNanoOfDay(source.longValue());
        }
    }

    @WritingConverter
    public enum LocalTimeToLongConverter implements Converter<LocalTime, Long> {

        INSTANCE;

        @Override
        public Long convert(LocalTime source) {
            return source.getLong(ChronoField.NANO_OF_DAY);
        }
    }

    @ReadingConverter
    public enum NumberToInstantConverter implements Converter<Number, Instant> {

        INSTANCE;

        @Override
        public Instant convert(Number source) {
            return Instant.ofEpochMilli(source.longValue());
        }
    }

    @WritingConverter
    public enum InstantToLongConverter implements Converter<Instant, Long> {

        INSTANCE;

        @Override
        public Long convert(Instant source) {
            return source.toEpochMilli();
        }
    }
}
