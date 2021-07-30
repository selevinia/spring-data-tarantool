package org.springframework.data.tarantool.core.convert;

import org.springframework.data.convert.CustomConversions;
import org.springframework.data.tarantool.core.mapping.TarantoolSimpleTypeHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Tarantool custom conversions
 *
 * @author Tatiana Blinova
 */
public class TarantoolCustomConversions extends CustomConversions {

    private static final List<Object> STORE_CONVERTERS;
    private static final StoreConversions STORE_CONVERSIONS;

    static {
        List<Object> converters = new ArrayList<>();

        converters.addAll(TarantoolJsr310Converters.getConvertersToRegister());
        converters.addAll(TarantoolMessagePackConverters.getConvertersToRegister());

        STORE_CONVERTERS = Collections.unmodifiableList(converters);
        STORE_CONVERSIONS = StoreConversions.of(TarantoolSimpleTypeHolder.HOLDER, STORE_CONVERTERS);
    }

    public TarantoolCustomConversions(Collection<?> converters) {
        super(STORE_CONVERSIONS, converters);
    }
}
