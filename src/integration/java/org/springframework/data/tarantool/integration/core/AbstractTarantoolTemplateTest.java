package org.springframework.data.tarantool.integration.core;

import io.tarantool.driver.TarantoolVersion;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.protocol.TarantoolIndexQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.tarantool.TarantoolDataRetrievalException;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.core.TarantoolTemplate;
import org.springframework.data.tarantool.core.mapping.BasicMapId;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.MapIdFactory;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.integration.domain.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.tarantool.integration.core.util.AssertConsumer.articleAssertConsumer;
import static org.springframework.data.tarantool.integration.core.util.TestConfigProvider.*;
import static org.springframework.data.tarantool.integration.core.util.TestData.*;

/**
 * Class contains all test methods for Tarantool template usage
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTarantoolTemplateTest {

    private TarantoolTemplate tarantoolTemplate;

    public abstract TarantoolClientOptions getOptions();

    @BeforeAll
    void setUp() {
        TarantoolMappingContext mappingContext = mappingContext();
        tarantoolTemplate = new TarantoolTemplate(clientFactory(getOptions()).createClient(), converter(mappingContext), exceptionTranslator());
    }

    @BeforeEach
    void clear() {
        tarantoolTemplate.delete(Conditions.any(), Article.class);
        tarantoolTemplate.delete(Conditions.any(), User.class);
        tarantoolTemplate.delete(Conditions.any(), Comment.class);
        tarantoolTemplate.delete(Conditions.any(), TranslatedArticle.class);
    }

    @Test
    void shouldGetVersion() {
        TarantoolVersion version = tarantoolTemplate.getVersion();
        assertThat(version).isNotNull();
        assertThat(version.toString()).contains("Tarantool 2.9.0");
    }

    @Test
    void shouldInsertArticle() {
        Article article = article();

        Article selected = tarantoolTemplate.selectById(article.getId(), Article.class);
        assertThat(selected).isNull();

        selected = tarantoolTemplate.insert(article, Article.class);
        assertThat(selected).isEqualTo(article);

        selected = tarantoolTemplate.selectById(article.getId(), Article.class);
        assertThat(selected).isEqualTo(article);
    }

    @Test
    void shouldNotInsertEmptyArticle() {
        Article article = emptyArticle();

        assertThatThrownBy(() -> tarantoolTemplate.insert(article, Article.class))
                .isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageEndingWith("required by space format is missing");
    }

    @Test
    void shouldInsertTranslatedArticle() {
        TranslatedArticle translatedArticle = translatedArticle();
        TranslatedArticleKey translatedArticleId = translatedArticle.getId();

        TranslatedArticle selected = tarantoolTemplate.selectById(translatedArticleId, TranslatedArticle.class);
        assertThat(selected).isNull();

        selected = tarantoolTemplate.insert(translatedArticle, TranslatedArticle.class);
        assertThat(selected).isEqualTo(translatedArticle);

        selected = tarantoolTemplate.selectById(translatedArticleId, TranslatedArticle.class);
        assertWith(selected, articleAssertConsumer(translatedArticle));
    }

    @Test
    void shouldInsertTranslatedArticleWithFlatKey() {
        TranslatedArticleWithFlatKey translatedArticle = translatedArticleWithFlatKey();
        MapId translatedArticleMapId = MapIdFactory.id(MapId.class)
                .with("articleId", translatedArticle.getArticleId())
                .with("locale", translatedArticle.getLocale());

        TranslatedArticleWithFlatKey selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithFlatKey.class);
        assertThat(selected).isNull();

        selected = tarantoolTemplate.insert(translatedArticle, TranslatedArticleWithFlatKey.class);
        assertThat(selected).isEqualTo(translatedArticle);

        selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithFlatKey.class);
        assertWith(selected, articleAssertConsumer(translatedArticle));
    }

    @Test
    void shouldInsertTranslatedArticleWithMapId() {
        TranslatedArticleWithMapId translatedArticle = translatedArticleWithMapId();
        MapId translatedArticleMapId = translatedArticle.getMapId();

        TranslatedArticleWithMapId selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithMapId.class);
        assertThat(selected).isNull();

        selected = tarantoolTemplate.insert(translatedArticle, TranslatedArticleWithMapId.class);
        assertThat(selected).isEqualTo(translatedArticle);

        selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithMapId.class);
        assertWith(selected, articleAssertConsumer(translatedArticle));
    }

    @Test
    void shouldReplaceArticle() {
        Article article = simpleArticle();

        Article inserted = tarantoolTemplate.insert(article, Article.class);
        assertThat(inserted).isEqualTo(article);

        article.setLikes(10);
        inserted = tarantoolTemplate.replace(article, Article.class);
        assertThat(inserted).isEqualTo(article);
    }

    @Test
    void shouldInsertArticleWithReplace() {
        Article article = simpleArticle();

        Article replaced = tarantoolTemplate.replace(article, Article.class);
        assertThat(replaced).isEqualTo(article);

        Article selected = tarantoolTemplate.selectById(article.getId(), Article.class);
        assertThat(selected).isEqualTo(article);
    }

    @Test
    void shouldUpdateArticle() {
        Article article = article();

        Article inserted = tarantoolTemplate.insert(article, Article.class);
        assertThat(inserted).isEqualTo(article);

        Article selected = tarantoolTemplate.selectById(article.getId(), Article.class);
        assertThat(selected).isEqualTo(article);

        Article partialArticle = Article.builder()
                .tags(List.of(new Tag("test")))
                .likes(article.getLikes() + 1)
                .build();
        article.setTags(partialArticle.getTags());
        article.setLikes(partialArticle.getLikes());
        List<Article> updated = tarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(article.getId())), partialArticle, Article.class);
        assertWith(updated.get(0), articleAssertConsumer(article));
    }

    @Test
    void shouldUpdateArticles() {
        Article article1 = article();
        Article article2 = article();

        tarantoolTemplate.insert(article1, Article.class);
        tarantoolTemplate.insert(article2, Article.class);

        Article partialArticle = Article.builder()
                .slug("Single slug about tarantool")
                .build();

        List<Article> updated = tarantoolTemplate.update(Conditions.any(), partialArticle, Article.class);
        assertThat(updated).hasSize(2);
        updated.forEach(article -> assertThat(article.getSlug()).isEqualTo(partialArticle.getSlug()));
    }

    @Test
    void shouldUpdateTranslatedArticle() {
        TranslatedArticle translatedArticle = translatedArticle();

        TranslatedArticle inserted = tarantoolTemplate.insert(translatedArticle, TranslatedArticle.class);
        assertThat(inserted).isEqualTo(translatedArticle);

        TranslatedArticle selected = tarantoolTemplate.selectById(translatedArticle.getId(), TranslatedArticle.class);
        assertThat(selected).isEqualTo(translatedArticle);

        TranslatedArticle partialTranslatedArticle = TranslatedArticle.builder()
                .text("Simple article text")
                .build();
        List<? extends Serializable> translatedArticleIdParts = List.of(translatedArticle.getId().getArticleId(), translatedArticle.getId().getLocale().toLanguageTag());
        List<TranslatedArticle> updated = tarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, translatedArticleIdParts), partialTranslatedArticle, TranslatedArticle.class);
        assertWith(updated.get(0), result -> {
            assertThat(result.getId()).isEqualTo(translatedArticle.getId());
            assertThat(result.getName()).isEqualTo(translatedArticle.getName());
            assertThat(result.getText()).isNotEqualTo(translatedArticle.getText());
            assertThat(result.getText()).isEqualTo(partialTranslatedArticle.getText());
        });
    }

    @Test
    void shouldUpdateTranslatedArticleWithFlatKey() {
        TranslatedArticleWithFlatKey translatedArticle = translatedArticleWithFlatKey();
        MapId translatedArticleMapId = MapIdFactory.id(MapId.class)
                .with("articleId", translatedArticle.getArticleId())
                .with("locale", translatedArticle.getLocale());

        TranslatedArticleWithFlatKey inserted = tarantoolTemplate.insert(translatedArticle, TranslatedArticleWithFlatKey.class);
        assertThat(inserted).isEqualTo(translatedArticle);

        TranslatedArticleWithFlatKey selected = tarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithFlatKey.class);
        assertThat(selected).isEqualTo(translatedArticle);

        TranslatedArticleWithFlatKey partialTranslatedArticle = TranslatedArticleWithFlatKey.builder()
                .text("Simple article text")
                .build();
        List<? extends Serializable> translatedArticleIdParts = List.of(translatedArticle.getArticleId(), translatedArticle.getLocale().toLanguageTag());
        List<TranslatedArticleWithFlatKey> updated = tarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, translatedArticleIdParts), partialTranslatedArticle, TranslatedArticleWithFlatKey.class);
        assertWith(updated.get(0), result -> {
            assertThat(result.getArticleId()).isEqualTo(translatedArticle.getArticleId());
            assertThat(result.getLocale()).isEqualTo(translatedArticle.getLocale());
            assertThat(result.getName()).isEqualTo(translatedArticle.getName());
            assertThat(result.getText()).isNotEqualTo(translatedArticle.getText());
            assertThat(result.getText()).isEqualTo(partialTranslatedArticle.getText());
        });
    }

    @Test
    void shouldUpdateTranslatedArticleWithMapId() {
        TranslatedArticleWithMapId translatedArticle = translatedArticleWithMapId();

        TranslatedArticleWithMapId inserted = tarantoolTemplate.insert(translatedArticle, TranslatedArticleWithMapId.class);
        assertThat(inserted).isEqualTo(translatedArticle);

        TranslatedArticleWithMapId selected = tarantoolTemplate.selectById(translatedArticle.getMapId(), TranslatedArticleWithMapId.class);
        assertThat(selected).isEqualTo(translatedArticle);

        TranslatedArticleWithMapId partialTranslatedArticle = TranslatedArticleWithMapId.builder()
                .text("Simple article text")
                .build();
        List<? extends Serializable> translatedArticleIdParts = List.of(translatedArticle.getArticleId(), translatedArticle.getLocale().toLanguageTag());
        List<TranslatedArticleWithMapId> updated = tarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, translatedArticleIdParts), partialTranslatedArticle, TranslatedArticleWithMapId.class);
        assertWith(updated.get(0), result -> {
            assertThat(result.getArticleId()).isEqualTo(translatedArticle.getArticleId());
            assertThat(result.getLocale()).isEqualTo(translatedArticle.getLocale());
            assertThat(result.getName()).isEqualTo(translatedArticle.getName());
            assertThat(result.getText()).isNotEqualTo(translatedArticle.getText());
            assertThat(result.getText()).isEqualTo(partialTranslatedArticle.getText());
        });
    }

    @Test
    void shouldCountArticles() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();

        tarantoolTemplate.insert(article1, Article.class);
        tarantoolTemplate.insert(article2, Article.class);

        Long count = tarantoolTemplate.count(Article.class);
        assertThat(count).isEqualTo(2L);

        count = tarantoolTemplate.count(Conditions.any(), Article.class);
        assertThat(count).isEqualTo(2L);

        count = tarantoolTemplate.count(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(article1.getId())), Article.class);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void shouldDeleteArticle() {
        Article article1 = article();
        Article article2 = article();

        tarantoolTemplate.insert(article1, Article.class);
        tarantoolTemplate.insert(article2, Article.class);

        Long count = tarantoolTemplate.count(Article.class);
        assertThat(count).isEqualTo(2L);

        Article deleted = tarantoolTemplate.delete(article1, Article.class);
        assertThat(deleted).isEqualTo(article1);

        count = tarantoolTemplate.count(Article.class);
        assertThat(count).isEqualTo(1L);

        deleted = tarantoolTemplate.deleteById(article2.getId(), Article.class);
        assertThat(deleted).isEqualTo(article2);

        count = tarantoolTemplate.count(Article.class);
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void shouldDeleteTranslatedArticle() {
        TranslatedArticle translatedArticle1 = translatedArticle();
        TranslatedArticle translatedArticle2 = translatedArticle();

        tarantoolTemplate.insert(translatedArticle1, TranslatedArticle.class);
        tarantoolTemplate.insert(translatedArticle2, TranslatedArticle.class);

        Long count = tarantoolTemplate.count(TranslatedArticle.class);
        assertThat(count).isEqualTo(2L);

        TranslatedArticle deleted = tarantoolTemplate.delete(translatedArticle1, TranslatedArticle.class);
        assertThat(deleted).isEqualTo(translatedArticle1);

        count = tarantoolTemplate.count(TranslatedArticle.class);
        assertThat(count).isEqualTo(1L);

        deleted = tarantoolTemplate.deleteById(translatedArticle2.getId(), TranslatedArticle.class);
        assertThat(deleted).isEqualTo(translatedArticle2);

        count = tarantoolTemplate.count(TranslatedArticle.class);
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void shouldDeleteTranslatedArticleWithFlatKey() {
        TranslatedArticleWithFlatKey translatedArticle = translatedArticleWithFlatKey();

        TranslatedArticleWithFlatKey inserted = tarantoolTemplate.insert(translatedArticle, TranslatedArticleWithFlatKey.class);
        assertThat(inserted).isEqualTo(translatedArticle);

        Long count = tarantoolTemplate.count(TranslatedArticleWithFlatKey.class);
        assertThat(count).isEqualTo(1L);

        TranslatedArticleWithFlatKey deleted = tarantoolTemplate.delete(translatedArticle, TranslatedArticleWithFlatKey.class);
        assertThat(deleted).isEqualTo(translatedArticle);

        count = tarantoolTemplate.count(TranslatedArticleWithFlatKey.class);
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void shouldDeleteTranslatedArticleWithMapId() {
        TranslatedArticleWithMapId translatedArticle = translatedArticleWithMapId();

        TranslatedArticleWithMapId inserted = tarantoolTemplate.insert(translatedArticle, TranslatedArticleWithMapId.class);
        assertThat(inserted).isEqualTo(translatedArticle);

        Long count = tarantoolTemplate.count(TranslatedArticleWithMapId.class);
        assertThat(count).isEqualTo(1L);

        TranslatedArticleWithMapId deleted = tarantoolTemplate.delete(translatedArticle, TranslatedArticleWithMapId.class);
        assertThat(deleted).isEqualTo(translatedArticle);

        count = tarantoolTemplate.count(TranslatedArticleWithMapId.class);
        assertThat(count).isEqualTo(0L);
    }

    @Test
    void shouldSelectArticles() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();
        Article article3 = simpleArticle();
        Article article4 = simpleArticle();
        Article article5 = simpleArticle();

        List.of(article1, article2, article3, article4, article5).forEach(article -> tarantoolTemplate.insert(article, Article.class));

        List<Article> all = tarantoolTemplate.select(Article.class);
        assertThat(all).hasSize(5);

        List<Article> selected = tarantoolTemplate.select(Conditions.any(), Article.class);
        assertThat(selected).hasSize(5);

        Article single = tarantoolTemplate.selectById(article1.getId(), Article.class);
        assertWith(single, articleAssertConsumer(article1));

        List<Article> articles = tarantoolTemplate.selectByIds(List.of(article2.getId(), article3.getId()), Article.class);
        assertThat(articles).hasSize(2);

        Article notFound = tarantoolTemplate.selectOne(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(UUID.randomUUID())), Article.class);
        assertThat(notFound).isNull();
    }

    @Test
    void shouldSelectTranslatedArticles() {
        TranslatedArticle article1 = translatedArticle();
        TranslatedArticleWithFlatKey article2 = translatedArticleWithFlatKey();
        TranslatedArticleWithFlatKey article3 = translatedArticleWithFlatKey();
        TranslatedArticleWithFlatKey article4 = translatedArticleWithFlatKey();
        TranslatedArticleWithFlatKey article5 = translatedArticleWithFlatKey();

        tarantoolTemplate.insert(article1, TranslatedArticle.class);

        List.of(article2, article3, article4, article5).forEach(article -> tarantoolTemplate.insert(article, TranslatedArticleWithFlatKey.class));

        List<TranslatedArticle> all = tarantoolTemplate.select(TranslatedArticle.class);
        assertThat(all).hasSize(5);

        List<TranslatedArticle> selected = tarantoolTemplate.select(Conditions.any(), TranslatedArticle.class);
        assertThat(selected).hasSize(5);

        TranslatedArticle one = tarantoolTemplate.selectById(article1.getId(), TranslatedArticle.class);
        assertWith(one, articleAssertConsumer(article1));

        MapId article2MapId = MapIdFactory.id(MapId.class)
                .with("articleId", article2.getArticleId())
                .with("locale", article2.getLocale());
        TranslatedArticleWithFlatKey two = tarantoolTemplate.selectById(article2MapId, TranslatedArticleWithFlatKey.class);
        assertWith(two, articleAssertConsumer(article2));

        TranslatedArticleKeyInterface article3Key = MapIdFactory.id(TranslatedArticleKeyInterface.class)
                .setArticleId(article3.getArticleId())
                .setLocale(article3.getLocale());
        TranslatedArticleWithFlatKey three = tarantoolTemplate.selectById(article3Key, TranslatedArticleWithFlatKey.class);
        assertWith(three, articleAssertConsumer(article3));

        MapId article4MapId = new BasicMapId()
                .with("articleId", article4.getArticleId())
                .with("locale", article4.getLocale());
        TranslatedArticleWithFlatKey four = tarantoolTemplate.selectById(article4MapId, TranslatedArticleWithFlatKey.class);
        assertWith(four, articleAssertConsumer(article4));
    }

    @Test
    void shouldCallAndGetError() {
        assertThatThrownBy(() -> tarantoolTemplate.callForAll("raise_error", Article.class))
                .isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageEndingWith("Error from raise_error function");

        assertThatThrownBy(() -> tarantoolTemplate.call("raise_error", Article.class))
                .isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageEndingWith("Error from raise_error function");

        assertThatThrownBy(() -> tarantoolTemplate.callForAll("get_error", Article.class))
                .isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageStartingWith("Error from get_error function");

        assertThatThrownBy(() -> tarantoolTemplate.call("get_error", Article.class))
                .isInstanceOf(TarantoolDataRetrievalException.class)
                .hasMessageStartingWith("Error from get_error function");
    }

    @Test
    void shouldCallAndGetNil() {
        List<Article> empty = tarantoolTemplate.callForAll("get_nil", Article.class);
        assertThat(empty).isEmpty();

        Article nothing = tarantoolTemplate.call("get_nil", Article.class);
        assertThat(nothing).isNull();
    }

    @Test
    void shouldCallAndGetNothing() {
        List<Article> empty = tarantoolTemplate.callForAll("get_nothing", Article.class);
        assertThat(empty).isEmpty();

        Article nothing = tarantoolTemplate.call("get_nothing", Article.class);
        assertThat(nothing).isNull();
    }

    @Test
    void shouldCallForArticle() {
        Article article = article();

        Article inserted = tarantoolTemplate.insert(article, Article.class);
        assertThat(inserted).isEqualTo(article);

        Article received = tarantoolTemplate.call("get_article_by_entity", List.of(article), Article.class);
        assertWith(received, articleAssertConsumer(article));
    }

    @Test
    void shouldCallForArticleElement() {
        User user = user();
        Article article = article();
        article.setUserId(user.getId());
        Comment comment = comment();
        comment.setArticleId(article.getId());
        comment.setUserId(article.getUserId());

        tarantoolTemplate.insert(article, Article.class);
        tarantoolTemplate.insert(user, User.class);
        tarantoolTemplate.insert(comment, Comment.class);

        ArticleElement received = tarantoolTemplate.call("get_article_element_by_id", List.of(article.getId()), ArticleElement.class);
        assertWith(received, articleElement -> {
            assertThat(articleElement.getId()).isEqualTo(article.getId());
            assertThat(articleElement.getName()).isEqualTo(article.getName());
            assertThat(articleElement.getSlug()).isEqualTo(article.getSlug());
            assertThat(articleElement.getPublishDate().withNano(0)).isEqualTo(article.getPublishDate().withNano(0));
            assertThat(articleElement.getUserId()).isEqualTo(article.getUserId());
            assertThat(articleElement.getTags()).isEqualTo(article.getTags());
            assertThat(articleElement.getLikes()).isEqualTo(article.getLikes());

            assertThat(articleElement.getUser()).isNotNull();
            assertThat(articleElement.getUser().getId()).isEqualTo(user.getId());
            assertThat(articleElement.getUser().getFirstName()).isEqualTo(user.getFirstName());
            assertThat(articleElement.getUser().getLastName()).isEqualTo(user.getLastName());
            assertThat(articleElement.getUser().getEmail()).isEqualTo(user.getEmail());
            assertThat(articleElement.getUser().getAddress()).isEqualTo(user.getAddress());

            assertThat(articleElement.getComments()).isNotNull();
            assertThat(articleElement.getComments()).hasSize(1);
            assertThat(articleElement.getComments().get(0).getId()).isEqualTo(comment.getId());
            assertThat(articleElement.getComments().get(0).getArticleId()).isEqualTo(comment.getArticleId());
            assertThat(articleElement.getComments().get(0).getUserId()).isEqualTo(comment.getUserId());
            assertThat(articleElement.getComments().get(0).getValue()).isEqualTo(comment.getValue());
            assertThat(articleElement.getComments().get(0).getLikes()).isEqualTo(comment.getLikes());
        });
    }

    @Test
    void shouldCallForCountArticles() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();
        article2.setUserId(article1.getUserId());

        List.of(article1, article2).forEach(article -> tarantoolTemplate.insert(article, Article.class));

        Long received = tarantoolTemplate.call("count_articles_by_user_id", List.of(article1.getUserId()), Long.class);
        assertThat(received).isEqualTo(2L);
    }

    @Test
    void shouldCallForArticles() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();

        List.of(article1, article2).forEach(article -> tarantoolTemplate.insert(article, Article.class));

        List<Article> received = tarantoolTemplate.callForAll("get_articles", Article.class);
        assertThat(received).hasSize(2);

        Set<UUID> uuids = received.stream().map(Article::getId).collect(Collectors.toSet());
        assertThat(uuids).contains(article1.getId());
        assertThat(uuids).contains(article2.getId());
    }

    @Test
    void shouldCallForArticlesByUser() {
        User user = user();
        Article article = article();
        article.setUserId(user.getId());

        Article inserted = tarantoolTemplate.insert(article, Article.class);
        assertThat(inserted).isEqualTo(article);

        List<Article> received = tarantoolTemplate.callForAll("get_articles_by_user", List.of(user), Article.class);
        assertWith(received.get(0), articleAssertConsumer(article));
    }

    @Test
    void shouldCallForArticlesByUserId() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();
        Article article3 = simpleArticle();
        article2.setUserId(article1.getUserId());

        List.of(article1, article2, article3).forEach(article -> tarantoolTemplate.insert(article, Article.class));

        List<Article> received = tarantoolTemplate.callForAll("get_articles_by_user_id", List.of(article1.getUserId()), Article.class);
        assertThat(received).hasSize(2);

        Set<UUID> uuids = received.stream().map(Article::getId).collect(Collectors.toSet());
        assertThat(uuids).contains(article1.getId());
        assertThat(uuids).contains(article2.getId());
    }

}
