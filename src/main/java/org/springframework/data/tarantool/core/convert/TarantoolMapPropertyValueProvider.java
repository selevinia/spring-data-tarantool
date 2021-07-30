package org.springframework.data.tarantool.core.convert;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.model.EntityInstantiators;
import org.springframework.data.mapping.model.PropertyValueProvider;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link PropertyValueProvider} to read property values from a Map
 *
 * @author Tatiana Blinova
 */
public class TarantoolMapPropertyValueProvider extends AbstractTarantoolPropertyValueProvider {

    private final Map<String, Object> tarantoolMap;

    public TarantoolMapPropertyValueProvider(Map<String, Object> tarantoolMap,
                                             TarantoolMappingContext mappingContext,
                                             TypeMapper<Map<String, Object>> mapTypeMapper,
                                             EntityInstantiators instantiators,
                                             CustomConversions conversions,
                                             ConversionService conversionService) {
        super(mappingContext, mapTypeMapper, instantiators, conversions, conversionService);

        Assert.notNull(tarantoolMap, "Map object must not be null");

        this.tarantoolMap = tarantoolMap;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <R> R getPropertyValue(TarantoolPersistentProperty property) {
        TypeInformation<?> propertyType = property.getTypeInformation();

        Object propertyValue;
        if (property.isCompositePrimaryKey()) {
            propertyValue = new HashMap<String, Object>();
            mappingContext.getRequiredPersistentEntity(property.getType()).forEach(idProperty -> {
                String fieldName = idProperty.getFieldName();
                ((Map<String, Object>) propertyValue).put(fieldName, tarantoolMap.get(fieldName));
            });
        } else {
            String fieldName = property.getFieldName();
            propertyValue = tarantoolMap.get(fieldName);
        }

        return readValue(propertyValue, propertyType);
    }
}
