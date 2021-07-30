package org.springframework.data.tarantool.core.mapping;

import java.lang.annotation.*;

/**
 * Identifies the composite primary key field of the entity, which may be only of a basic type.
 *
 * @author Tatiana Blinova
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface PrimaryKeyField {

    /**
     * The target Tarantool space field for storing the marked class field.
     *
     * @return the name of a field in space
     */
    String value() default "";
}
