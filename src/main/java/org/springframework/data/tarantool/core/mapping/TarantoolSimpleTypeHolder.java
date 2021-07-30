package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public class TarantoolSimpleTypeHolder extends SimpleTypeHolder {

    private static final Set<Class<?>> TARANTOOL_SIMPLE_TYPES;

    static {
        TARANTOOL_SIMPLE_TYPES = Set.of(
                boolean.class,
                long.class,
                short.class,
                int.class,
                byte.class,
                byte[].class,
                float.class,
                double.class,
                char.class,
                Boolean.class,
                Long.class,
                Short.class,
                Integer.class,
                Byte.class,
                Float.class,
                Double.class,
                String.class,
                UUID.class,
                BigDecimal.class
        );
    }

    public static final SimpleTypeHolder HOLDER = new TarantoolSimpleTypeHolder();

    public TarantoolSimpleTypeHolder() {
        super(TARANTOOL_SIMPLE_TYPES, false);
    }
}
