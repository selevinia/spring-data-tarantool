package org.springframework.data.tarantool.repository.config;

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * {@link ImportBeanDefinitionRegistrar} to setup Tarantool repositories via
 * {@link EnableTarantoolRepositories}.
 *
 * @author Alexander Rublev
 */
public class TarantoolRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    /**
     * @see RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
     */
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableTarantoolRepositories.class;
    }

    /**
     * @see RepositoryBeanDefinitionRegistrarSupport#getExtension()
     */
    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new TarantoolRepositoryConfigurationExtension();
    }
}
