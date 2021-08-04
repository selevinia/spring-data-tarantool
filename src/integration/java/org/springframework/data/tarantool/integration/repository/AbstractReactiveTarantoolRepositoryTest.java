package org.springframework.data.tarantool.integration.repository;

import io.tarantool.driver.api.conditions.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.tarantool.core.ReactiveTarantoolOperations;
import org.springframework.data.tarantool.integration.domain.DistributedUser;
import org.springframework.data.tarantool.integration.domain.TranslatedArticle;
import org.springframework.data.tarantool.integration.domain.TranslatedArticleKey;
import org.springframework.data.tarantool.integration.domain.User;
import org.springframework.data.tarantool.repository.Query;
import org.springframework.data.tarantool.repository.ReactiveTarantoolRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newTranslatedArticle;
import static org.springframework.data.tarantool.integration.repository.util.TestData.newUser;

/**
 * Class contains all test methods for Tarantool reactive repository usage
 */
public abstract class AbstractReactiveTarantoolRepositoryTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected DistributedUserRepository distributedUserRepository;

    @Autowired
    protected TranslatedArticleRepository translatedArticleRepository;

    @Autowired
    protected ReactiveTarantoolOperations operations;

    @BeforeEach
    void cleanup() {
        operations.delete(Conditions.any(), User.class).then().as(StepVerifier::create).verifyComplete();
    }

    @Test
    void shouldNotFindNotExisted() {
        userRepository.findById(UUID.randomUUID()).as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void shouldDoSaveAndFindById() {
        User user = newUser();
        userRepository.save(user).as(StepVerifier::create)
                .expectNext(user)
                .verifyComplete();

        userRepository.findById(user.getId()).as(StepVerifier::create)
                .assertNext(actual -> {
                    assertThat(actual.getId()).isEqualTo(user.getId());
                    assertThat(actual.getVersion()).isEqualTo(0L);
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateVersionedEntity() {
        User user = newUser();
        userRepository.save(user)
                .map(u -> {
                    u.setFirstName("New FirstName");
                    return u;
                })
                .flatMap(u -> userRepository.save(u))
                .as(StepVerifier::create)
                .assertNext(actual -> {
                    assertThat(actual.getId()).isEqualTo(user.getId());
                    assertThat(actual.getVersion()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void shouldDoExists() {
        User user = newUser();
        userRepository.save(user).as(StepVerifier::create)
                .expectNext(user)
                .verifyComplete();

        userRepository.existsById(user.getId()).as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldDoDelete() {
        User user = newUser();
        userRepository.save(user).as(StepVerifier::create)
                .expectNext(user)
                .verifyComplete();

        userRepository.delete(user).then().as(StepVerifier::create)
                .verifyComplete();

        userRepository.existsById(user.getId()).as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldDoDeleteById() {
        User user = newUser();
        userRepository.save(user).as(StepVerifier::create)
                .expectNext(user)
                .verifyComplete();

        userRepository.deleteById(user.getId()).then().as(StepVerifier::create)
                .verifyComplete();

        userRepository.existsById(user.getId()).as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldFindAll() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            userRepository.save(user).as(StepVerifier::create)
                    .expectNext(user)
                    .verifyComplete();
        }

        userRepository.findAll().as(StepVerifier::create)
                .expectNextCount(4L)
                .verifyComplete();
    }

    @Test
    void shouldNotFindAll() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            userRepository.save(user).as(StepVerifier::create)
                    .expectNext(user)
                    .verifyComplete();
        }

        userRepository.findAllById(List.of(UUID.randomUUID())).as(StepVerifier::create)
                .expectNextCount(0L)
                .verifyComplete();
    }

    @Test
    void shouldDoCount() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            userRepository.save(user).as(StepVerifier::create)
                    .expectNext(user)
                    .verifyComplete();
        }

        userRepository.count().as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    void shouldFindByLastNameUsingQuery() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            userRepository.save(user).as(StepVerifier::create)
                    .expectNext(user)
                    .verifyComplete();
        }

        userRepository.findUsersByLastName("Kuzin").as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void shouldCountByLastNameUsingQuery() {
        for (int i = 0; i < 4; i++) {
            User user = newUser();
            userRepository.save(user).as(StepVerifier::create)
                    .expectNext(user)
                    .verifyComplete();
        }

        userRepository.countUsersByLastName("Kuzin").as(StepVerifier::create)
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    void shouldFindByEntityUsingQuery() {
        User user = newUser();
        userRepository.save(user).as(StepVerifier::create)
                .expectNext(user)
                .verifyComplete();

        userRepository.findOneUser(user).as(StepVerifier::create)
                .assertNext(actual -> {
                    assertThat(actual.getId()).isEqualTo(user.getId());
                    assertThat(actual.getFirstName()).isEqualTo(user.getFirstName());
                    assertThat(actual.getLastName()).isEqualTo(user.getLastName());
                    assertThat(actual.getEmail()).isEqualTo(user.getEmail());
                })
                .verifyComplete();
    }

    @Test
    void shouldDoSaveAndFindByIdEntityWithCompositePrimaryKey() {
        TranslatedArticle article = newTranslatedArticle();
        translatedArticleRepository.save(article).as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        translatedArticleRepository.findById(article.getId()).as(StepVerifier::create)
                .assertNext(actual -> {
                    assertThat(actual.getId()).isEqualTo(article.getId());
                    assertThat(actual.getName()).isEqualTo(article.getName());
                    assertThat(actual.getText()).isEqualTo(article.getText());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateEntityWithCompositePrimaryKey() {
        TranslatedArticle article = newTranslatedArticle();
        translatedArticleRepository.save(article)
                .map(a -> {
                    a.setName("New name");
                    return a;
                })
                .flatMap(a -> translatedArticleRepository.save(a))
                .as(StepVerifier::create)
                .assertNext(actual -> {
                    assertThat(actual.getId()).isEqualTo(article.getId());
                    assertThat(actual.getName()).isEqualTo("New name");
                })
                .verifyComplete();
    }

    protected interface UserRepository extends ReactiveTarantoolRepository<User, UUID> {

        @Query(function = "find_user_by_user")
        Flux<User> findOneUser(User user);

        @Query(function = "find_users_by_last_name")
        Flux<User> findUsersByLastName(String lastName);

        @Query(function = "count_users_by_last_name")
        Mono<Long> countUsersByLastName(String lastName);

        @Query(function = "upload_users")
        Mono<Long> uploadUsers(List<User> users);
    }

    protected interface DistributedUserRepository extends ReactiveTarantoolRepository<DistributedUser, UUID> {
    }

    protected interface TranslatedArticleRepository extends ReactiveTarantoolRepository<TranslatedArticle, TranslatedArticleKey> {
    }
}
