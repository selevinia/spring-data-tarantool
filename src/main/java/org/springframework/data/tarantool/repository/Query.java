package org.springframework.data.tarantool.repository;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * Annotation to declare function call repository methods.
 *
 * @author Alexander Rublev
 */
@Documented
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@QueryAnnotation
public @interface Query {

    /**
     * Lua function name to invoke
     * @return the function name to call
     */
    String function();
}
