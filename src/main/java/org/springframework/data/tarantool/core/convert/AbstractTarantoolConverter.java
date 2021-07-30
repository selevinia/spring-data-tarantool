package org.springframework.data.tarantool.core.convert;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * Base class for {@link TarantoolConverter} implementations. Sets up a {@link ConversionService} and populates basic
 * converters.
 *
 * @author Tatiana Blinova
 */
public abstract class AbstractTarantoolConverter implements TarantoolConverter, InitializingBean {

    private final ConversionService conversionService;
    private CustomConversions conversions = new TarantoolCustomConversions(Collections.emptyList());
    private EntityInstantiators instantiators = new EntityInstantiators();

    protected AbstractTarantoolConverter(ConversionService conversionService) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        this.conversionService = conversionService;
    }

    public void setInstantiators(EntityInstantiators instantiators) {
        Assert.notNull(instantiators, "EntityInstantiators must not be null");
        this.instantiators = instantiators;
    }

    public EntityInstantiators getInstantiators() {
        return instantiators;
    }

    @Override
    public ConversionService getConversionService() {
        return this.conversionService;
    }

    public void setCustomConversions(CustomConversions conversions) {
        this.conversions = conversions;
    }

    @Override
    public CustomConversions getCustomConversions() {
        return this.conversions;
    }

    @Override
    public void afterPropertiesSet() {
        if (conversionService instanceof ConverterRegistry) {
            conversions.registerConvertersIn((ConverterRegistry) conversionService);
        }
    }
}
