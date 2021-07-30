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
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfiguration;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.tarantool.core.mapping.Space;
import org.springframework.data.tarantool.repository.MapIdReactiveTarantoolRepository;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.fail;

public class ReactiveTarantoolRepositoryConfigurationExtensionTest {

    private AnnotationMetadata metadata = AnnotationMetadata.introspect(Config.class);
    private ResourceLoader loader = new PathMatchingResourcePatternResolver();
    private Environment environment = new StandardEnvironment();
    private BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
    private RepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(metadata,
            EnableReactiveTarantoolRepositories.class, loader, environment, registry, null);

    private ReactiveTarantoolRepositoryConfigurationExtension extension;

    @BeforeEach
    void setUp() {
        extension = new ReactiveTarantoolRepositoryConfigurationExtension();
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

    @EnableReactiveTarantoolRepositories(considerNestedRepositories = true)
    private static class Config {
    }

    @Space
    private static class Sample {
    }

    interface SampleRepository extends ReactiveCrudRepository<Sample, Long> {
    }

    interface UnannotatedRepository extends ReactiveCrudRepository<Object, Long> {
    }

    interface StoreRepository extends MapIdReactiveTarantoolRepository<Object> {
    }
}
