package org.springframework.data.tarantool.config;

import io.tarantool.driver.api.TarantoolClient;
import io.tarantool.driver.api.TarantoolResult;
import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.tarantool.config.client.TarantoolClientFactory;
import org.springframework.data.tarantool.core.TarantoolExceptionTranslator;
import org.springframework.data.tarantool.core.TarantoolOperations;
import org.springframework.data.tarantool.core.TarantoolTemplate;
import org.springframework.data.tarantool.core.convert.TarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolCustomConversions;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;

/**
 * Base class for Spring Data Tarantool configuration using JavaConfig.
 *
 * @author Alexander Rublev
 * @see TarantoolConfigurationSupport
 */
@Configuration(proxyBeanMethods = false)
public abstract class AbstractTarantoolConfiguration extends TarantoolConfigurationSupport {

    /**
     * Creates {@link TarantoolOperations}.
     *
     * @param tarantoolClientFactory       {@link TarantoolClientFactory} instance to use
     * @param tarantoolConverter           {@link TarantoolConverter} instance to use
     * @param tarantoolExceptionTranslator {@link TarantoolExceptionTranslator} instance to use
     * @return never {@literal null}.
     * @see #tarantoolClientOptions()
     * @see #tarantoolConverter(TarantoolMappingContext, TarantoolCustomConversions)
     * @see #tarantoolExceptionTranslator()
     */
    @Bean
    public TarantoolTemplate tarantoolTemplate(TarantoolClientFactory tarantoolClientFactory,
                                               TarantoolConverter tarantoolConverter,
                                               TarantoolExceptionTranslator tarantoolExceptionTranslator) {
        TarantoolClient<TarantoolTuple, TarantoolResult<TarantoolTuple>> tarantoolClient = tarantoolClientFactory.createClient();
        return new TarantoolTemplate(tarantoolClient, tarantoolConverter, tarantoolExceptionTranslator);
    }

}
