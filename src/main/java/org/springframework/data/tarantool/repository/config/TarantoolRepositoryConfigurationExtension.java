package org.springframework.data.tarantool.repository.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.tarantool.core.mapping.Space;
import org.springframework.data.tarantool.repository.TarantoolRepository;
import org.springframework.data.tarantool.repository.support.TarantoolRepositoryFactoryBean;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link RepositoryConfigurationExtension} for Tarantool.
 *
 * @author Alexander Rublev
 */
public class TarantoolRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    @Override
    public String getModuleName() {
        return "Tarantool";
    }

    @Override
    @SuppressWarnings("deprecation")
    protected String getModulePrefix() {
        return "tarantool";
    }

    @Override
    public String getRepositoryFactoryBeanClassName() {
        return TarantoolRepositoryFactoryBean.class.getName();
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
        String tarantoolTemplateRef = config.getAttributes().getString("tarantoolTemplateRef");

        if (StringUtils.hasText(tarantoolTemplateRef)) {
            builder.addPropertyReference("tarantoolOperations", tarantoolTemplateRef);
        }
    }

    @Override
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Collections.singleton(Space.class);
    }

    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Collections.singleton(TarantoolRepository.class);
    }

    protected boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
        return !metadata.isReactiveRepository();
    }

}
