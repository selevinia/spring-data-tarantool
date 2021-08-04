package org.springframework.data.tarantool.config;

import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;

import java.lang.annotation.*;

/**
 * Annotation to enable auditing in Tarantool via annotation configuration.
 *
 * @author Alexander Rublev
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TarantoolAuditingRegistrar.class)
public @interface EnableTarantoolAuditing {

    /**
     * Configures the {@link AuditorAware} bean to be used to lookup the current principal.
     *
     * @return empty {@link String} by default.
     */
    String auditorAwareRef() default "";

    /**
     * Configures whether the creation and modification dates are set. Defaults to {@literal true}.
     *
     * @return {@literal true} by default.
     */
    boolean setDates() default true;

    /**
     * Configures whether the entity shall be marked as modified on creation. Defaults to {@literal true}.
     *
     * @return {@literal true} by default.
     */
    boolean modifyOnCreate() default true;

    /**
     * Configures a {@link DateTimeProvider} bean name that allows customizing the
     * {@link java.time.temporal.TemporalAccessor} to be used for setting creation and modification dates.
     *
     * @return empty {@link String} by default.
     */
    String dateTimeProviderRef() default "";
}
