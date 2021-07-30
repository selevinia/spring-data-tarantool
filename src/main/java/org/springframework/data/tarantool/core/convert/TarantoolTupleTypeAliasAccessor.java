package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.data.convert.TypeAliasAccessor;
import org.springframework.data.mapping.Alias;
import org.springframework.util.Assert;

/**
 * Reads and writes type alias into a tuple
 *
 * @author Tatiana Blinova
 */
public class TarantoolTupleTypeAliasAccessor implements TypeAliasAccessor<TarantoolTuple> {

    private final String typeKey;

    public TarantoolTupleTypeAliasAccessor(final String typeKey) {
        Assert.notNull(typeKey, "TypeKey for TypeAliasAccessor must not be null");
        this.typeKey = typeKey;
    }

    @Override
    public Alias readAliasFrom(TarantoolTuple source) {
        return Alias.ofNullable(source.getObject(typeKey, String.class).orElse(null));
    }

    @Override
    public void writeTypeTo(TarantoolTuple sink, Object alias) {
        sink.putObject(typeKey, alias);
    }
}
