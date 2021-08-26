package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
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
import java.util.Optional;

/**
 * {@link PropertyValueProvider} to read property values from a TarantoolTuple
 *
 * @author Tatiana Blinova
 */
public class TarantoolTuplePropertyValueProvider extends AbstractTarantoolPropertyValueProvider {

    private final TarantoolTuple tarantoolTuple;

    public TarantoolTuplePropertyValueProvider(TarantoolTuple tarantoolTuple,
                                               TarantoolMappingContext mappingContext,
                                               TypeMapper<Map<String, Object>> mapTypeMapper,
                                               EntityInstantiators instantiators,
                                               CustomConversions conversions,
                                               ConversionService conversionService) {
        super(mappingContext, mapTypeMapper, instantiators, conversions, conversionService);

        Assert.notNull(tarantoolTuple, "Tuple object must not be null");

        this.tarantoolTuple = tarantoolTuple;
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
                ((Map<String, Object>) propertyValue).put(fieldName, tarantoolTuple.getObject(fieldName).orElse(null));
            });
        } else {
            String fieldName = property.getFieldName();
            if (propertyType.isCollectionLike()) {
                if (propertyType.getType().equals(byte[].class)) {
                    propertyValue = tarantoolTuple.getByteArray(fieldName);
                } else {
                    propertyValue = tarantoolTuple.getList(fieldName);
                }
            } else if (propertyType.isMap()) {
                propertyValue = tarantoolTuple.getMap(fieldName);
            } else {
                Class<?> propertyClass = propertyType.getType();
                Optional<Class<?>> customTargetClass = conversions.getCustomWriteTarget(propertyClass);
                boolean canUseCustomTargetClass = customTargetClass.isPresent() && conversions.hasCustomReadTarget(customTargetClass.get(), propertyClass);

                if (canUseCustomTargetClass && tarantoolTuple.canGetObject(fieldName, customTargetClass.get())) {
                    propertyValue = tarantoolTuple.getObject(fieldName, customTargetClass.get()).orElse(null);
                } else if (tarantoolTuple.canGetObject(fieldName, Map.class)) {
                    Map<String, Object> map = (Map<String, Object>) tarantoolTuple.getMap(fieldName);
                    propertyValue = map != null ? convertCustomType(map, propertyType) : null;
                } else {
                    propertyValue = tarantoolTuple.getObject(fieldName).orElse(null);
                }
            }
        }
        return readValue(propertyValue, propertyType);
    }
}
