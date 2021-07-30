package org.springframework.data.tarantool.integration.core.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Locale;

@ReadingConverter
public class StringToLocaleConverter implements Converter<String, Locale> {

    @Override
    public Locale convert(String source) {
        return Locale.forLanguageTag(source);
    }
}
