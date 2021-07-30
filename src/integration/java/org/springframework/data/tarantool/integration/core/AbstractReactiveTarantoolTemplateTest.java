package org.springframework.data.tarantool.integration.core;

import io.tarantool.driver.TarantoolVersion;
import io.tarantool.driver.api.conditions.Conditions;
import io.tarantool.driver.protocol.TarantoolIndexQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy;
import org.springframework.data.tarantool.TarantoolDataRetrievalException;
import org.springframework.data.tarantool.config.client.DefaultTarantoolClientFactory;
import org.springframework.data.tarantool.config.client.TarantoolClientFactory;
import org.springframework.data.tarantool.config.client.TarantoolClientOptions;
import org.springframework.data.tarantool.core.DefaultTarantoolExceptionTranslator;
import org.springframework.data.tarantool.core.ReactiveTarantoolTemplate;
import org.springframework.data.tarantool.core.TarantoolExceptionTranslator;
import org.springframework.data.tarantool.core.convert.MappingTarantoolConverter;
import org.springframework.data.tarantool.core.convert.TarantoolCustomConversions;
import org.springframework.data.tarantool.core.mapping.BasicMapId;
import org.springframework.data.tarantool.core.mapping.MapId;
import org.springframework.data.tarantool.core.mapping.MapIdFactory;
import org.springframework.data.tarantool.core.mapping.TarantoolMappingContext;
import org.springframework.data.tarantool.integration.core.convert.LocaleToStringConverter;
import org.springframework.data.tarantool.integration.core.convert.StringToLocaleConverter;
import org.springframework.data.tarantool.integration.domain.*;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * Class contains all test methods for Tarantool reactive template usage
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class AbstractReactiveTarantoolTemplateTest {

    private ReactiveTarantoolTemplate reactiveTarantoolTemplate;

    public abstract TarantoolClientOptions getOptions();

    @BeforeAll
    void setUp() {
        TarantoolClientFactory clientFactory = new DefaultTarantoolClientFactory(getOptions());

        TarantoolMappingContext mappingContext = new TarantoolMappingContext();
        mappingContext.setFieldNamingStrategy(new SnakeCaseFieldNamingStrategy());

        MappingTarantoolConverter converter = new MappingTarantoolConverter(mappingContext);
        converter.setCustomConversions(new TarantoolCustomConversions(List.of(new LocaleToStringConverter(), new StringToLocaleConverter())));
        converter.afterPropertiesSet();

        TarantoolExceptionTranslator exceptionTranslator = new DefaultTarantoolExceptionTranslator();

        reactiveTarantoolTemplate = new ReactiveTarantoolTemplate(clientFactory.createClient(), converter, exceptionTranslator);
    }

    @BeforeEach
    void clear() {
        Flux.concat(
                reactiveTarantoolTemplate.delete(Conditions.any(), Article.class),
                reactiveTarantoolTemplate.delete(Conditions.any(), User.class),
                reactiveTarantoolTemplate.delete(Conditions.any(), Comment.class),
                reactiveTarantoolTemplate.delete(Conditions.any(), TranslatedArticle.class)
        )
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldGetVersion() {
        TarantoolVersion version = reactiveTarantoolTemplate.getVersion();
        assertThat(version).isNotNull();
        assertThat(version.toString()).contains("Tarantool 2.9.0");
    }

    @Test
    void shouldInsertArticle() {
        Article article = article();

        reactiveTarantoolTemplate.selectById(article.getId(), Article.class).as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.insert(article, Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(article.getId(), Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();
    }

    @Test
    void shouldNotInsertEmptyArticle() {
        Article article = emptyArticle();

        reactiveTarantoolTemplate.insert(article, Article.class).as(StepVerifier::create)
                .expectErrorSatisfies(throwable ->
                        assertThat(throwable).isInstanceOf(TarantoolDataRetrievalException.class)
                                .hasMessageEndingWith("required by space format is missing")
                )
                .verify();
    }

    @Test
    void shouldInsertTranslatedArticle() {
        TranslatedArticle translatedArticle = translatedArticle();
        TranslatedArticleKey translatedArticleId = translatedArticle.getId();

        reactiveTarantoolTemplate.selectById(translatedArticleId, TranslatedArticle.class).as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.insert(translatedArticle, TranslatedArticle.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(translatedArticleId, TranslatedArticle.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(translatedArticle))
                .verifyComplete();
    }

    @Test
    void shouldInsertTranslatedArticleWithFlatKey() {
        TranslatedArticleWithFlatKey translatedArticle = translatedArticleWithFlatKey();
        MapId translatedArticleMapId = MapIdFactory.id(MapId.class)
                .with("articleId", translatedArticle.getArticleId())
                .with("locale", translatedArticle.getLocale());

        reactiveTarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.insert(translatedArticle, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(translatedArticle))
                .verifyComplete();
    }

    @Test
    void shouldInsertTranslatedArticleWithMapId() {
        TranslatedArticleWithMapId translatedArticle = translatedArticleWithMapId();
        MapId translatedArticleMapId = translatedArticle.getMapId();

        reactiveTarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithMapId.class).as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.insert(translatedArticle, TranslatedArticleWithMapId.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithMapId.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(translatedArticle))
                .verifyComplete();
    }

    @Test
    void shouldReplaceArticle() {
        Article article = simpleArticle();

        reactiveTarantoolTemplate.insert(article, Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();

        article.setLikes(10);
        reactiveTarantoolTemplate.replace(article, Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();
    }

    @Test
    void shouldInsertArticleWithReplace() {
        Article article = simpleArticle();

        reactiveTarantoolTemplate.replace(article, Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(article.getId(), Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();
    }

    @Test
    void shouldUpdateArticle() {
        Article article = article();

        reactiveTarantoolTemplate.insert(article, Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(article.getId(), Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();

        Article partialArticle = Article.builder()
                .tags(List.of(new Tag("test")))
                .likes(article.getLikes() + 1)
                .build();
        article.setTags(partialArticle.getTags());
        article.setLikes(partialArticle.getLikes());
        reactiveTarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(article.getId())), partialArticle, Article.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(article))
                .verifyComplete();
    }

    @Test
    void shouldUpdateArticles() {
        Article article1 = article();
        Article article2 = article();

        Flux.concat(reactiveTarantoolTemplate.insert(article1, Article.class), reactiveTarantoolTemplate.insert(article2, Article.class))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        Article partialArticle = Article.builder()
                .slug("Single slug about tarantool")
                .build();
        reactiveTarantoolTemplate.update(Conditions.any(), partialArticle, Article.class).as(StepVerifier::create)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(article -> Set.of(article1.getId(), article2.getId()).contains(article.getId()))
                .consumeRecordedWith(articles -> {
                    assertThat(articles).hasSize(2);
                    articles.forEach(article -> assertThat(article.getSlug()).isEqualTo(partialArticle.getSlug()));
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateTranslatedArticle() {
        TranslatedArticle translatedArticle = translatedArticle();

        reactiveTarantoolTemplate.insert(translatedArticle, TranslatedArticle.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(translatedArticle.getId(), TranslatedArticle.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        TranslatedArticle partialTranslatedArticle = TranslatedArticle.builder()
                .text("Simple article text")
                .build();
        List<? extends Serializable> translatedArticleIdParts = List.of(translatedArticle.getId().getArticleId(), translatedArticle.getId().getLocale().toLanguageTag());
        reactiveTarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, translatedArticleIdParts), partialTranslatedArticle, TranslatedArticle.class).as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result.getId()).isEqualTo(translatedArticle.getId());
                    assertThat(result.getName()).isEqualTo(translatedArticle.getName());
                    assertThat(result.getText()).isNotEqualTo(translatedArticle.getText());
                    assertThat(result.getText()).isEqualTo(partialTranslatedArticle.getText());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateTranslatedArticleWithFlatKey() {
        TranslatedArticleWithFlatKey translatedArticle = translatedArticleWithFlatKey();
        MapId translatedArticleMapId = MapIdFactory.id(MapId.class)
                .with("articleId", translatedArticle.getArticleId())
                .with("locale", translatedArticle.getLocale());

        reactiveTarantoolTemplate.insert(translatedArticle, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(translatedArticleMapId, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        TranslatedArticleWithFlatKey partialTranslatedArticle = TranslatedArticleWithFlatKey.builder()
                .text("Simple article text")
                .build();
        List<? extends Serializable> translatedArticleIdParts = List.of(translatedArticle.getArticleId(), translatedArticle.getLocale().toLanguageTag());
        reactiveTarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, translatedArticleIdParts), partialTranslatedArticle, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result.getArticleId()).isEqualTo(translatedArticle.getArticleId());
                    assertThat(result.getLocale()).isEqualTo(translatedArticle.getLocale());
                    assertThat(result.getName()).isEqualTo(translatedArticle.getName());
                    assertThat(result.getText()).isNotEqualTo(translatedArticle.getText());
                    assertThat(result.getText()).isEqualTo(partialTranslatedArticle.getText());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateTranslatedArticleWithMapId() {
        TranslatedArticleWithMapId translatedArticle = translatedArticleWithMapId();

        reactiveTarantoolTemplate.insert(translatedArticle, TranslatedArticleWithMapId.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(translatedArticle.getMapId(), TranslatedArticleWithMapId.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        TranslatedArticleWithMapId partialTranslatedArticle = TranslatedArticleWithMapId.builder()
                .text("Simple article text")
                .build();
        List<? extends Serializable> translatedArticleIdParts = List.of(translatedArticle.getArticleId(), translatedArticle.getLocale().toLanguageTag());
        reactiveTarantoolTemplate.update(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, translatedArticleIdParts), partialTranslatedArticle, TranslatedArticleWithMapId.class).as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result.getArticleId()).isEqualTo(translatedArticle.getArticleId());
                    assertThat(result.getLocale()).isEqualTo(translatedArticle.getLocale());
                    assertThat(result.getName()).isEqualTo(translatedArticle.getName());
                    assertThat(result.getText()).isNotEqualTo(translatedArticle.getText());
                    assertThat(result.getText()).isEqualTo(partialTranslatedArticle.getText());
                })
                .verifyComplete();
    }

    @Test
    void shouldCountArticles() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();

        Flux.concat(reactiveTarantoolTemplate.insert(article1, Article.class), reactiveTarantoolTemplate.insert(article2, Article.class))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.count(Article.class).as(StepVerifier::create).expectNext(2L).verifyComplete();
        reactiveTarantoolTemplate.count(Conditions.any(), Article.class).as(StepVerifier::create).expectNext(2L).verifyComplete();
        reactiveTarantoolTemplate.count(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(article1.getId())), Article.class).as(StepVerifier::create).expectNext(1L).verifyComplete();
    }

    @Test
    void shouldDeleteArticle() {
        Article article1 = article();
        Article article2 = article();

        Flux.concat(reactiveTarantoolTemplate.insert(article1, Article.class), reactiveTarantoolTemplate.insert(article2, Article.class))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.count(Article.class).as(StepVerifier::create).expectNext(2L).verifyComplete();

        reactiveTarantoolTemplate.delete(article1, Article.class).as(StepVerifier::create)
                .expectNext(article1)
                .verifyComplete();

        reactiveTarantoolTemplate.count(Article.class).as(StepVerifier::create).expectNext(1L).verifyComplete();

        reactiveTarantoolTemplate.deleteById(article2.getId(), Article.class).as(StepVerifier::create)
                .expectNext(article2)
                .verifyComplete();

        reactiveTarantoolTemplate.count(Article.class).as(StepVerifier::create).expectNext(0L).verifyComplete();
    }

    @Test
    void shouldDeleteTranslatedArticle() {
        TranslatedArticle translatedArticle1 = translatedArticle();
        TranslatedArticle translatedArticle2 = translatedArticle();

        Flux.concat(
                reactiveTarantoolTemplate.insert(translatedArticle1, TranslatedArticle.class),
                reactiveTarantoolTemplate.insert(translatedArticle2, TranslatedArticle.class)
        )
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.count(TranslatedArticle.class).as(StepVerifier::create).expectNext(2L).verifyComplete();

        reactiveTarantoolTemplate.delete(translatedArticle1, TranslatedArticle.class).as(StepVerifier::create)
                .expectNext(translatedArticle1)
                .verifyComplete();

        reactiveTarantoolTemplate.count(TranslatedArticle.class).as(StepVerifier::create).expectNext(1L).verifyComplete();

        reactiveTarantoolTemplate.deleteById(translatedArticle2.getId(), TranslatedArticle.class).as(StepVerifier::create)
                .expectNext(translatedArticle2)
                .verifyComplete();

        reactiveTarantoolTemplate.count(TranslatedArticle.class).as(StepVerifier::create).expectNext(0L).verifyComplete();
    }

    @Test
    void shouldDeleteTranslatedArticleWithFlatKey() {
        TranslatedArticleWithFlatKey translatedArticle = translatedArticleWithFlatKey();

        reactiveTarantoolTemplate.insert(translatedArticle, TranslatedArticleWithFlatKey.class)
                .as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.count(TranslatedArticleWithFlatKey.class).as(StepVerifier::create).expectNext(1L).verifyComplete();

        reactiveTarantoolTemplate.delete(translatedArticle, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.count(TranslatedArticleWithFlatKey.class).as(StepVerifier::create).expectNext(0L).verifyComplete();
    }

    @Test
    void shouldDeleteTranslatedArticleWithMapId() {
        TranslatedArticleWithMapId translatedArticle = translatedArticleWithMapId();

        reactiveTarantoolTemplate.insert(translatedArticle, TranslatedArticleWithMapId.class)
                .as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.count(TranslatedArticleWithMapId.class).as(StepVerifier::create).expectNext(1L).verifyComplete();

        reactiveTarantoolTemplate.delete(translatedArticle, TranslatedArticleWithMapId.class).as(StepVerifier::create)
                .expectNext(translatedArticle)
                .verifyComplete();

        reactiveTarantoolTemplate.count(TranslatedArticleWithMapId.class).as(StepVerifier::create).expectNext(0L).verifyComplete();
    }

    @Test
    void shouldSelectArticles() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();
        Article article3 = simpleArticle();
        Article article4 = simpleArticle();
        Article article5 = simpleArticle();

        Flux.just(article1, article2, article3, article4, article5)
                .flatMap(article -> reactiveTarantoolTemplate.insert(article, Article.class))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.select(Article.class).as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();

        reactiveTarantoolTemplate.select(Conditions.any(), Article.class).as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(article1.getId(), Article.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(article1))
                .verifyComplete();

        reactiveTarantoolTemplate.selectByIds(Flux.just(article2.getId(), article3.getId()), Article.class).as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();

        reactiveTarantoolTemplate.selectOne(Conditions.indexEquals(TarantoolIndexQuery.PRIMARY, List.of(UUID.randomUUID())), Article.class).as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldSelectTranslatedArticles() {
        TranslatedArticle article1 = translatedArticle();
        TranslatedArticleWithFlatKey article2 = translatedArticleWithFlatKey();
        TranslatedArticleWithFlatKey article3 = translatedArticleWithFlatKey();
        TranslatedArticleWithFlatKey article4 = translatedArticleWithFlatKey();
        TranslatedArticleWithFlatKey article5 = translatedArticleWithFlatKey();

        reactiveTarantoolTemplate.insert(article1, TranslatedArticle.class)
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        Flux.just(article2, article3, article4, article5)
                .flatMap(article -> reactiveTarantoolTemplate.insert(article, TranslatedArticleWithFlatKey.class))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.select(TranslatedArticle.class).as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();

        reactiveTarantoolTemplate.select(Conditions.any(), TranslatedArticle.class).as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();

        reactiveTarantoolTemplate.selectById(article1.getId(), TranslatedArticle.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(article1))
                .verifyComplete();

        MapId article2MapId = MapIdFactory.id(MapId.class)
                .with("articleId", article2.getArticleId())
                .with("locale", article2.getLocale());
        reactiveTarantoolTemplate.selectById(article2MapId, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(article2))
                .verifyComplete();

        TranslatedArticleKeyInterface article3Key = MapIdFactory.id(TranslatedArticleKeyInterface.class)
                .setArticleId(article3.getArticleId())
                .setLocale(article3.getLocale());
        reactiveTarantoolTemplate.selectById(article3Key, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(article3))
                .verifyComplete();

        MapId article4MapId = new BasicMapId()
                .with("articleId", article4.getArticleId())
                .with("locale", article4.getLocale());
        reactiveTarantoolTemplate.selectById(article4MapId, TranslatedArticleWithFlatKey.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(article4))
                .verifyComplete();
    }

    @Test
    void shouldCallAndGetError() {
        reactiveTarantoolTemplate.callForAll("raise_error", Article.class).as(StepVerifier::create)
                .expectErrorSatisfies(throwable ->
                        assertThat(throwable).isInstanceOf(TarantoolDataRetrievalException.class)
                                .hasMessageEndingWith("Error from raise_error function")
                )
                .verify();
        reactiveTarantoolTemplate.call("raise_error", Article.class).as(StepVerifier::create)
                .expectErrorSatisfies(throwable ->
                        assertThat(throwable).isInstanceOf(TarantoolDataRetrievalException.class)
                                .hasMessageEndingWith("Error from raise_error function")
                )
                .verify();
        reactiveTarantoolTemplate.callForAll("get_error", Article.class).as(StepVerifier::create)
                .expectErrorSatisfies(throwable ->
                        assertThat(throwable).isInstanceOf(TarantoolDataRetrievalException.class)
                                .hasMessageStartingWith("Error from get_error function")
                )
                .verify();
        reactiveTarantoolTemplate.call("get_error", Article.class).as(StepVerifier::create)
                .expectErrorSatisfies(throwable ->
                        assertThat(throwable).isInstanceOf(TarantoolDataRetrievalException.class)
                                .hasMessageStartingWith("Error from get_error function")
                )
                .verify();
    }

    @Test
    void shouldCallAndGetNil() {
        reactiveTarantoolTemplate.callForAll("get_nil", Article.class).as(StepVerifier::create).verifyComplete();
        reactiveTarantoolTemplate.call("get_nil", Article.class).as(StepVerifier::create).verifyComplete();
    }

    @Test
    void shouldCallAndGetNothing() {
        reactiveTarantoolTemplate.callForAll("get_nothing", Article.class).as(StepVerifier::create).verifyComplete();
        reactiveTarantoolTemplate.call("get_nothing", Article.class).as(StepVerifier::create).verifyComplete();
    }

    @Test
    void shouldCallForArticle() {
        Article article = article();

        reactiveTarantoolTemplate.insert(article, Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();

        reactiveTarantoolTemplate.call("get_article_by_entity", List.of(article), Article.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(article))
                .verifyComplete();
    }

    @Test
    void shouldCallForArticleElement() {
        User user = user();
        Article article = article();
        article.setUserId(user.getId());
        Comment comment = comment();
        comment.setArticleId(article.getId());
        comment.setUserId(article.getUserId());

        Flux.concat(
                reactiveTarantoolTemplate.insert(article, Article.class),
                reactiveTarantoolTemplate.insert(user, User.class),
                reactiveTarantoolTemplate.insert(comment, Comment.class)
        )
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.call("get_article_element_by_id", List.of(article.getId()), ArticleElement.class).as(StepVerifier::create)
                .assertNext(articleElement -> {
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
                })
                .verifyComplete();
    }

    @Test
    void shouldCallForCountArticles() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();
        article2.setUserId(article1.getUserId());

        Flux.just(article1, article2)
                .flatMap(article -> reactiveTarantoolTemplate.insert(article, Article.class))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.call("count_articles_by_user_id", List.of(article1.getUserId()), Long.class).as(StepVerifier::create)
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void shouldCallForArticles() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();

        Flux.just(article1, article2)
                .flatMap(article -> reactiveTarantoolTemplate.insert(article, Article.class))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.callForAll("get_articles", Article.class).as(StepVerifier::create)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(article -> Set.of(article1.getId(), article2.getId()).contains(article.getId()))
                .consumeRecordedWith(articles -> assertThat(articles).hasSize(2))
                .verifyComplete();
    }

    @Test
    void shouldCallForArticlesByUser() {
        User user = user();
        Article article = article();
        article.setUserId(user.getId());

        reactiveTarantoolTemplate.insert(article, Article.class).as(StepVerifier::create)
                .expectNext(article)
                .verifyComplete();

        reactiveTarantoolTemplate.callForAll("get_articles_by_user", List.of(user), Article.class).as(StepVerifier::create)
                .assertNext(articleAssertConsumer(article))
                .verifyComplete();
    }

    @Test
    void shouldCallForArticlesByUserId() {
        Article article1 = simpleArticle();
        Article article2 = simpleArticle();
        Article article3 = simpleArticle();
        article2.setUserId(article1.getUserId());

        Flux.just(article1, article2, article3)
                .flatMap(article -> reactiveTarantoolTemplate.insert(article, Article.class))
                .then()
                .as(StepVerifier::create)
                .verifyComplete();

        reactiveTarantoolTemplate.callForAll("get_articles_by_user_id", List.of(article1.getUserId()), Article.class).as(StepVerifier::create)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(article -> Set.of(article1.getId(), article2.getId()).contains(article.getId()))
                .consumeRecordedWith(articles -> assertThat(articles).hasSize(2))
                .verifyComplete();
    }

    private Consumer<Article> articleAssertConsumer(Article expected) {
        return (Article actual) -> {
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getSlug()).isEqualTo(expected.getSlug());
            assertThat(actual.getPublishDate().withNano(0)).isEqualTo(expected.getPublishDate().withNano(0));
            assertThat(actual.getUserId()).isEqualTo(expected.getUserId());
            assertThat(actual.getTags()).isEqualTo(expected.getTags());
            assertThat(actual.getLikes()).isEqualTo(expected.getLikes());
        };
    }

    private Consumer<TranslatedArticle> articleAssertConsumer(TranslatedArticle expected) {
        return (TranslatedArticle actual) -> {
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getText()).isEqualTo(expected.getText());
        };
    }

    private Consumer<TranslatedArticleWithFlatKey> articleAssertConsumer(TranslatedArticleWithFlatKey expected) {
        return (TranslatedArticleWithFlatKey actual) -> {
            assertThat(actual.getArticleId()).isEqualTo(expected.getArticleId());
            assertThat(actual.getLocale()).isEqualTo(expected.getLocale());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getText()).isEqualTo(expected.getText());
        };
    }

    private Consumer<TranslatedArticleWithMapId> articleAssertConsumer(TranslatedArticleWithMapId expected) {
        return (TranslatedArticleWithMapId actual) -> {
            assertThat(actual.getArticleId()).isEqualTo(expected.getArticleId());
            assertThat(actual.getLocale()).isEqualTo(expected.getLocale());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getText()).isEqualTo(expected.getText());
        };
    }

    private static User user() {
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

    private static Article article() {
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

    private static Article simpleArticle() {
        return Article.builder()
                .id(UUID.randomUUID())
                .name("Selevinia eats tarantool")
                .publishDate(LocalDateTime.now())
                .userId(UUID.randomUUID())
                .build();
    }

    private static Article emptyArticle() {
        return Article.builder()
                .id(UUID.randomUUID())
                .build();
    }

    private static TranslatedArticle translatedArticle() {
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

    private static TranslatedArticleWithFlatKey translatedArticleWithFlatKey() {
        return TranslatedArticleWithFlatKey.builder()
                .articleId(UUID.randomUUID())
                .locale(Locale.ENGLISH)
                .name("Selevinia eats tarantool")
                .build();
    }

    private static TranslatedArticleWithMapId translatedArticleWithMapId() {
        return TranslatedArticleWithMapId.builder()
                .articleId(UUID.randomUUID())
                .locale(Locale.ENGLISH)
                .name("Selevinia eats tarantool")
                .build();
    }

    private static Comment comment() {
        return Comment.builder()
                .id(UUID.randomUUID())
                .articleId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .value("This is the best article about tarantool")
                .likes(1)
                .build();
    }
}