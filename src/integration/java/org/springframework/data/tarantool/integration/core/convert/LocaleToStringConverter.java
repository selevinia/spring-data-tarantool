package org.springframework.data.tarantool.integration.core.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.Locale;

@WritingConverter
public class LocaleToStringConverter implements Converter<Locale, String> {

    @Override
    public String convert(Locale source) {
        return source.toLanguageTag();
    }
}
