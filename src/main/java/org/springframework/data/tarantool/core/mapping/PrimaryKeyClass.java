package org.springframework.data.tarantool.core.mapping;

import org.springframework.data.annotation.Persistent;

import java.lang.annotation.*;

/**
 * Specifies a composite primary key class, which used in entity (marked as {@link Space}
 * as field type marked as {@link PrimaryKey}.
 *
 * @author Alexander Rublev
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PrimaryKeyClass {
}
