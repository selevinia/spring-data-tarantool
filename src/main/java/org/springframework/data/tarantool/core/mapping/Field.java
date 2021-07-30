package org.springframework.data.tarantool.core.mapping;

import java.lang.annotation.*;

/**
 * Allows adding some metadata to the class fields relevant for storing them in the Tarantool space
 *
 * @author Alexander Rublev
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Field {

    /**
     * The target Tarantool space field for storing the marked class field.
     *
     * @return the name of a field in space
     */
    String value() default "";
}
