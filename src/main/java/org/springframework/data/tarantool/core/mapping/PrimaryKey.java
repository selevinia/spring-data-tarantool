package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.annotation.Id;

import java.lang.annotation.*;

/**
 * Identifies the primary key field of the entity, which may be of a basic type or of a type that represents a composite
 * primary key class.
 *
 * @author Tatiana Blinova
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Id
public @interface PrimaryKey {

    /**
     * The target Tarantool space field for the primary key if it is of a simple type, else ignored.
     *
     * @return the name of a field in space
     */
    String value() default "";
}
