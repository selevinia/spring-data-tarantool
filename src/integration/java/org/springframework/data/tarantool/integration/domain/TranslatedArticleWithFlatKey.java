package org.springframework.data.tarantool.integration.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.tarantool.core.mapping.Field;
import org.springframework.data.tarantool.core.mapping.PrimaryKeyField;
import org.springframework.data.tarantool.core.mapping.Space;

import java.util.Locale;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(of = {"articleId", "locale"})
@Space("translated_articles")
public class TranslatedArticleWithFlatKey {
    @PrimaryKeyField("article_id")
    private UUID articleId;
    @PrimaryKeyField
    private Locale locale;

    @Field("article_name")
    private String name;
    @Field("article_text")
    private String text;
}
