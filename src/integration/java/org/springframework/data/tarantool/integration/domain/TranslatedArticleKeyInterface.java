package org.springframework.data.tarantool.integration.domain;

import java.util.Locale;
import java.util.UUID;

public interface TranslatedArticleKeyInterface {
    TranslatedArticleKeyInterface setArticleId(UUID articleId);

    UUID getArticleId();

    TranslatedArticleKeyInterface setLocale(Locale locale);

    Locale getLocale();
}
