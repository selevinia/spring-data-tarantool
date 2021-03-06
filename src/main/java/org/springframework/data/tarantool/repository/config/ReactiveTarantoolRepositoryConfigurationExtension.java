package org.springframework.data.tarantool.repository.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.tarantool.core.mapping.Space;
import org.springframework.data.tarantool.repository.ReactiveTarantoolRepository;
import org.springframework.data.tarantool.repository.support.ReactiveTarantoolRepositoryFactoryBean;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

/**
 * Tarantool specific implementation of {@link RepositoryConfigurationExtension}
 * for different configuration options.
 *
 * @author Alexander Rublev
 */
public class ReactiveTarantoolRepositoryConfigurationExtension extends TarantoolRepositoryConfigurationExtension {

    @Override
    public String getModuleName() {
        return "Reactive Tarantool";
    }

    @Override
    public String getRepositoryFactoryBeanClassName() {
        return ReactiveTarantoolRepositoryFactoryBean.class.getName();
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
        String reactiveTarantoolTemplateRef = config.getAttributes().getString("reactiveTarantoolTemplateRef");

        if (StringUtils.hasText(reactiveTarantoolTemplateRef)) {
            builder.addPropertyReference("reactiveTarantoolOperations", reactiveTarantoolTemplateRef);
        }
    }

    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Collections.singleton(ReactiveTarantoolRepository.class);
    }

    protected boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
        return metadata.isReactiveRepository();
    }

}
