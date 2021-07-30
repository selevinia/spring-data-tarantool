package org.springframework.data.tarantool.integration.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.tarantool.core.mapping.*;
import org.springframework.lang.NonNull;

import java.util.Locale;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(of = {"articleId", "locale"})
@Space("translated_articles")
public class TranslatedArticleWithMapId implements MapIdentifiable {
    @PrimaryKeyField("article_id")
    private UUID articleId;
    @PrimaryKeyField
    private Locale locale;

    @Field("article_name")
    private String name;
    @Field("article_text")
    private String text;

    @NonNull
    @Override
    public MapId getMapId() {
        return BasicMapId.id("articleId", getArticleId()).with("locale", getLocale());
    }
}
