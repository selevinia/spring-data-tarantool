package org.springframework.data.tarantool.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.data.tarantool.core.convert.TarantoolCustomConversions;
import org.springframework.data.tarantool.core.mapping.PrimaryKeyClass;
import org.springframework.data.tarantool.core.mapping.Space;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.TarantoolSimpleTypeHolder;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Base class for Spring Data Tarantool to be extended for JavaConfiguration usage.
 *
 * @author Alexander Rublev
 */
public abstract class TarantoolConfigurationSupport {

    /**
     * Returns the base packages to scan for Tarantool mapped entities at startup. Will return the package name of the
     * configuration class' (the concrete class, not this one here) by default. So if you have a
     * {@code com.acme.AppConfig} extending {@link TarantoolConfigurationSupport} the base package will be considered
     * {@code com.acme} unless the method is overridden to implement alternate behavior.
     *
     * @return the base packages to scan for mapped {@link Space} classes or an empty collection to not enable scanning
     * for entities.
     */
    protected Collection<String> getMappingBasePackages() {
        Package mappingBasePackage = getClass().getPackage();
        return Collections.singleton(mappingBasePackage == null ? null : mappingBasePackage.getName());
    }

    /**
     * Creates a {@link TarantoolMappingContext} equipped with entity classes scanned from the mapping base package.
     *
     * @return TarantoolMappingContext instance
     * @throws ClassNotFoundException if the entity scan fails
     */
    @Bean
    public TarantoolMappingContext tarantoolMappingContext() throws ClassNotFoundException {
        TarantoolMappingContext mappingContext = new TarantoolMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());
        mappingContext.setSimpleTypeHolder(TarantoolSimpleTypeHolder.HOLDER);
        mappingContext.setFieldNamingStrategy(fieldNamingStrategy());

        return mappingContext;
    }

    /**
     * Creates an instance of {@link TarantoolCustomConversions} for customizing the conversion from some Java objects
     * into the objects which have internal mapping in the driver (like primitive types, Tarantool tuples, etc)
     *
     * @return a {@link TarantoolCustomConversions} instance
     */
    @Bean
    public TarantoolCustomConversions tarantoolCustomConversions() {
        return new TarantoolCustomConversions(customConverters());
    }

    /**
     * Override this method for providing custom conversions
     *
     * @return list of custom conversions
     */
    protected List<?> customConverters() {
        return Collections.emptyList();
    }

    /**
     * Scans the mapping base package for classes annotated with {@link Space}. By default, it scans for entities in
     * all packages returned by {@link #getMappingBasePackages()}.
     *
     * @return set of initial entities
     * @throws ClassNotFoundException if class not found
     * @see #getMappingBasePackages()
     */
    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        Set<Class<?>> initialEntitySet = new HashSet<>();

        for (String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }

        return initialEntitySet;
    }

    /**
     * Scans the given base package for entities, i.e. Tarantool specific types annotated with {@link Space} and {@link PrimaryKeyClass}.
     *
     * @return set of entities was found
     * @param basePackage must not be {@literal null}.
     * @throws ClassNotFoundException if class not found
     */
    protected Set<Class<?>> scanForEntities(String basePackage) throws ClassNotFoundException {
        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        }

        Set<Class<?>> initialEntitySet = new HashSet<>();
        ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(false);
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(Space.class));
        componentProvider.addIncludeFilter(new AnnotationTypeFilter(PrimaryKeyClass.class));
        for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
            initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(), TarantoolConfigurationSupport.class.getClassLoader()));
        }

        return initialEntitySet;
    }

    /**
     * Configures a {@link FieldNamingStrategy} on the {@link TarantoolMappingContext} instance created.
     *
     * @return default field naming strategy
     */
    protected FieldNamingStrategy fieldNamingStrategy() {
        return new SnakeCaseFieldNamingStrategy();
    }

}
