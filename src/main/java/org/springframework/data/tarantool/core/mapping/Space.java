package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * Identifies domain object for saving into a Tarantool space
 *
 * @author Alexander Rublev
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Space {

    /**
     * The name of Tarantool space where the marked class objects are supposed to be stored in. The space name will be
     * derived from the class name if not specified.
     *
     * @return the name of space for storing the object
     */
    String value() default "";
}
