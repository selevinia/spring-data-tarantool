package org.springframework.data.tarantool.config;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.tarantool.config.client.DefaultTarantoolClientFactory;
import org.springframework.data.tarantool.config.client.DefaultTarantoolClientOptions;
import org.springframework.data.tarantool.config.client.TarantoolClientFactory;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.core.DefaultTarantoolExceptionTranslator;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.core.ReactiveTarantoolTemplate;
import org.springframework.data.tarantool.core.TarantoolExceptionTranslator;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolCustomConversions;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;

/**
 * Base class for reactive Spring Data Tarantool configuration using JavaConfig.
 *
 * @author Alexander Rublev
 * @see TarantoolConfigurationSupport
 */
@Configuration(proxyBeanMethods = false)
public abstract class AbstractReactiveTarantoolConfiguration extends TarantoolConfigurationSupport {

    /**
     * Creates {@link ReactiveTarantoolOperations}.
     *
     * @return never {@literal null}.
     * @see #tarantoolClientOptions()
     * @see #tarantoolConverter(TarantoolMappingContext, TarantoolCustomConversions)
     * @see #tarantoolExceptionTranslator()
     */
    @Bean
    public ReactiveTarantoolTemplate reactiveTarantoolTemplate(TarantoolClientFactory tarantoolClientFactory,
                                                               TarantoolConverter tarantoolConverter,
                                                               TarantoolExceptionTranslator tarantoolExceptionTranslator) {
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient = tarantoolClientFactory.createClient();
        return new ReactiveTarantoolTemplate(tarantoolClient, tarantoolConverter, tarantoolExceptionTranslator);
    }

    /**
     * Creates {@link TarantoolClientFactory} to produce Tarantool Client using driver
     */
    @Bean
    public TarantoolClientFactory tarantoolClientFactory(TarantoolClientOptions tarantoolClientOptions) {
        return new DefaultTarantoolClientFactory(tarantoolClientOptions);
    }

    /**
     * Creates {@link TarantoolClientOptions} default implementation. Override this method to provide real options
     *
     * @see #tarantoolClientFactory(TarantoolClientOptions)
     */
    @Bean
    public TarantoolClientOptions tarantoolClientOptions() {
        return new DefaultTarantoolClientOptions();
    }

    /**
     * Creates a {@link MappingTarantoolConverter} instance for the specified type conversions
     */
    @Bean
    public TarantoolConverter tarantoolConverter(TarantoolMappingContext tarantoolMappingContext, TarantoolCustomConversions tarantoolCustomConversions) {
        MappingTarantoolConverter converter = new MappingTarantoolConverter(tarantoolMappingContext);
        converter.setCustomConversions(tarantoolCustomConversions);
        converter.afterPropertiesSet();
        return converter;
    }

    /**
     * Creates the default driver-to-Spring exception translator
     */
    @Bean
    public TarantoolExceptionTranslator tarantoolExceptionTranslator() {
        return new DefaultTarantoolExceptionTranslator();
    }
}
