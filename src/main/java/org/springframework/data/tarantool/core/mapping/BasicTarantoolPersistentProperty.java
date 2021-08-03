package org.springframework.data.tarantool.core.mapping;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.StringUtils;

/**
 * Tarantool specific {@link org.springframework.data.mapping.model.AnnotationBasedPersistentProperty} implementation.
 *
 * @author Alexander Rublev
 */
public class BasicTarantoolPersistentProperty extends AnnotationBasedPersistentProperty<TarantoolPersistentProperty> implements TarantoolPersistentProperty {

    private final FieldNamingStrategy fieldNamingStrategy;

    /**
     * Creates a new {@link BasicTarantoolPersistentProperty}.
     *
     * @param property         the actual {@link Property} in the domain entity corresponding to this persistent entity.
     * @param owner            the containing object or {@link TarantoolPersistentEntity} of this persistent property.
     * @param simpleTypeHolder mapping of Java [simple|wrapper] types to Tarantool data types.
     * @param fieldNamingStrategy {@link FieldNamingStrategy} to use
     */
    public BasicTarantoolPersistentProperty(Property property, TarantoolPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder, FieldNamingStrategy fieldNamingStrategy) {
        super(property, owner, simpleTypeHolder);
        this.fieldNamingStrategy = fieldNamingStrategy;
    }

    @Override
    protected Association<TarantoolPersistentProperty> createAssociation() {
        return new Association<>(this, null);
    }

    @Override
    public String getFieldName() {
        if (isCompositePrimaryKey()) { // then the id type has @PrimaryKeyClass
            throw new IllegalStateException(String.format("Cannot determine field name for composite primary key: %s", this));
        }

        String overriddenName = null;

        if (isIdProperty()) {
            PrimaryKey primaryKey = findAnnotation(PrimaryKey.class);
            if (primaryKey != null) {
                overriddenName = primaryKey.value();
            }
        }

        if (isPrimaryKeyField()) {
            PrimaryKeyField primaryKeyField = findAnnotation(PrimaryKeyField.class);
            if (primaryKeyField != null) {
                overriddenName = primaryKeyField.value();
            }
        }

        Field field = findAnnotation(Field.class);
        if (field != null && StringUtils.hasText(field.value())) {
            overriddenName = field.value();
        }

        return StringUtils.hasText(overriddenName) ? overriddenName : fieldNamingStrategy.getFieldName(this);
    }

    @Override
    public boolean isCompositePrimaryKey() {
        return AnnotatedElementUtils.findMergedAnnotation(getType(), PrimaryKeyClass.class) != null;
    }

    @Override
    public boolean isPrimaryKeyField() {
        return isAnnotationPresent(PrimaryKeyField.class);
    }
}
