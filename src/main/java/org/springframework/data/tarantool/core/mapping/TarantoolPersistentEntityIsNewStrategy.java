package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.support.IsNewStrategy;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.function.Function;

/**
 * An {@link IsNewStrategy} to use a {@link PersistentEntity}'s version or id property followed by it
 *
 * @author Tatiana Blinova
 */
public class TarantoolPersistentEntityIsNewStrategy implements IsNewStrategy {
    private final Function<Object, Object> valueLookup;
    private final Class<?> valueType;

    private TarantoolPersistentEntityIsNewStrategy(PersistentEntity<?, ?> entity) {
        Assert.notNull(entity, "PersistentEntity must not be null!");

        this.valueLookup = entity.hasVersionProperty()
                ? source -> entity.getPropertyAccessor(source).getProperty(entity.getRequiredVersionProperty())
                : source -> entity.getIdentifierAccessor(source).getIdentifier();

        this.valueType = entity.hasVersionProperty()
                ? entity.getRequiredVersionProperty().getType()
                : entity.hasIdProperty() ? entity.getRequiredIdProperty().getType() : MapId.class;

        if (valueType.isPrimitive() && !ClassUtils.isAssignable(Number.class, valueType)) {
            throw new IllegalArgumentException(String
                    .format("Only numeric primitives are supported as identifier / version field types! Got: %s.", valueType));
        }
    }

    public static TarantoolPersistentEntityIsNewStrategy of(PersistentEntity<?, ?> entity) {
        return new TarantoolPersistentEntityIsNewStrategy(entity);
    }

    @Override
    public boolean isNew(Object entity) {
        Object value = valueLookup.apply(entity);

        if (value == null) {
            return true;
        }

        if (value instanceof MapId && ((MapId) value).isEmpty()) {
            return true;
        }

        if (valueType != null && !valueType.isPrimitive()) {
            return false;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue() == 0;
        }

        throw new IllegalArgumentException(String.format("Could not determine whether %s is new! Unsupported identifier or version property!", entity));
    }
}
