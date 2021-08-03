package org.springframework.data.tarantool.repository.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfiguration;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.tarantool.core.mapping.Space;
import org.springframework.data.tarantool.repository.MapIdTarantoolRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.fail;

public class TarantoolRepositoryConfigurationExtensionTest {

    private final AnnotationMetadata metadata = AnnotationMetadata.introspect(Config.class);
    private final ResourceLoader loader = new PathMatchingResourcePatternResolver();
    private final Environment environment = new StandardEnvironment();
    private final BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
    private final RepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(metadata,
            EnableTarantoolRepositories.class, loader, environment, registry, null);

    private TarantoolRepositoryConfigurationExtension extension;

    @BeforeEach
    void setUp() {
        extension = new TarantoolRepositoryConfigurationExtension();
    }

    @Test
    void shouldStrictMatchIfDomainTypeIsAnnotatedWithSpace() {
        assertHasRepo(SampleRepository.class, extension.getRepositoryConfigurations(configurationSource, loader, true));
    }

    @Test
    void shouldStrictMatchIfRepositoryExtendsStoreSpecificBase() {
        assertHasRepo(StoreRepository.class, extension.getRepositoryConfigurations(configurationSource, loader, true));
    }

    @Test
    void shouldNotStrictMatchIfDomainTypeIsNotAnnotatedWithSpace() {
        assertDoesNotHaveRepo(UnannotatedRepository.class,
                extension.getRepositoryConfigurations(configurationSource, loader, true));
    }

    private static void assertDoesNotHaveRepo(Class<?> repositoryInterface,
                                              Collection<RepositoryConfiguration<RepositoryConfigurationSource>> configs) {
        try {
            assertHasRepo(repositoryInterface, configs);
            fail("Expected not to find config for repository interface " + repositoryInterface.getName());
        } catch (AssertionError ignored) {
            // repo not there. we're fine.
        }
    }

    private static void assertHasRepo(Class<?> repositoryInterface,
                                      Collection<RepositoryConfiguration<RepositoryConfigurationSource>> configs) {
        for (RepositoryConfiguration<?> config : configs) {
            if (config.getRepositoryInterface().equals(repositoryInterface.getName())) {
                return;
            }
        }

        fail(String.format("Expected to find config for repository interface %s but got %s", repositoryInterface.getName(), configs));
    }

    @EnableTarantoolRepositories(considerNestedRepositories = true)
    private static class Config {
    }

    @Space
    private static class Sample {
    }

    interface SampleRepository extends CrudRepository<Sample, Long> {
    }

    interface UnannotatedRepository extends CrudRepository<Object, Long> {
    }

    interface StoreRepository extends MapIdTarantoolRepository<Object> {
    }
}
