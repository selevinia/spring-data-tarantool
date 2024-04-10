package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mapping.model.ConvertingPropertyAccessor;
import org.springframework.data.tarantool.core.mapping.BasicTarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentEntity;
import org.springframework.data.tarantool.core.mapping.TarantoolPersistentProperty;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Mapping converter for Tarantool for writing objects to tuples or map
 *
 * @author Tatiana Blinova
 */
public class MappingTarantoolWriteConverter implements TarantoolWriteConverter {

    private final TarantoolMappingContext mappingContext;
    private final TypeMapper<TarantoolTuple> tupleTypeMapper;
    private final TypeMapper<Map<String, Object>> mapTypeMapper;
    private final Supplier<CustomConversions> conversions;
    private final ConversionService conversionService;

    public MappingTarantoolWriteConverter(TarantoolMappingContext mappingContext,
                                          TypeMapper<TarantoolTuple> tupleTypeMapper,
                                          TypeMapper<Map<String, Object>> mapTypeMapper,
                                          Supplier<CustomConversions> conversions,
                                          ConversionService conversionService) {
        this.tupleTypeMapper = tupleTypeMapper;
        this.mappingContext = mappingContext;
        this.mapTypeMapper = mapTypeMapper;
        this.conversions = conversions;
        this.conversionService = conversionService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(Object source, Object target) {
        Assert.notNull(source, "Source object must not be null");
        Assert.notNull(target, "Target object must not be null");

        if (target instanceof TarantoolTuple) {
            write(source, (TarantoolTuple) target);
        } else if (source instanceof Collection && target instanceof Collection) {
            write((Collection<Object>) source, (Collection<Object>) target);
        } else {
            throw new MappingException(String.format("Unknown write target [%s]", ObjectUtils.nullSafeClassName(target)));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convert(Object source) {
        Assert.notNull(source, "Source object must not be null");

        if (source instanceof Collection) {
            return convertCollection((Collection<Object>) source);
        } else {
            return convertValue(source);
        }
    }

    private void write(Object source, TarantoolTuple target) {
        Optional<Class<?>> customTarget = conversions.get().getCustomWriteTarget(source.getClass(), target.getClass());
        customTarget.ifPresentOrElse(ct -> {
            TarantoolTuple result = conversionService.convert(source, TarantoolTuple.class);
            if (result == null) {
                throw new MappingException("Couldn't convert source object to TarantoolTuple with custom conversions");
            }
            target.getFields().clear();
            target.getFields().addAll(result.getFields());
            tupleTypeMapper.writeType(ClassUtils.getUserClass(source.getClass()), target);
        }, () -> {
            TarantoolPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(source.getClass());
            ConvertingPropertyAccessor<?> accessor = new ConvertingPropertyAccessor<>(entity.getPropertyAccessor(source), conversionService);
            convertProperties(entity, accessor).forEach(target::putObject);
        });
    }

    private void write(Collection<Object> source, Collection<Object> target) {
        target.clear();
        target.addAll(convertCollection(source));
    }

    private Map<String, Object> convertProperties(TarantoolPersistentEntity<?> entity, ConvertingPropertyAccessor<?> accessor) {
        Map<String, Object> convertedProperties = new HashMap<>();

        entity.doWithProperties((PropertyHandler<TarantoolPersistentProperty>) property -> {
            Object value = accessor.getProperty(property);
            if (!property.isWritable() || value == null) {
                return;
            }

            if (property.isCompositePrimaryKey()) {
                BasicTarantoolPersistentEntity<?> pkEntity = mappingContext.getRequiredPersistentEntity(property.getType());
                ConvertingPropertyAccessor<?> pkAccessor = new ConvertingPropertyAccessor<>(pkEntity.getPropertyAccessor(value), conversionService);
                Map<String, Object> pkProperties = convertProperties(pkEntity, pkAccessor);
                convertedProperties.putAll(pkProperties);
                return;
            }

            Object valueToWrite = getValueToWrite(value, property.getTypeInformation());
            if (valueToWrite != null) {
                convertedProperties.put(property.getFieldName(), valueToWrite);
            }
        });

        return convertedProperties;
    }

    @Nullable
    private Object getValueToWrite(@Nullable Object value, @Nullable TypeInformation<?> type) {
        if (value == null) {
            return null;
        } else if (type == null || conversions.get().isSimpleType(value.getClass())) {
            return getSimpleValueToWrite(value);
        } else {
            return getNonSimpleValueToWrite(value, type);
        }
    }

    @SuppressWarnings("rawtypes")
    private Object getSimpleValueToWrite(Object value) {
        return conversions.get().getCustomWriteTarget(value.getClass())
                .map(aClass -> (Object) conversionService.convert(value, aClass))
                .orElseGet(() -> {
                    if (Enum.class.isAssignableFrom(value.getClass())) {
                        return ((Enum) value).name();
                    } else {
                        return value;
                    }
                });
    }

    private Object getNonSimpleValueToWrite(Object value, TypeInformation<?> type) {
        TypeInformation<?> valueType = TypeInformation.of(value.getClass());
        if (valueType.isCollectionLike()) {
            return convertCollection(asCollection(value), type);
        } else if (valueType.isMap()) {
            return convertMap(asMap(value), type);
        } else {
            return conversions.get().getCustomWriteTarget(value.getClass())
                    .map(aClass -> (Object) conversionService.convert(value, aClass))
                    .orElseGet(() -> convertCustomType(value, type));
        }
    }

    private Collection<?> asCollection(final Object source) {
        if (source instanceof Collection) {
            return (Collection<?>) source;
        }
        return source.getClass().isArray() ? CollectionUtils.arrayToList(source) : Collections.singleton(source);
    }

    @SuppressWarnings("unchecked")
    private Map<Object, Object> asMap(Object value) {
        return (Map<Object, Object>) value;
    }

    @Nullable
    private Object convertValue(@Nullable Object source) {
        if (source == null) {
            return null;
        }
        return convertValue(source, source.getClass());
    }

    @Nullable
    private Object convertValue(@Nullable Object source, Class<?> targetClass) {
        if (source == null) {
            return null;
        }
        return getValueToWrite(source, getTypeInformation(targetClass));
    }

    private List<Object> convertCollection(Collection<?> source) {
        return source.stream().map(this::convertValue).collect(Collectors.toList());
    }

    private List<Object> convertCollection(final Collection<?> source, final TypeInformation<?> type) {
        Assert.notNull(source, "Source collection must not be null");
        Assert.notNull(type, "Type must not be null");

        TypeInformation<?> componentType = type.getComponentType();
        return source.stream().map(element -> getValueToWrite(element, componentType)).collect(Collectors.toList());
    }

    private Map<String, Object> convertMap(final Map<Object, Object> source, final TypeInformation<?> type) {
        Assert.notNull(source, "Source map must not be null");
        Assert.notNull(type, "Type must not be null");

        return source.entrySet().stream().collect(HashMap::new, (m, e) -> {
            Object key = e.getKey();
            Object value = e.getValue();
            if (!conversions.get().isSimpleType(key.getClass())) {
                throw new MappingException("Cannot use a complex object as a key value");
            }
            String simpleKey = key.toString();
            Object convertedValue = getValueToWrite(value, type.getMapValueType());
            m.put(simpleKey, convertedValue);
        }, HashMap::putAll);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertCustomType(Object source, TypeInformation<?> type) {
        Assert.notNull(source, "Source object must not be null");
        Assert.notNull(type, "Type must not be null");

        Optional<Class<?>> customTarget = conversions.get().getCustomWriteTarget(source.getClass(), type.getType());
        return customTarget.map(ct -> {
            Map<String, Object> result = conversionService.convert(source, Map.class);
            if (result == null) {
                throw new MappingException("Couldn't convert source object to Map with custom conversions");
            }
            mapTypeMapper.writeType(ClassUtils.getUserClass(source.getClass()), result);
            return result;
        }).orElseGet(() -> {
            TarantoolPersistentEntity<?> entity = mappingContext.getRequiredPersistentEntity(source.getClass());
            ConvertingPropertyAccessor<?> accessor = new ConvertingPropertyAccessor<>(entity.getPropertyAccessor(source), conversionService);
            return convertProperties(entity, accessor);
        });
    }

    private TypeInformation<?> getTypeInformation(Class<?> cls) {
        TarantoolPersistentEntity<?> entity = mappingContext.getPersistentEntity(cls);
        if (entity == null) {
            return TypeInformation.of(cls);
        } else {
            return entity.getTypeInformation();
        }
    }
}
