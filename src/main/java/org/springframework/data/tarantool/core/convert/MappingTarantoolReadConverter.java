package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.model.*;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Mapping converter for Tarantool for reading objects from tuples or map
 *
 * @author Tatiana Blinova
 */
public class MappingTarantoolReadConverter implements TarantoolReadConverter {

    private final TarantoolMappingContext mappingContext;
    private final TypeMapper<TarantoolTuple> tupleTypeMapper;
    private final TypeMapper<Map<String, Object>> mapTypeMapper;
    private final Supplier<EntityInstantiators> instantiators;
    private final Supplier<CustomConversions> conversions;
    private final ConversionService conversionService;

    public MappingTarantoolReadConverter(TarantoolMappingContext mappingContext,
                                         TypeMapper<TarantoolTuple> tupleTypeMapper,
                                         TypeMapper<Map<String, Object>> mapTypeMapper,
                                         Supplier<EntityInstantiators> instantiators,
                                         Supplier<CustomConversions> conversions,
                                         ConversionService conversionService) {
        this.mappingContext = mappingContext;
        this.tupleTypeMapper = tupleTypeMapper;
        this.mapTypeMapper = mapTypeMapper;
        this.instantiators = instantiators;
        this.conversions = conversions;
        this.conversionService = conversionService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R read(Class<R> type, Object source) {
        Assert.notNull(source, "Source object must not be null");

        TypeInformation<? extends R> typeInformation;
        PropertyValueProvider<TarantoolPersistentProperty> provider;
        if (source instanceof TarantoolTuple) {
            typeInformation = tupleTypeMapper.readType((TarantoolTuple) source, ClassTypeInformation.from(type));

            Class<? extends R> rawType = typeInformation.getType();
            if (conversions.get().hasCustomReadTarget(TarantoolTuple.class, rawType)) {
                R result = conversionService.convert(source, rawType);
                if (result == null) {
                    throw new MappingException(String.format("Couldn't convert source TarantoolTuple to %s with custom conversions", rawType.getSimpleName()));
                }
                return result;
            } else {
                provider = new TarantoolTuplePropertyValueProvider((TarantoolTuple) source, mappingContext, mapTypeMapper, instantiators.get(), conversions.get(), conversionService);
            }
        } else if (source instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) source;
            typeInformation = mapTypeMapper.readType(map, ClassTypeInformation.from(type));
            provider = new TarantoolMapPropertyValueProvider(map, mappingContext, mapTypeMapper, instantiators.get(), conversions.get(), conversionService);
        } else {
            throw new MappingException(String.format("Couldn't read from object of type %s", source.getClass()));
        }

        TarantoolPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(typeInformation);
        ConvertingPropertyAccessor<?> accessor = getConvertingPropertyAccessor(entity, provider);

        entity.doWithProperties(new MappingTarantoolPropertyHandler(entity, provider, accessor));

        return (R) accessor.getBean();
    }

    private ConvertingPropertyAccessor<?> getConvertingPropertyAccessor(TarantoolPersistentEntity<?> entity, PropertyValueProvider<TarantoolPersistentProperty> propertyValueProvider) {
        EntityInstantiator instantiator = instantiators.get().getInstantiatorFor(entity);
        ParameterValueProvider<TarantoolPersistentProperty> provider = new PersistentEntityParameterValueProvider<>(entity, propertyValueProvider, null);
        Object instance = instantiator.createInstance(entity, provider);
        PersistentPropertyAccessor<?> accessor = entity.getPropertyAccessor(instance);

        return new ConvertingPropertyAccessor<>(accessor, conversionService);
    }
}
