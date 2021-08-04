package org.springframework.data.tarantool.integration.core.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.tarantool.integration.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@UtilityClass
public class TestData {

    public static User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Alexey")
                .lastName("Kuzin")
                .birthDate(LocalDate.now().minusYears(24))
                .age(24)
                .active(true)
                .email("akuzin@mail.ru")
                .address(Address.builder()
                        .city("Kandalaksha")
                        .street("Lenina 12-2")
                        .postcode("123456")
                        .build())
                .build();
    }

    public static Article article() {
        return Article.builder()
                .id(UUID.randomUUID())
                .name("Selevinia eats tarantool")
                .slug("About Selevinia")
                .publishDate(LocalDateTime.now())
                .userId(UUID.randomUUID())
                .tags(List.of(new Tag("selevinia"), new Tag("tarantool")))
                .likes(1)
                .build();
    }

    public static Article simpleArticle() {
        return Article.builder()
                .id(UUID.randomUUID())
                .name("Selevinia eats tarantool")
                .publishDate(LocalDateTime.now())
                .userId(UUID.randomUUID())
                .build();
    }

    public static Article emptyArticle() {
        return Article.builder()
                .id(UUID.randomUUID())
                .build();
    }

    public static TranslatedArticle translatedArticle() {
        return TranslatedArticle.builder()
                .id(
                        TranslatedArticleKey.builder()
                                .articleId(UUID.randomUUID())
                                .locale(Locale.ENGLISH)
                                .build()
                )
                .name("Selevinia eats tarantool")
                .build();
    }

    public static TranslatedArticleWithFlatKey translatedArticleWithFlatKey() {
        return TranslatedArticleWithFlatKey.builder()
                .articleId(UUID.randomUUID())
                .locale(Locale.ENGLISH)
                .name("Selevinia eats tarantool")
                .build();
    }

    public static TranslatedArticleWithMapId translatedArticleWithMapId() {
        return TranslatedArticleWithMapId.builder()
                .articleId(UUID.randomUUID())
                .locale(Locale.ENGLISH)
                .name("Selevinia eats tarantool")
                .build();
    }

    public static Comment comment() {
        return Comment.builder()
                .id(UUID.randomUUID())
                .articleId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .value("This is the best article about tarantool")
                .likes(1)
                .build();
    }

}
