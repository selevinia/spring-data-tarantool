package org.springframework.data.tarantool.core.convert;

import io.tarantool.driver.api.tuple.TarantoolTuple;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * A mapping converter for Tarantool.
 *
 * @author Tatiana Blinova
 */
public class MappingTarantoolConverter extends AbstractTarantoolConverter {

    private final TarantoolMappingContext mappingContext;
    private final TarantoolReadConverter readConverter;
    private final TarantoolWriteConverter writeConverter;

    public MappingTarantoolConverter() {
        this(newMappingContext());
    }

    public MappingTarantoolConverter(TarantoolMappingContext mappingContext) {
        super(newConversionService());

        Assert.notNull(mappingContext, "TarantoolMappingContext must not be null");
        this.mappingContext = mappingContext;

        TypeMapper<TarantoolTuple> tupleTypeMapper = new TarantoolTupleTypeMapper();
        TypeMapper<Map<String, Object>> mapTypeMapper = new TarantoolMapTypeMapper();

        this.readConverter = new MappingTarantoolReadConverter(this.mappingContext, tupleTypeMapper, mapTypeMapper, this::getInstantiators, this::getCustomConversions, getConversionService());
        this.writeConverter = new MappingTarantoolWriteConverter(this.mappingContext, tupleTypeMapper, mapTypeMapper, this::getCustomConversions, getConversionService());
    }

    @Override
    public TarantoolMappingContext getMappingContext() {
        return mappingContext;
    }

    @Override
    public <R> R read(Class<R> type, Object source) {
        return readConverter.read(type, source);
    }

    @Override
    public void write(Object source, Object sink) {
        writeConverter.write(source, sink);
    }

    @Override
    public Object convertToWritableType(Object source) {
        return writeConverter.convert(source);
    }

    public static TarantoolConverter newConverter() {
        MappingTarantoolConverter converter = new MappingTarantoolConverter();
        converter.afterPropertiesSet();
        return converter;
    }

    private static ConversionService newConversionService() {
        GenericConversionService conversionService = new GenericConversionService();
        DefaultConversionService.addCollectionConverters(conversionService);
        return conversionService;
    }

    private static TarantoolMappingContext newMappingContext() {
        return new TarantoolMappingContext();
    }
}
