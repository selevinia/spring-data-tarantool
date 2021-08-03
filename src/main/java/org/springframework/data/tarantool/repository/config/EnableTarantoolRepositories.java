package org.springframework.data.tarantool.repository.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.tarantool.core.TarantoolTemplate;
import org.springframework.data.tarantool.repository.support.TarantoolRepositoryFactoryBean;

import java.lang.annotation.*;

import static org.springframework.context.annotation.ComponentScan.Filter;
import static org.springframework.data.repository.query.QueryLookupStrategy.Key;

/**
 * Annotation to enable Tarantool repositories.
 *
 * @author Alexander Rublev
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import(TarantoolRepositoriesRegistrar.class)
public @interface EnableTarantoolRepositories {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations e.g.:
     * {@code @EnableReactiveTarantoolRepositories("org.my.pkg")} instead of
     * {@code @EnableReactiveTarantoolRepositories(basePackages="org.my.pkg")}.
     */
    String[] value() default {};

    /**
     * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
     * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
     * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
     * each package that serves no purpose other than being referenced by this attribute.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
     * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
     */
    Filter[] includeFilters() default {};

    /**
     * Specifies which types are not eligible for component scanning.
     */
    Filter[] excludeFilters() default {};

    /**
     * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
     * for a repository named {@code UserRepository} the corresponding implementation class will be looked up scanning for
     * {@code UserRepositoryImpl}.
     */
    String repositoryImplementationPostfix() default "Impl";

    /**
     * Configures the location of where to find the Spring Data named queries properties file. Not supported yet.
     */
    String namedQueriesLocation() default "";

    /**
     * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
     * {@link Key#CREATE_IF_NOT_FOUND}.
     */
    Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

    /**
     * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
     * {@link TarantoolRepositoryFactoryBean}.
     */
    Class<?> repositoryFactoryBeanClass() default TarantoolRepositoryFactoryBean.class;

    /**
     * Configure the repository base class to be used to create repository proxies for this particular configuration.
     */
    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    /**
     * Configures the name of the {@link TarantoolTemplate} bean to be
     * used with the repositories detected.
     */
    String tarantoolTemplateRef() default "tarantoolTemplate";

    /**
     * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
     * repositories infrastructure.
     */
    boolean considerNestedRepositories() default false;
}