package org.springframework.data.tarantool.integration.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.data.tarantool.config.client.DefaultTarantoolClientFactory;
import org.springframework.data.tarantool.config.client.TarantoolClientFactory;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.core.DefaultTarantoolExceptionTranslator;
import org.springframework.data.tarantool.core.TarantoolExceptionTranslator;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolCustomConversions;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;

import java.util.List;

@UtilityClass
public class TestConfigProvider {

    public static TarantoolClientFactory clientFactory(TarantoolClientOptions clientOptions) {
        return new DefaultTarantoolClientFactory(clientOptions);
    }

    public static TarantoolMappingContext mappingContext() {
        TarantoolMappingContext mappingContext = new TarantoolMappingContext();
        mappingContext.setFieldNamingStrategy(new SnakeCaseFieldNamingStrategy());
        mappingContext.afterPropertiesSet();
        return mappingContext;
    }

    public static MappingTarantoolConverter converter(TarantoolMappingContext mappingContext) {
        MappingTarantoolConverter converter = new MappingTarantoolConverter(mappingContext);
        converter.setCustomConversions(new TarantoolCustomConversions(List.of(new LocaleToStringConverter(), new StringToLocaleConverter())));
        converter.afterPropertiesSet();
        return converter;
    }

    public static TarantoolExceptionTranslator exceptionTranslator() {
        return new DefaultTarantoolExceptionTranslator();
    }
}
