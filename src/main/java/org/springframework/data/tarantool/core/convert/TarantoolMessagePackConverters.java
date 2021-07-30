package org.springframework.data.tarantool.core.convert;

import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Tarantool MessagePack values converters
 *
 * @author Tatiana Blinova
 */
public class TarantoolMessagePackConverters {

    public static Collection<Converter<?, ?>> getConvertersToRegister() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(IntegerToLongConverter.INSTANCE);
        converters.add(ShortToLongConverter.INSTANCE);
        converters.add(ShortToIntegerConverter.INSTANCE);
        converters.add(FloatToDoubleConverter.INSTANCE);
        return converters;
    }

    /**
     * Small long values may be stored and returned as short integers in MessagePack
     */
    public enum IntegerToLongConverter implements Converter<Integer, Long> {

        INSTANCE;

        public Long convert(Integer source) {
            return Long.valueOf(source);
        }
    }

    /**
     * Small long values may be stored and returned as short integers in MessagePack
     */
    public enum ShortToLongConverter implements Converter<Short, Long> {

        INSTANCE;

        public Long convert(Short source) {
            return Long.valueOf(source);
        }
    }

    /**
     * Small long values may be stored and returned as short integers in MessagePack
     */
    public enum ShortToIntegerConverter implements Converter<Short, Integer> {

        INSTANCE;

        public Integer convert(Short source) {
            return Integer.valueOf(source);
        }
    }

    /**
     * Small floating point values may be stored and returned as single precision values in MessagePack
     */
    public enum FloatToDoubleConverter implements Converter<Float, Double> {

        INSTANCE;

        public Double convert(Float source) {
            return Double.valueOf(source);
        }
    }
}
