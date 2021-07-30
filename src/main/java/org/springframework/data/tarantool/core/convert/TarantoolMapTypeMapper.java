package org.springframework.data.tarantool.core.convert;

import org.springframework.data.convert.DefaultTypeMapper;

import java.util.Map;

/**
 * Value type mapper for nested objects based on the special "class" field value
 *
 * @author Tatiana Blinova
 */
public class TarantoolMapTypeMapper extends DefaultTypeMapper<Map<String, Object>> {

    public static final String DEFAULT_TYPE_KEY = "_class";

    public TarantoolMapTypeMapper() {
        super(new TarantoolMapTypeAliasAccessor(DEFAULT_TYPE_KEY));
    }

    public TarantoolMapTypeMapper(final String typeKey) {
        super(new TarantoolMapTypeAliasAccessor(typeKey));
    }
}
