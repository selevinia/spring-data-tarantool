package org.springframework.data.tarantool.core.convert;

import org.springframework.data.convert.TypeAliasAccessor;
import org.springframework.data.mapping.Alias;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Actually reads and writes type alias into a nested object in tuple
 *
 * @author Tatiana Blinova
 */
public class TarantoolMapTypeAliasAccessor implements TypeAliasAccessor<Map<String, Object>> {

    private final String typeKey;

    public TarantoolMapTypeAliasAccessor(final String typeKey) {
        Assert.notNull(typeKey, "TypeKey for TypeAliasAccessor must not be null");
        this.typeKey = typeKey;
    }

    @Override
    public Alias readAliasFrom(Map<String, Object> source) {
        return Alias.ofNullable(source.get(typeKey));
    }

    @Override
    public void writeTypeTo(Map<String, Object> sink, Object alias) {
        sink.put(typeKey, alias);
    }
}
