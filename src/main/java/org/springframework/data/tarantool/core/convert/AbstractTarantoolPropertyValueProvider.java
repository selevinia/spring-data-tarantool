package org.springframework.data.tarantool.core.convert;

import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.model.*;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Abstract {@link PropertyValueProvider} to read property values from objects from tarantool
 *
 * @author Tatiana Blinova
 */
public abstract class AbstractTarantoolPropertyValueProvider implements PropertyValueProvider<TarantoolPersistentProperty> {

    protected final TarantoolMappingContext mappingContext;
    protected final TypeMapper<Map<String, Object>> mapTypeMapper;
    protected final EntityInstantiators instantiators;
    protected final CustomConversions conversions;
    protected final ConversionService conversionService;

    public AbstractTarantoolPropertyValueProvider(TarantoolMappingContext mappingContext,
                                                  TypeMapper<Map<String, Object>> mapTypeMapper,
                                                  EntityInstantiators instantiators,
                                                  CustomConversions conversions,
                                                  ConversionService conversionService) {
        this.mappingContext = mappingContext;
        this.mapTypeMapper = mapTypeMapper;
        this.instantiators = instantiators;
        this.conversions = conversions;
        this.conversionService = conversionService;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <R> R readValue(@Nullable Object propertyValue, TypeInformation<?> propertyType) {
        Assert.notNull(propertyType, "Target type must not be null");

        if (propertyValue == null) {
            return null;
        }

        Class<?> targetClass = propertyType.getType();
        boolean hasCustomReadTarget = conversions.hasCustomReadTarget(propertyValue.getClass(), targetClass);
        boolean canConvertSimpleType = conversions.isSimpleType(targetClass) && conversionService.canConvert(propertyValue.getClass(), targetClass);
        if (hasCustomReadTarget || canConvertSimpleType) {
            return (R) conversionService.convert(propertyValue, targetClass);
        } else if (propertyType.isCollectionLike()) {
            return convertCollection(asCollection(propertyValue), propertyType);
        } else if (propertyType.isMap()) {
            return convertMap((Map<String, Object>) propertyValue, propertyType);
        }
        return (R) convertIfNeeded(propertyValue, propertyType);
    }

    @SuppressWarnings("unchecked")
    protected <R> R convertCustomType(Map<String, Object> propertyValue, TypeInformation<?> propertyType) {
        PropertyValueProvider<TarantoolPersistentProperty> propertyValueProvider = new PropertyValueProvider<>() {
            @Override
            public <T> T getPropertyValue(TarantoolPersistentProperty property) {
                TypeInformation<?> propType = property.getTypeInformation();
                return readValue(propertyValue.get(property.getFieldName()), propType);
            }
        };

        TypeInformation<?> typeToUse = mapTypeMapper.readType(propertyValue, propertyType);
        TarantoolPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(typeToUse);
        ParameterValueProvider<TarantoolPersistentProperty> parameterValueProvider = new PersistentEntityParameterValueProvider<>(entity, propertyValueProvider, null);
        Object instance = instantiators.getInstantiatorFor(entity).createInstance(entity, parameterValueProvider);
        PersistentPropertyAccessor<?> accessor = new ConvertingPropertyAccessor<>(entity.getPropertyAccessor(instance), conversionService);

        entity.doWithProperties(new MappingTarantoolPropertyHandler(entity, propertyValueProvider, accessor));

        return (R) accessor.getBean();
    }

    private Collection<?> asCollection(Object source) {
        if (source instanceof Collection) {
            return (Collection<?>) source;
        }
        return source.getClass().isArray() ? CollectionUtils.arrayToList(source) : Collections.singleton(source);
    }

    @SuppressWarnings("unchecked")
    private <R> R convertCollection(Collection<?> propertyValue, TypeInformation<?> propertyType) {
        Class<?> collectionClass = propertyType.getType();
        TypeInformation<?> elementType = propertyType.getComponentType();
        if (elementType == null) {
            throw new MappingException(String.format("Couldn't get element type for collection of type %s", propertyType.getType().getSimpleName()));
        }

        Class<?> elementClass = elementType.getType();
        Collection<Object> items = collectionClass.isArray() ? new ArrayList<>() : CollectionFactory.createCollection(collectionClass, elementClass, propertyValue.size());

        propertyValue.forEach(item -> items.add(readValue(item, elementType)));

        return (R) convertIfNeeded(items, propertyType);
    }

    @SuppressWarnings("unchecked")
    private <R> R convertMap(Map<String, Object> propertyValue, TypeInformation<?> propertyType) {
        Class<?> mapClass = propertyType.getType();
        TypeInformation<?> keyType = propertyType.getComponentType();
        Class<?> keyClass = keyType == null ? null : keyType.getType();
        TypeInformation<?> mapValueType = propertyType.getMapValueType();
        if (mapValueType == null) {
            throw new MappingException(String.format("Couldn't get map value type for map of type %s", propertyType.getType().getSimpleName()));
        }

        Map<Object, Object> converted = CollectionFactory.createMap(mapClass, keyClass, propertyValue.keySet().size());
        propertyValue.forEach((key, value) -> converted.put(key, readValue(value, mapValueType)));

        return (R) convertIfNeeded(converted, propertyType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertIfNeeded(Object propertyValue, TypeInformation<?> propertyType) {
        Class<?> targetClass = propertyType.getType();
        if (Enum.class.isAssignableFrom(targetClass)) {
            return Enum.valueOf((Class<Enum>) targetClass, propertyValue.toString());
        } else if (propertyValue instanceof Map && !propertyType.isMap()) {
            return convertCustomType((Map<String, Object>) propertyValue, propertyType);
        } else {
            return propertyValue;
        }
    }
}
