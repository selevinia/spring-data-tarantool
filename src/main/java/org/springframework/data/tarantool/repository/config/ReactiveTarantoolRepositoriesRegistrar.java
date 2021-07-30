package org.springframework.data.tarantool.repository.config;

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * {@link ImportBeanDefinitionRegistrar} to setup Tarantool repositories via
 * {@link EnableReactiveTarantoolRepositories}.
 *
 * @author Alexander Rublev
 */
public class ReactiveTarantoolRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    /**
     * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getAnnotation()
     */
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableReactiveTarantoolRepositories.class;
    }

    /**
     * @see org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport#getExtension()
     */
    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new ReactiveTarantoolRepositoryConfigurationExtension();
    }
}
